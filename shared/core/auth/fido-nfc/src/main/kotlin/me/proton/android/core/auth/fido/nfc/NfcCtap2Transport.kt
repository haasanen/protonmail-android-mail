/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.proton.android.core.auth.fido.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep

class Ctap2Error(
    message: String,
    val ctapStatus: Int = 0,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/**
 * CTAP2 over NFC (ISO 14443-4 / IsoDep) transport, per CTAP2 spec §11.3.
 *
 * Framing:
 *  - applet select: 00 A4 04 00 Lc AID. Success SW 9000.
 *  - message:       80 10 00 00 Lc <CBOR> Le=00.
 *  - response:      SW 9000 + data, where data = CTAP status byte (1) || CBOR.
 *                   91 00 = status update, keep polling with GETRESPONSE.
 *                   61 xx = xx trailing bytes, read with GETRESPONSE.
 *  - GETRESPONSE:   80 C0 00 00 (Le omitted; authenticator chooses max). 0xC0 per
 *                   the spec's normative YubiKey trace.
 */
class NfcCtap2Transport {

    private var isoDep: IsoDep? = null

    /** Binds [tag]. Returns false if the tag does not support IsoDep or connect fails. */
    fun bindTag(tag: Tag): Boolean {
        val dep = IsoDep.get(tag) ?: return false
        return try {
            dep.connect()
            isoDep = dep
            true
        } catch (e: Exception) {
            false
        }
    }

    fun release() {
        try {
            isoDep?.close()
        } catch (e: Exception) {
            // best-effort
        }
        isoDep = null
    }

    /**
     * FIDO2 applet select (spec §11.3.3). Returns true when the applet is present.
     */
    fun selectFidoApplet(): Boolean {
        val dep = isoDep ?: return false
        return try {
            val apdu = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, AID.size.toByte()) + AID
            val resp = transceive(dep, apdu)
            val sw = statusWord(resp)
            sw == 0x9000 || (sw shr 8) == 0x61
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Runs authenticatorGetAssertion. Returns the CBOR response body when the CTAP
     * status is 0x00; otherwise throws [Ctap2Error] with the CTAP status code.
     */
    fun getAssertion(cborPayload: ByteArray): ByteArray {
        val raw = sendCommand(cborPayload)
        if (raw.isEmpty()) throw Ctap2Error("empty NFC response")
        val ctapStatus = raw[0].toInt()
        val body = raw.copyOfRange(1, raw.size)
        if (ctapStatus != 0x00) {
            throw Ctap2Error(
                "CTAP getAssertion failed, status 0x${ctapStatus.toString(16).uppercase()}",
                ctapStatus = ctapStatus,
            )
        }
        return body
    }

    private fun sendCommand(payload: ByteArray): ByteArray {
        val dep = isoDep ?: throw Ctap2Error("NFC transport not bound")
        if (payload.size > MAX_SHORT_DATA) {
            throw Ctap2Error("getAssertion CBOR too large for short APDU: ${payload.size}")
        }
        // Single short APDU: 80 10 00 00 Lc <CBOR> Le=00
        val apdu = byteArrayOf(0x80.toByte(), 0x10, 0x00, 0x00, payload.size.toByte()) + payload + byteArrayOf(0x00)
        return unwrapResponse(transceive(dep, apdu), dep)
    }

    /**
     * Unwraps the ISO 7816 layer: strips status words, follows 9100 (status update)
     * and 61xx (trailing bytes) chains. Returns the application data, i.e.
     * CTAP status byte || CBOR.
     */
    private fun unwrapResponse(first: ByteArray, dep: IsoDep): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        var resp = first
        var sw = statusWord(resp)
        while (true) {
            when {
                sw == 0x9000 -> {
                    out.write(bodyOf(resp))
                    return out.toByteArray()
                }
                sw == 0x9100 -> {
                    // Status update — poll for data.
                    resp = getResponse(dep)
                    sw = statusWord(resp)
                }
                (sw and 0xFF00) == 0x6100 -> {
                    // xx trailing bytes available.
                    out.write(bodyOf(resp))
                    resp = getResponse(dep)
                    sw = statusWord(resp)
                }
                else -> throw Ctap2Error("NFC rejected, SW 0x${sw.toString(16).uppercase()}")
            }
        }
    }

    /** GETRESPONSE, INS 0xC0 per the spec's normative YubiKey trace (80 C0 00 00). */
    private fun getResponse(dep: IsoDep): ByteArray =
        transceive(dep, byteArrayOf(0x80.toByte(), INS_GETRESPONSE, 0x00, 0x00))

    private fun transceive(dep: IsoDep, apdu: ByteArray): ByteArray {
        dep.setTimeout(TIMEOUT_MS)
        return try {
            dep.transceive(apdu)
        } catch (e: Exception) {
            throw Ctap2Error("NFC transceive failed: ${e.message}", cause = e)
        }
    }

    private fun bodyOf(resp: ByteArray): ByteArray =
        if (resp.size <= 2) ByteArray(0) else resp.copyOfRange(0, resp.size - 2)

    private fun statusWord(resp: ByteArray): Int =
        if (resp.size < 2) -1
        else ((resp[resp.size - 2].toInt() and 0xFF) shl 8) or (resp[resp.size - 1].toInt() and 0xFF)

    companion object {
        private const val TIMEOUT_MS = 5000
        private const val MAX_SHORT_DATA = 255
        private const val INS_GETRESPONSE: Byte = 0xC0.toByte()
        // FIDO2 AID: RID A0 00 00 06 47, PIX 2F 00 01.
        private val AID = byteArrayOf(0xA0.toByte(), 0x00, 0x00, 0x06, 0x47.toByte(), 0x2F, 0x00, 0x01)
    }
}

/**
 * Holds the host activity in NFC reader mode for the duration of a key tap; every
 * IsoDep-capable tag is forwarded to [onTag]. The returned lambda stops the reader.
 */
class NfcReader(private val onTag: (Tag) -> Unit) {

    fun start(activity: Activity): Boolean {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return false
        if (!adapter.isEnabled) return false
        try {
            adapter.enableReaderMode(
                activity,
                { tag -> onTag(tag) },
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                null,
            )
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun stop(activity: Activity) {
        val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
        try {
            adapter.disableReaderMode(activity)
        } catch (e: Exception) {
            // best-effort
        }
    }
}
