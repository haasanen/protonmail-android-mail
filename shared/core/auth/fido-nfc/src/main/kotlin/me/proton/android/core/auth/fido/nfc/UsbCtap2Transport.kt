/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.proton.android.core.auth.fido.nfc

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbRequest
import android.util.Log
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom

/**
 * CTAP2 over USB HID transport ("CTAPHID"), per the FIDO Alliance CTAP v2.0 spec
 * (fidoalliance.org/specs/fido-v2.0-id-20180227), section 8.1 "USB Human Interface
 * Device (USB HID)".
 *
 * Wire layer (spec §8.1.4): fixed-size HID reports (64 bytes on full-speed devices).
 * Initialization packet:  CID(4) CMD(1, bit 7 set) BCNT(2, big-endian) DATA(...).
 * Continuation packet:    CID(4) SEQ(1, 0..0x7f, ascending) DATA(...).
 *
 * Handshake (spec §8.1.9.1.3, CTAPHID_INIT 0x06): INIT on the broadcast CID
 * (0xFFFFFFFF) allocates a channel. Request data is an 8-byte nonce; the response
 * (on the broadcast channel) carries the same nonce, then 4-byte channel ID,
 * protocol version, device version, capability flags. Commands are then exchanged
 * on the allocated channel.
 *
 * CTAP2 commands (spec §8.1.9.1.2, CTAPHID_CBOR 0x10): request data is the CTAP
 * command byte followed by the CBOR payload; response data is the CTAP status byte
 * followed by the CBOR payload. While the device works, it may interleave
 * keep-alive packets (CTAPHID_KEEPALIVE 0x3B: status 1 = processing, 2 = user
 * presence needed), which are consumed and skipped. A CTAPHID_ERROR (0x3F) response
 * carries a 1-byte CTAPHID error code (spec §8.1.9.1.6).
 *
 * Host side: standard Android USB host mode (UsbDeviceConnection.requestWait +
 * UsbRequest). Both the IN and OUT UsbRequests are always queued; requestWait
 * returns whichever completes first, so an in-flight response is never lost even
 * though a write request completes as soon as the report is handed to the bus.
 */
class UsbCtap2Transport {

    private var connection: UsbDeviceConnection? = null
    private var inRequest: UsbRequest? = null
    private var outRequest: UsbRequest? = null
    private var channel: Int = -1
    private var reportSize = REPORT_SIZE_DEFAULT
    private val inBuffer = ByteBuffer.allocateDirect(REPORT_SIZE_DEFAULT)
    private val outBuffer = ByteBuffer.allocateDirect(REPORT_SIZE_DEFAULT)

    /**
     * Binds the CTAPHID interface of [device]. Returns false when the device has no
     * HID interface with both an IN and an OUT endpoint (the CTAPHID pair,
     * spec §8.1.8.1) or the interface cannot be claimed.
     */
    fun bindDevice(device: UsbDevice, usb: UsbManager): Boolean {
        val interfaces = (0 until device.interfaceCount).map { device.getInterface(it) }
        val intf = interfaces.firstOrNull { it.id == HID_INTERFACE_ID }
            ?: interfaces.firstOrNull { it.interfaceClass == USB_CLASS_HID }
            ?: return false
        val endpoints = (0 until intf.endpointCount).map { intf.getEndpoint(it) }
        val out = endpoints.firstOrNull { it.direction == UsbConstants.USB_DIR_OUT } ?: return false
        val inp = endpoints.firstOrNull { it.direction == UsbConstants.USB_DIR_IN } ?: return false
        reportSize = inp.maxPacketSize.coerceAtLeast(MIN_REPORT_SIZE)
        return try {
            val conn = usb.openDevice(device) ?: return false
            if (!conn.claimInterface(intf, true)) return false
            connection = conn
            inRequest = UsbRequest().also { it.initialize(conn, inp) }
            outRequest = UsbRequest().also { it.initialize(conn, out) }
            true
        } catch (e: Exception) {
            Log.e(TAG, "bindDevice failed", e)
            release()
            false
        }
    }

    fun release() {
        try {
            inRequest?.cancel()
            outRequest?.cancel()
            inRequest?.close()
            outRequest?.close()
        } catch (e: Exception) {
            // best-effort
        }
        try {
            connection?.close()
        } catch (e: Exception) {
            // best-effort
        }
        inRequest = null
        outRequest = null
        connection = null
        channel = -1
    }

