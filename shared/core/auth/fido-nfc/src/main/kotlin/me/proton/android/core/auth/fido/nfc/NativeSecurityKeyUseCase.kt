/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.proton.android.core.auth.fido.nfc

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.activity.result.ActivityResultCaller
import java.security.MessageDigest
import java.util.Base64
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.ErrorCode
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.ErrorData
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.LaunchResult
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.Result
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.Result.Error
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.Result.NoCredentialsResponse
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.Result.Success
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey.SuccessResponseData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIDO2 assertion flow over NFC (CTAP2, ISO 14443 / IsoDep) without GMS.
 *
 * Contract mirrors the platform (GMS) implementation:
 *  - [register] stores the result callback (activity calls it in onCreate).
 *  - [invoke] is called on the main thread from lifecycleScope. It arms NFC
 *    reader mode and returns [LaunchResult.Success] immediately ("dialog
 *    launched"); the actual [Result] arrives later through the callback,
 *    when the security key is tapped and the CTAP2 exchange completes.
 *  - The caller tears the flow down by finishing the activity; Android
 *    disables reader mode with the activity.
 */
@Singleton
class NativeSecurityKeyUseCase @Inject constructor(
    private val context: Context,
) : PerformTwoFaWithSecurityKey<ActivityResultCaller, Activity> {

    @Volatile
    private var onResult: ((Result, Fido2PublicKeyCredentialRequestOptions) -> Unit)? = null

    @Volatile
    private var currentOptions: Fido2PublicKeyCredentialRequestOptions? = null

    @Volatile
    private var inProgress = false

    override fun register(
        caller: ActivityResultCaller,
        onResult: (Result, Fido2PublicKeyCredentialRequestOptions) -> Unit,
    ) {
        this.onResult = onResult
    }

    override suspend fun invoke(
        activity: Activity,
        publicKey: Fido2PublicKeyCredentialRequestOptions,
    ): LaunchResult {
        val adapter = NfcAdapter.getDefaultAdapter(activity)
            ?: return LaunchResult.Failure(
                FidoNativeException("NFC is not available on this device"),
            )
        if (!adapter.isEnabled) {
            return LaunchResult.Failure(
                FidoNativeException("NFC is disabled on this device"),
            )
        }
        if (inProgress) {
            return LaunchResult.Failure(
                FidoNativeException("A security key operation is already in progress"),
            )
        }

        currentOptions = publicKey
        inProgress = true

        val started = NfcReader { tag -> onTag(tag) }.start(activity)
        if (!started) {
            inProgress = false
            return LaunchResult.Failure(
                FidoNativeException("Could not start the NFC security key reader"),
            )
        }

        // Reader mode is armed: behave like the GMS dialog launch. The result
        // is delivered through the registered callback when the key is tapped.
        return LaunchResult.Success
    }

    private fun onTag(tag: Tag) {
        val options = currentOptions ?: return
        val callback = onResult ?: return
        inProgress = false
        currentOptions = null
        // The CTAP2 exchange blocks on IsoDep transceives: off the main thread.
        Thread {
            val result = runCatching { performAssertion(tag, options) }
                .getOrElse { ex -> errorResult(ex) }
            callback(result, options)
        }.apply { isDaemon = true }.start()
    }

    private fun performAssertion(
        tag: Tag,
        options: Fido2PublicKeyCredentialRequestOptions,
    ): Result {
        val transport = NfcCtap2Transport()
        return try {
            if (!transport.bindTag(tag)) {
                throw FidoNativeException("The tag does not support contactless (IsoDep) exchange")
            }
            if (!transport.selectFidoApplet()) {
                throw FidoNativeException("No FIDO2 security key applet found on the tag")
            }

            val appId = options.extensions?.appId?.takeIf { it.isNotBlank() }
            val rpId = options.rpId
                ?: return Error(
                    ErrorData(ErrorCode.CONSTRAINT_ERR, "Missing relying party id"),
                )
            // WebAuthn L2 §10.1 (appid): when the legacy appid extension is present the
            // AppID replaces the RP id in the CTAP2 request (U2F credentials are scoped
            // to the AppID), and the clientData origin is the AppID, not the facet.
            // Without it the origin is this app's android:apk-key-hash facet.
            val rpIdForKey = appId ?: rpId
            val origin = appId ?: facetId()
            if (origin.isEmpty()) {
                throw FidoNativeException("Could not determine the WebAuthn origin for this app")
            }
            val clientDataJson = buildClientDataJson(bytesOfUByteArray(options.challenge), origin)
            val clientDataHash = MessageDigest.getInstance("SHA-256").digest(clientDataJson.toByteArray())
            val request = Ctap2Cbor.encodeGetAssertion(
                rpId = rpIdForKey,
                clientDataHash = clientDataHash,
                allowCredentials = options.allowCredentials,
                appId = appId,
                userVerification = options.userVerification,
            )

            val response = transport.getAssertion(request)
            val ctap = Ctap2Cbor.decodeGetAssertion(response, options.allowCredentials)

            Success(
                rawId = ctap.credentialId,
                authenticatorAttachment = "cross-platform",
                type = "public-key",
                id = Base64.getUrlEncoder().withoutPadding().encodeToString(ctap.credentialId),
                response = SuccessResponseData(
                    clientDataJSON = clientDataJson.toByteArray(),
                    authenticatorData = ctap.authenticatorData,
                    signature = ctap.signature,
                ),
            )
        } catch (e: Ctap2Error) {
            errorResult(e)
        } finally {
            transport.release()
        }
    }

    /**
     * Maps CTAP2 status codes (spec §6.3) to the domain [ErrorCode]s. Note these are
     * CTAP2 codes — the legacy CTAP1 codes (0x04, 0x05…) are not used by FIDO2 keys.
     */
    private fun errorResult(ex: Throwable): Result {
        val status = (ex as? Ctap2Error)?.ctapStatus
        val (code, message) = when (status) {
            0x2E -> ErrorCode.CONSTRAINT_ERR to "No matching credential on this security key"
            0x2F -> ErrorCode.TIMEOUT_ERR to "The security key timed out waiting for user presence"
            0x3A -> ErrorCode.TIMEOUT_ERR to "The security key timed out"
            0x27 -> ErrorCode.NOT_ALLOWED_ERR to "The security key did not authorize the operation"
            0x30 -> ErrorCode.NOT_ALLOWED_ERR to "The security key did not allow the operation"
            0x12 -> ErrorCode.ENCODING_ERR to "The security key rejected the request (malformed CBOR)"
            0x11 -> ErrorCode.ENCODING_ERR to "The security key rejected the request (unexpected CBOR)"
            0x2B -> ErrorCode.NOT_SUPPORTED_ERR to "The security key does not support a requested option"
            0x2C -> ErrorCode.NOT_SUPPORTED_ERR to "The security key does not accept a requested option"
            0x26 -> ErrorCode.NOT_SUPPORTED_ERR to "The security key does not support the requested algorithm"
            0x39 -> ErrorCode.NOT_SUPPORTED_ERR to "The request is too large for this security key"
            else -> ErrorCode.UNKNOWN_ERR to (ex.message ?: "Security key operation failed")
        }
        return if (status == 0x2E) {
            NoCredentialsResponse(ex)
        } else {
            Error(ErrorData(code, message))
        }
    }

    /**
     * Android facet/origin: android:apk-key-hash:<base64url-no-pad(SHA-256(leaf cert DER))>.
     * minSdk is 29, so the signing-certificate API is always available.
     */
    private fun facetId(): String {
        return try {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES,
            )
            val cs = info.signingInfo?.apkContentsSigners
                ?: info.signingInfo?.signingCertificateHistory
                ?: return ""
            val cert = cs.first().toByteArray()
            "android:apk-key-hash:" + base64UrlNoPad(MessageDigest.getInstance("SHA-256").digest(cert))
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * clientDataJSON for an assertion. Per W3C WebAuthn L2 (CollectedClientData):
     * type is "webauthn.get", challenge is base64url (no padding), origin is the
     * request origin (AppID when the appid extension is in use, else the facet),
     * crossOrigin is false for a top-level app window.
     */
    private fun buildClientDataJson(challenge: ByteArray, origin: String): String = buildString {
        append("{\"type\":\"webauthn.get\",")
        append("\"challenge\":\"").append(base64UrlNoPad(challenge)).append("\",")
        append("\"origin\":\"").append(origin).append("\",")
        append("\"crossOrigin\":false}")
    }

    private fun base64UrlNoPad(data: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(data)

    private fun bytesOfUByteArray(data: UByteArray): ByteArray =
        ByteArray(data.size) { data[it].toByte() }
}

/** Marker exception for transport-level failures of the native FIDO flow. */
class FidoNativeException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
