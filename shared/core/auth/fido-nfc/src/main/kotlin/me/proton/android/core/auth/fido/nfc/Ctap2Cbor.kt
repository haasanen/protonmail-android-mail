/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.proton.android.core.auth.fido.nfc

import com.upokecenter.cbor.CBORObject
import com.upokecenter.cbor.CBORType
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialDescriptor
import java.security.MessageDigest
import java.util.Base64

/**
 * Minimal CBOR codec for the CTAP2 `authenticatorGetAssertion` (0x10) command
 * and its result, per CTAP2 spec §5.2.
 *
 * Request (map):
 *   1  rpId            (text, REQUIRED)  — RP id, or the FIDO AppID when the
 *                                         appid extension is present
 *   2  clientDataHash  (byte, REQUIRED)  — SHA-256 of the serialized clientDataJSON
 *   3  allowList       (array, optional) — [{1 type, 2 id}]
 *   4  extensions      (map, optional)   — {"appid": <AppID>}
 *   5  options         (map, optional)   — {"up": true}
 *
 * Response (map):
 *   1  credentialId    (byte, REQUIRED)
 *   2  authData        (byte, REQUIRED)
 *   3  signature       (byte, REQUIRED)
 *   4  user            (map, optional)
 *   5  numberOfCredentials (uint, optional)
 */
internal object Ctap2Cbor {

    fun encodeGetAssertion(
        rpId: String,
        clientDataHash: ByteArray,
        allowCredentials: List<Fido2PublicKeyCredentialDescriptor>?,
        appId: String?,
        userVerification: String?,
    ): ByteArray {
        val map = CBORObject.NewMap()
        map.set(1, CBORObject.FromObject(rpId))
        map.set(2, CBORObject.FromObject(clientDataHash))
        if (!allowCredentials.isNullOrEmpty()) {
            val arr = CBORObject.NewArray()
            for (desc in allowCredentials) {
                val c = CBORObject.NewMap()
                c.set(1, CBORObject.FromObject(desc.type))
                c.set(2, CBORObject.FromObject(bytesOfUByteArray(desc.id)))
                arr.Add(c)
            }
            map.set(3, arr)
        }
        if (!appId.isNullOrEmpty()) {
            val ext = CBORObject.NewMap()
            ext.set("appid", CBORObject.FromObject(appId))
            map.set(4, ext)
        }
        // CTAP2 options (spec §5.2, key 5). "up" (user presence) is required for
        // security keys; "uv" reflects the WebAuthn userVerification preference
        // only when the server asks for it.
        val opts = CBORObject.NewMap()
        opts.set("up", CBORObject.FromObject(true))
        if (userVerification == "required") {
            opts.set("uv", CBORObject.FromObject(true))
        }
        map.set(5, opts)
        return map.EncodeToBytes()
    }

    private fun bytesOfUByteArray(data: UByteArray): ByteArray =
        ByteArray(data.size) { data[it].toByte() }

    data class GetAssertionResponse(
        val credentialId: ByteArray,
        val authenticatorData: ByteArray,
        val signature: ByteArray,
    )

    fun decodeGetAssertion(bytes: ByteArray): GetAssertionResponse {
        val map = CBORObject.DecodeFromBytes(bytes)
        return GetAssertionResponse(
            credentialId = asBytes(map.get(1), "credentialId"),
            authenticatorData = asBytes(map.get(2), "authData"),
            signature = asBytes(map.get(3), "signature"),
        )
    }

    private fun asBytes(item: CBORObject?, field: String): ByteArray {
        if (item == null) throw IllegalStateException("CTAP2 response missing key: $field")
        return when (item.getType()) {
            CBORType.ByteString -> item.GetByteString()
            CBORType.TextString -> item.AsString().toByteArray()
            else -> throw IllegalStateException("CTAP2 response field $field is not a byte string")
        }
    }

    fun b64urlEncode(data: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(data)

    fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)
}