    /**
     * Allocates a CTAPHID channel via INIT on the broadcast CID. Returns true on
     * success; the response must echo the request nonce (guards against a stale
     * broadcast response from another client).
     */
    fun initialize(): Boolean {
        val nonce = ByteArray(NONCE_SIZE)
        SecureRandom().nextBytes(nonce)
        val packet = newOutPacket()
        packet.putInt(BROADCAST_CID)
        packet.put(CMD_INIT)
        putBcnt(packet, nonce.size)
        packet.put(nonce)

        val resp = transact(packet, CMD_INIT, BROADCAST_CID) ?: return false
        if (resp.size < INIT_RESPONSE_SIZE) return false
        if (!resp.copyOfRange(0, NONCE_SIZE).contentEquals(nonce)) return false
        channel = (resp[NONCE_SIZE].toInt() and 0xFF) shl 24 or
            (resp[NONCE_SIZE + 1].toInt() and 0xFF) shl 16 or
            (resp[NONCE_SIZE + 2].toInt() and 0xFF) shl 8 or
            (resp[NONCE_SIZE + 3].toInt() and 0xFF)
        val caps = resp[NONCE_SIZE + 8].toInt() and 0xFF
        if (caps and CAPABILITY_CBOR == 0) {
            Log.w(TAG, "device reports no CTAPHID_CBOR capability (caps=0x${caps.toString(16)})")
        }
        return true
    }

    /**
     * Runs authenticatorGetAssertion on the allocated channel. Returns the CBOR
     * response body when the CTAP status is 0x00; otherwise throws [Ctap2Error]
     * with the CTAP status code (same contract as [NfcCtap2Transport.getAssertion]).
     */
    fun getAssertion(cborPayload: ByteArray): ByteArray {
        if (channel < 0) throw Ctap2Error("USB channel not initialized")
        val data = byteArrayOf(CTAP_GET_ASSERTION) + cborPayload
        val resp = transactMessage(CMD_CBOR, data) ?: throw Ctap2Error("no USB response to getAssertion")
        if (resp.isEmpty()) throw Ctap2Error("empty USB response")
        val ctapStatus = resp[0].toInt()
        val body = resp.copyOfRange(1, resp.size)
        if (ctapStatus != 0x00) {
            throw Ctap2Error(
                "CTAP getAssertion failed, status 0x${ctapStatus.toString(16).uppercase()}",
                ctapStatus = ctapStatus,
            )
        }
        return body
    }

    /**
     * Sends one CTAPHID message (command [cmd], payload [data]; spans continuation
     * packets when the payload exceeds one report) and returns the full response
     * payload. Keep-alive packets are skipped; a CTAPHID_ERROR throws [Ctap2Error].
     */
    private fun transactMessage(cmd: Byte, data: ByteArray): ByteArray? {
        val total = data.size
        val perPacket = reportSize - 7
        var offset = 0
        var packetIndex = 0
        while (true) {
            val chunk = minOf(perPacket, total - offset)
            val packet = newOutPacket()
            packet.putInt(channel)
            if (packetIndex == 0) {
                packet.put(cmd)
                putBcnt(packet, total)
            } else {
                packet.put(packetIndex.toByte()) // SEQ, 0-based continuation index
            }
            if (chunk > 0) packet.put(data, offset, chunk)
            if (!sendOut(packet)) return null
            offset += chunk
            packetIndex++
            if (offset >= total) break
        }

        val body = ByteArrayOutputStream()
        while (true) {
            val resp = waitForIn() ?: return null
            val cmdByte = resp[4]
            val len = bcntOf(resp)
            when (cmdByte) {
                CMD_KEEPALIVE -> continue // processing / user-presence indicator
                CMD_ERROR -> {
                    val code = if (len >= 1) resp[5].toInt() and 0xFF else -1
                    throw Ctap2Error("USB CTAPHID error 0x${code.toString(16).uppercase()}")
                }
                else -> {
                    if (cidOf(resp) != channel) continue
                    if (len < 1) return null
                    val take = minOf(len, resp.remaining() - 5)
                    val bytes = ByteArray(take)
                    resp.position(5)
                    resp.get(bytes)
                    resp.position(0)
                    body.write(bytes)
                    return body.toByteArray()
                }
            }
        }
    }

