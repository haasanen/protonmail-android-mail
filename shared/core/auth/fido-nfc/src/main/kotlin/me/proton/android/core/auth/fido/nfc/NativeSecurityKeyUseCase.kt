/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.proton.android.core.auth.fido.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Handler
import android.os.Looper
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
 * FIDO2 assertion flow (CTAP2) without GMS, over USB (CTAPHID) and NFC
 * (ISO 14443 / IsoDep) with the same code path after transport selection.
 *
 * Transport selection is automatic and concurrent: a plugged-in USB key and an
 * NFC tap are both watched at the same time, and whichever becomes available
 * first wins the challenge. A key plugged in after the button press is picked
 * up by the USB-attach listener (covering the OS enumeration delay), so the
 * user chooses no transport and needs no specific key order.
 *
 * Contract mirrors the platform (GMS) implementation:
 *  - [register] stores the result callback (activity calls it in onCreate).
 *  - [invoke] is called on the main thread from lifecycleScope. It either starts
 *    the USB flow, or arms NFC reader mode and the USB-attach watch (whichever
 *    key becomes available first wins), and returns [LaunchResult.Success]
 *    immediately ("dialog launched"); the actual [Result] arrives later through
 *    the callback, when the security key interaction completes.
 *  - The caller tears the flow down by finishing the activity; Android disables
 *    reader mode with the activity, and a USB permission receiver registered in
 *    the activity is released with it.
 */
