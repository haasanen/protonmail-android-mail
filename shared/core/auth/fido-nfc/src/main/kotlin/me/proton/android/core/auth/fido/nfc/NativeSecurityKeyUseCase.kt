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
import android.util.Log
import androidx.activity.result.ActivityResultCaller
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
) : PerformTwoFaWithSecurityKey<ActivityResultCaller, Activity> {

    @Volatile
    private var onResult: ((Result, Fido2PublicKeyCredentialRequestOptions) -> Unit)? = null

    @Volatile
    private var currentOptions: Fido2PublicKeyCredentialRequestOptions? = null

    @Volatile
    private var inProgress = false

    /** The activity that started the current flow; the guard is scoped to it. */
    @Volatile
    private var inProgressActivity: Activity? = null

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
        if (inProgress && activity === inProgressActivity) {
            return LaunchResult.Failure(
                FidoNativeException("A security key operation is already in progress"),
            )
        }

        currentOptions = publicKey
        inProgress = true
        inProgressActivity = activity

        val started = NfcReader { tag -> onTag(tag) }.start(activity)
        if (!started) {
            inProgress = false
            inProgressActivity = null
            return LaunchResult.Failure(
                FidoNativeException("Could not start the NFC security key reader"),
            )
        }

        // Reader mode is armed: behave like the GMS dialog launch. The result
        // is delivered through the registered callback when the key is tapped.
        return LaunchResult.Success
    }

    private fun onTag(tag: Tag) {
        Log.d(NfcCtap2Transport.TAG, "tag received: ${tag.techList?.joinToString { it.javaClass.simpleName }}")
        val options = currentOptions ?: return
        val callback = onResult ?: return
        inProgress = false
        inProgressActivity = null
        currentOptions = null
        // The CTAP2 exchange blocks on IsoDep transceives: off the main thread.
        Thread {
            val result = runCatching { performAssertion(tag, options) }
                .getOrElse { ex ->
                    Log.e(NfcCtap2Transport.TAG, "assertion flow failed", ex)
                    errorResult(ex)
                }
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
            Log.d(NfcCtap2Transport.TAG, "applet selected, building getAssertion")

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
            // Origin: AppID > official Proton origin (for the Proton RP only) > this app's
            // own facet. The Proton server expects the official app's facet; this fork is
            // re-signed, so its own facet is rejected. See OFFICIAL_PROTON_CERT_SHA256_HEX.
            val origin = appId ?: if (isProtonRpid(rpId)) officialProtonOrigin() else facetId()
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
            Log.d(
                NfcCtap2Transport.TAG,
                "assertion body len=${response.size} hex=[${
                    response.joinToString(" ") { (it.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0') }
                }]"
            )
            val ctap = Ctap2Cbor.decodeGetAssertion(response, options.allowCredentials)
            Log.d(
                NfcCtap2Transport.TAG,
                "decoded: cred=${ctap.credentialId.size} authData=${ctap.authenticatorData.size} sig=${ctap.signature.size}"
            )

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
     * The Proton RP id as the server reports it. The official-origin override is
     * scoped to this RP only so we never present Proton's facet to another relying
     * party.
     */
    private fun isProtonRpid(rpId: String): Boolean =
        rpId.equals("proton.me", ignoreCase = true) ||
            rpId.endsWith(".proton.me", ignoreCase = true)

    /**
     * The SHA-256 of the official Proton Mail Android signing certificate.
     *
     * Verified against the official APK (ProtonMail-7.10.4_17667.apk) downloaded from
     * Proton's own release channel (github.com/ProtonMail/android-mail/releases, the
     * source linked from proton.me/mail/download):
     *   - apksigner verify --print-certs: v3 scheme, cert SHA-256 matches below
     *   - independent re-hash of the X.509 DER cert extracted from the APK v3
     *     signing block: same digest
     * The cert is RSA-2048, CN=Proton Technologies AG (Geneva, CH).
     */
    private val OFFICIAL_PROTON_CERT_SHA256_HEX =
        "dcc9439ec1a6c6a8d0203f3423ee42bcc8b970628e53cb73a0393f398dd5b853"

    /**
     * The official app's Android WebAuthn origin (facet):
     * android:apk-key-hash:<base64url-no-pad(SHA-256(official cert DER))>.
     * Derived from [OFFICIAL_PROTON_CERT_SHA256_HEX], not a magic string.
     */
    private fun officialProtonOrigin(): String {
        val hex = OFFICIAL_PROTON_CERT_SHA256_HEX
        val digest = ByteArray(hex.length / 2) { i ->
            val hi = hex[i * 2].digitToInt(16)
            val lo = hex[i * 2 + 1].digitToInt(16)
            ((hi shl 4) or lo).toByte()
        }
        return "android:apk-key-hash:" + base64UrlNoPad(digest)
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