    /**
     * Sends [packet] and returns the payload of the next non-keepalive response
     * matching [expectedCmd] on [expectedCid], or null on failure. Used for INIT.
     */
    private fun transact(packet: ByteBuffer, expectedCmd: Byte, expectedCid: Int): ByteArray? {
        if (!sendOut(packet)) return null
        while (true) {
            val resp = waitForIn() ?: return null
            val cmdByte = resp[4]
            val len = bcntOf(resp)
            when (cmdByte) {
                CMD_KEEPALIVE -> continue
                CMD_ERROR -> {
                    val code = if (len >= 1) resp[5].toInt() and 0xFF else -1
                    throw Ctap2Error("USB CTAPHID error 0x${code.toString(16).uppercase()}")
                }
                else -> {
                    if (cmdByte != expectedCmd || cidOf(resp) != expectedCid) continue
                    if (len < 1) return null
                    val take = minOf(len, resp.remaining() - 5)
                    return ByteArray(take) { resp.get(5 + it) }
                }
            }
        }
    }

    /** Queues the OUT request; the write completes once the report hits the bus. */
    private fun sendOut(packet: ByteBuffer): Boolean {
        val req = outRequest ?: return false
        val conn = connection ?: return false
        // Spec §8.1.4: unused bytes of the fixed-size report SHOULD be zero.
        while (packet.hasRemaining()) packet.put(0.toByte())
        packet.rewind()
        try {
            req.queue(packet, reportSize)
        } catch (e: Exception) {
            Log.e(TAG, "queue out failed", e)
            return false
        }
        while (true) {
            val got = waitRequest(WAIT_TIMEOUT_MS) ?: return false
            if (got === req) return true
            if (got === inRequest) inBuffer.rewind() // response already arrived; kept for waitForIn
        }
    }

    /**
     * Queues the IN request and waits until one full report is received. Returns
     * the buffer (rewound) or null on timeout/failure.
     */
    private fun waitForIn(): ByteBuffer? {
        val req = inRequest ?: return null
        val conn = connection ?: return null
        inBuffer.clear()
        inBuffer.limit(reportSize)
        try {
            req.queue(inBuffer, reportSize)
        } catch (e: Exception) {
            Log.e(TAG, "queue in failed", e)
            return null
        }
        while (true) {
            val got = waitRequest(PACKET_TIMEOUT_MS) ?: return null
            if (got === req) {
                inBuffer.rewind()
                return inBuffer
            }
            if (got === outRequest) continue // a write finished; the IN packet is still due
        }
    }

    private fun waitRequest(timeoutMs: Long): UsbRequest? {
        val conn = connection ?: return null
        return try {
            conn.requestWait(timeoutMs)
        } catch (e: Exception) {
            Log.e(TAG, "requestWait failed", e)
            null
        }
    }

    private fun newOutPacket(): ByteBuffer {
        outBuffer.clear()
        outBuffer.limit(reportSize)
        outBuffer.order(ByteOrder.BIG_ENDIAN)
        return outBuffer
    }

    private fun cidOf(p: ByteBuffer): Int {
        p.mark()
        val cid = p.getInt()
        p.reset()
        return cid
    }

    /** BCNT field (bytes 5..6, big-endian) of a CTAPHID packet. */
    private fun bcntOf(p: ByteBuffer): Int {
        p.mark()
        val saved = p.position()
        p.position(5)
        val hi = p.get().toInt() and 0xFF
        val lo = p.get().toInt() and 0xFF
        p.position(saved)
        return (hi shl 8) or lo
    }

    private fun putBcnt(p: ByteBuffer, len: Int) {
        p.put((len shr 8).toByte())
        p.put((len and 0xFF).toByte())
    }

    companion object {
        const val TAG = "FidoUsb"

        // CTAPHID commands (spec §8.1.9).
        private const val CMD_CBOR: Byte = 0x10.toByte()
        private const val CMD_INIT: Byte = 0x06
        private const val CMD_KEEPALIVE: Byte = 0x3B.toByte()
        private const val CMD_ERROR: Byte = 0x3F.toByte()

        private const val BROADCAST_CID = -1
        private const val CAPABILITY_CBOR = 0x04

        private const val USB_CLASS_HID = 0x03
        // YubiKey interface layout: 0 = keyboard (HID boot), 1 = CTAP HID.
        private const val HID_INTERFACE_ID = 1

        private const val REPORT_SIZE_DEFAULT = 64
        private const val MIN_REPORT_SIZE = 8

        private const val NONCE_SIZE = 8
        // INIT response (spec §8.1.9.1.3): nonce(8) cid(4) protVer(1) devVer(3) caps(1).
        private const val INIT_RESPONSE_SIZE = 17

        private const val WAIT_TIMEOUT_MS = 1000L
        private const val PACKET_TIMEOUT_MS = 30_000L
        // authenticatorGetAssertion CTAP command byte (spec §5.2).
        private const val CTAP_GET_ASSERTION: Byte = 0x02
    }
}