@Singleton
@Suppress("TooGenericExceptionCaught")
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

    /** Receiver waiting for the USB permission result (USB path only). */
    @Volatile
    private var usbPermissionReceiver: BroadcastReceiver? = null

    /** Receiver waiting for a YubiKey to attach late while the flow is active. */
    @Volatile
    private var usbAttachReceiver: BroadcastReceiver? = null

    /** The activity the attach receiver was registered on, if any. */
    private var lateWatchActivity: Activity? = null

    /** Start of the late-attach window; bounds the polling safety net. */
    private var lateWatchStartMs = 0L

    /** The armed NFC reader; stopped when USB wins the race. */
    @Volatile
    private var nfcReader: NfcReader? = null

    /** Handles the bounded poll that watches for a late USB attach. */
    private val lateAttachHandler = Handler(Looper.getMainLooper())

    private val lateAttachPoll = Runnable { checkForLateUsbAttach() }

    /**
     * Bounds the wait for the USB permission dialog result. If the system
     * dialog never delivers a grant or denial (e.g. a broken dialog
     * implementation), the flow errors out with a retry hint instead of
     * hanging until the activity is finished.
     */
    private val grantTimeout = Runnable {
        if (usbPermissionReceiver != null) {
            Log.i(UsbCtap2Transport.TAG, "USB permission dialog did not complete in time")
            deliverError(FidoNativeException("The USB access dialog did not complete - please try again"))
        }
    }

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
        if (inProgress && activity === inProgressActivity) {
            return LaunchResult.Failure(
                FidoNativeException("A security key operation is already in progress"),
            )
        }

        currentOptions = publicKey
        inProgress = true
        inProgressActivity = activity

        // Fast path: a key is already plugged in and known — go straight to USB.
        val usbDevice = findUsbSecurityKey()
        if (usbDevice != null) {
            if (hasUsbPermission(usbDevice)) {
                startUsbFlow(usbDevice)
            } else {
                requestUsbPermission(activity, usbDevice)
            }
            return LaunchResult.Success
        }

        // No key yet: arm both transports at once (first-wins). NFC reader mode
        // covers a tap; the late-attach watch covers a key plugged in now or
        // appearing a few seconds later during OS enumeration.
        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null || !adapter.isEnabled) {
            // No NFC on this device (or it is off): wait for a USB key only.
            startLateUsbWatch(activity)
            return LaunchResult.Success
        }

        val reader = NfcReader { tag -> onTag(tag) }
        if (reader.start(activity)) {
            nfcReader = reader
        } else {
            Log.e(NfcCtap2Transport.TAG, "could not arm NFC reader mode; watching USB only")
        }

        startLateUsbWatch(activity)

        // Both transports armed (or the best available one): behave like the
        // GMS dialog launch. The result is delivered through the registered
        // callback when the key is tapped (NFC) or a USB key attaches.
        return LaunchResult.Success
    }

    private fun onTag(tag: Tag) {
        // NFC won the race: stop the USB-attach watch so a key plugged in
        // moments later cannot start a second flow while this one runs.
        stopLateUsbWatch()
        val options = currentOptions ?: return
        val callback = onResult ?: return
        resetFlowState()
        currentOptions = null
        // The CTAP2 exchange blocks on IsoDep transceives: off the main thread.
        Thread {
            val session = try {
                NfcSession(tag)
            } catch (e: Exception) {
                callback(errorResult(e), options)
                return@Thread
            }
            val result = runCatching { performAssertion(session, options) }
                .getOrElse { ex ->
                    Log.e(NfcCtap2Transport.TAG, "assertion flow failed", ex)
                    errorResult(ex)
                }
            session.close()
            callback(result, options)
        }.apply { isDaemon = true }.start()
    }

    /**
     * Starts the USB assertion flow on a worker thread: bind the HID interface,
     * allocate the CTAPHID channel, run the assertion. The result is delivered
     * through the registered callback.
     */
    private fun startUsbFlow(device: UsbDevice) {
        val options = currentOptions ?: return
        val callback = onResult ?: return
        resetFlowState()
        currentOptions = null
        Thread {
            val usb = usbManager()
            val transport = UsbCtap2Transport()
            val session = try {
                if (usb == null || !transport.bindDevice(device, usb)) {
                    throw FidoNativeException("Could not open the USB security key")
                }
                UsbSession(transport)
            } catch (e: Exception) {
                transport.release()
                callback(errorResult(e), options)
                return@Thread
            }
            val result = runCatching { performAssertion(session, options) }
                .getOrElse { ex ->
                    Log.e(UsbCtap2Transport.TAG, "assertion flow failed", ex)
                    errorResult(ex)
                }
            session.close()
            callback(result, options)
        }.apply { isDaemon = true }.start()
    }

    /**
     * Requests USB access through the system dialog. The receiver is registered
     * in the activity, so it is released with the activity; on grant the USB
     * flow starts, on denial an error is delivered through the callback.
     */
    private fun requestUsbPermission(activity: Activity, device: UsbDevice) {
        val usb = usbManager() ?: run {
            deliverError(FidoNativeException("USB is not available on this device"))
            return
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                unregisterUsbReceiver()
                if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    deliverError(FidoNativeException("USB access was not granted"))
                    return
                }
                startUsbFlow(device)
            }
        }
        usbPermissionReceiver = receiver
        // FLAG_UPDATE_CURRENT, never FLAG_NO_CREATE: with FLAG_NO_CREATE the
        // first call returns a null PendingIntent (nothing to reuse yet) and
        // the system dialog would be started without a response channel -
        // confirming it crashes the dialog and the grant is lost.
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val intent = Intent(USB_PERMISSION_ACTION).setPackage(context.packageName)
        val pending = PendingIntent.getBroadcast(context, 0, intent, flags)
        if (pending == null) {
            unregisterUsbReceiver()
            deliverError(FidoNativeException("Could not prepare the USB access request"))
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.registerReceiver(
                    receiver,
                    IntentFilter(USB_PERMISSION_ACTION),
                    Context.RECEIVER_NOT_EXPORTED,
                )
            } else {
                activity.registerReceiver(receiver, IntentFilter(USB_PERMISSION_ACTION))
            }
        } catch (e: Exception) {
            Log.e(UsbCtap2Transport.TAG, "registerReceiver failed", e)
        }
        try {
            usb.requestPermission(device, pending)
        } catch (e: Exception) {
            unregisterUsbReceiver()
            deliverError(FidoNativeException("Could not request USB access"))
        }
        lateAttachHandler.removeCallbacks(grantTimeout)
        lateAttachHandler.postDelayed(grantTimeout, USB_GRANT_TIMEOUT_MS)
    }

    private fun unregisterUsbReceiver() {
        lateAttachHandler.removeCallbacks(grantTimeout)
        usbPermissionReceiver?.let { receiver ->
            try {
                inProgressActivity?.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // receiver already released with the activity
            }
        }
        usbPermissionReceiver = null
    }

    /**
     * Arms the late-attach watch so a USB key that appears after the button
     * press can still win the flow. Two layers, both on the main thread:
     *  1. a runtime receiver for ACTION_USB_DEVICE_ATTACHED (primary, passive —
     *     no polling cost), and
     *  2. a bounded poll of deviceList as a safety net, because on some devices
     *     the OS takes several seconds to bring up a new device. The poll stops
     *     after [LATE_ATTACH_WATCH_WINDOW_MS]; the receiver stays until reset.
     */
    private fun startLateUsbWatch(activity: Activity) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (device == null || device.vendorId != USB_VENDOR_YUBICO) return
                onLateUsbAttach(device)
            }
        }
        usbAttachReceiver = receiver
        lateWatchActivity = activity
        lateWatchStartMs = System.currentTimeMillis()
        val filter = IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                activity.registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {
            Log.e(UsbCtap2Transport.TAG, "registerReceiver (USB attach) failed", e)
        }
        lateAttachHandler.removeCallbacks(lateAttachPoll)
        lateAttachHandler.postDelayed(lateAttachPoll, LATE_ATTACH_POLL_INTERVAL_MS)
    }

    /**
     * A YubiKey attached while the flow is active: USB wins the race. Release
     * the NFC arm and start the USB flow (requesting permission if needed).
     */
    private fun onLateUsbAttach(device: UsbDevice) {
        if (!inProgress) return
        val activity = inProgressActivity ?: return
        Log.i(UsbCtap2Transport.TAG, "USB key attached during flow; switching NFC -> USB")
        nfcReader?.stop(activity)
        nfcReader = null
        stopLateUsbWatch()
        if (hasUsbPermission(device)) {
            Log.i(
                UsbCtap2Transport.TAG,
                "USB access already granted from a previous allow; starting USB flow",
            )
            startUsbFlow(device)
        } else {
            requestUsbPermission(activity, device)
        }
    }

    /**
     * One tick of the bounded late-attach poll: if a YubiKey is present it wins
     * the race; otherwise reschedule until the watch window is over.
     */
    private fun checkForLateUsbAttach() {
        val device = findUsbSecurityKey()
        if (device != null) {
            onLateUsbAttach(device)
        } else if (System.currentTimeMillis() - lateWatchStartMs < LATE_ATTACH_WATCH_WINDOW_MS) {
            lateAttachHandler.postDelayed(lateAttachPoll, LATE_ATTACH_POLL_INTERVAL_MS)
        }
    }

    private fun stopLateUsbWatch() {
        lateAttachHandler.removeCallbacks(lateAttachPoll)
        usbAttachReceiver?.let { receiver ->
            val activity = lateWatchActivity
            if (activity != null) {
                try {
                    activity.unregisterReceiver(receiver)
                } catch (e: Exception) {
                    // receiver already released with the activity
                }
            }
        }
        usbAttachReceiver = null
        lateWatchActivity = null
    }

    private fun deliverError(ex: Exception) {
        val options = currentOptions
        val callback = onResult
        resetFlowState()
        currentOptions = null
        if (options != null && callback != null) callback(errorResult(ex), options)
    }

    private fun resetFlowState() {
        inProgress = false
        inProgressActivity = null
        unregisterUsbReceiver()
        stopLateUsbWatch()
    }

    /** Runs the shared CTAP2 assertion on any transport [session]. */
    private fun performAssertion(
        session: Ctap2Session,
        options: Fido2PublicKeyCredentialRequestOptions,
    ): Result {
        return try {
            if (!session.prepare()) {
                throw FidoNativeException("Could not reach the FIDO2 applet on the security key")
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

            val response = session.getAssertion(request)
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
     * Finds a plugged-in USB security key. Currently matches Yubico devices
     * (the key used by this fork); widen the filter to support other vendors.
     */
    private fun findUsbSecurityKey(): UsbDevice? {
        val usb = usbManager() ?: return null
        return usb.deviceList.values.firstOrNull { it.vendorId == USB_VENDOR_YUBICO }
    }

    private fun hasUsbPermission(device: UsbDevice): Boolean =
        usbManager()?.hasPermission(device) ?: false

    private fun usbManager(): UsbManager? =
        context.applicationContext.getSystemService(UsbManager::class.java)

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

    private inline fun withReset(block: () -> LaunchResult): LaunchResult {
        resetFlowState()
        currentOptions = null
        return block()
    }

    companion object {
        private const val USB_PERMISSION_ACTION = "me.proton.android.core.auth.fido.USB_PERMISSION"
        private const val USB_VENDOR_YUBICO = 0x1050
        private const val LATE_ATTACH_POLL_INTERVAL_MS = 500L
        private const val LATE_ATTACH_WATCH_WINDOW_MS = 60_000L
        private const val USB_GRANT_TIMEOUT_MS = 45_000L
    }
}

/**
 * A bound CTAP2 channel, transport-agnostic. [prepare] makes the FIDO2 applet
 * reachable (NFC: applet select; USB: CTAPHID channel allocation) and [getAssertion]
 * runs authenticatorGetAssertion, returning the CBOR response body (CTAP status
 * 0x00) or throwing [Ctap2Error] with the CTAP status code.
 */
internal interface Ctap2Session {
    fun prepare(): Boolean
    fun getAssertion(cbor: ByteArray): ByteArray
    fun close()
}

internal class NfcSession(tag: Tag) : Ctap2Session {
    private val transport = NfcCtap2Transport()

    init {
        if (!transport.bindTag(tag)) {
            throw FidoNativeException("The tag does not support contactless (IsoDep) exchange")
        }
    }

    override fun prepare(): Boolean = transport.selectFidoApplet()

    override fun getAssertion(cbor: ByteArray): ByteArray = transport.getAssertion(cbor)

    override fun close() {
        transport.release()
    }
}

internal class UsbSession(private val transport: UsbCtap2Transport) : Ctap2Session {
    override fun prepare(): Boolean {
        if (!transport.initialize()) {
            throw FidoNativeException("The USB security key did not respond (channel allocation failed)")
        }
        return true
    }

    override fun getAssertion(cbor: ByteArray): ByteArray = transport.getAssertion(cbor)

    override fun close() {
        transport.release()
    }
}

/** Marker exception for transport-level failures of the native FIDO flow. */
class FidoNativeException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
