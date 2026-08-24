/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.proton.android.core.auth.fido.nfc

import com.upokecenter.cbor.CBOREncodeOptions
import com.upokecenter.cbor.CBORObject
import com.upokecenter.cbor.CBORType
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialDescriptor

/**
 * Minimal CBOR codec for the CTAP2 `authenticatorGetAssertion` (0x02) command and its
 * result, per CTAP2 spec §5.2 and the spec's normative examples.
 *
 * Request (map):
 *   1  rpId            (text, REQUIRED)  — the RP id the credentials are bound to.
 *                                         With the appid extension this is the FIDO
 *                                         AppID itself (L2 §10.1: the appid extension
 *                                         overrides the RP id, so the rpIdHash the key
 *                                         checks is the hash of the AppID).
 *   2  clientDataHash  (byte, REQUIRED)  — SHA-256 of the serialized clientDataJSON
 *   3  allowList       (array, optional) — [{ "id": <byte>, "type": "public-key" }]
 *   4  extensions      (map, optional)   — { "appid": <AppID> }
 *   5  options         (map, optional)   — { "up": true, "uv": true? }
 *
 * Response (map):
 *   1  credential            (map { "id": <byte>, "type": "public-key" }, OPTIONAL)
 *   2  authData              (byte, REQUIRED)
 *   3  signature             (byte, REQUIRED)
 *   4  user                  (map, optional)
 *   5  numberOfCredentials   (uint, optional)
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
                arr.Add(descriptorCbor(desc.type, bytesOfUByteArray(desc.id)))
            }
            map.set(3, arr)
        }
        if (!appId.isNullOrEmpty()) {
            val ext = CBORObject.NewMap()
            ext.set("appid", CBORObject.FromObject(appId))
            map.set(4, ext)
        }
        // CTAP2 options (spec §5.2, key 5). "up" (user presence) is required for
        // security keys; "uv" reflects the WebAuthn userVerification preference only
        // when the server asks for it — a plain security key has no gesture, so an
        // unneeded uv:true would be rejected (CTAP2_ERR_UNSUPPORTED_OPTION).
        val opts = CBORObject.NewMap()
        opts.set("up", CBORObject.FromObject(true))
        if (userVerification == "required") {
            opts.set("uv", CBORObject.FromObject(true))
        }
        map.set(5, opts)
        return map.EncodeToBytes(CBOREncodeOptions.DefaultCtap2Canonical)
    }

    /**
     * PublicKeyCredentialDescriptor as a CBOR map with TEXT keys, per the CTAP2 spec
     * examples and WebAuthn §5.8.3: { "id": <byte>, "type": "public-key" }.
     */
    private fun descriptorCbor(type: String, id: ByteArray): CBORObject = CBORObject.NewMap().apply {
        set("id", CBORObject.FromObject(id))
        set("type", CBORObject.FromObject(type))
    }

    private fun bytesOfUByteArray(data: UByteArray): ByteArray =
        ByteArray(data.size) { data[it].toByte() }

    data class GetAssertionResponse(
        val credentialId: ByteArray,
        val authenticatorData: ByteArray,
        val signature: ByteArray,
    )

    /**
     * Decodes the getAssertion result. Key 1 `credential` is a descriptor map
     * { "id": <byte>, "type": ... } (spec §5.2 / normative example), not a bare byte
     * string. It may be absent (e.g. device-resident credentials); the credential id
     * is also recoverable from [allowCredentials] when exactly one is allowed.
     */
    fun decodeGetAssertion(
        bytes: ByteArray,
        allowCredentials: List<Fido2PublicKeyCredentialDescriptor>?,
    ): GetAssertionResponse {
        val map = CBORObject.DecodeFromBytes(bytes)
        val credObj = map.get(1) as CBORObject?
        val credentialId: CBORObject? = credObj?.get("id")
            ?: allowCredentials?.singleOrNull()?.let { desc -> CBORObject.FromObject(bytesOfUByteArray(desc.id)) }
            ?: throw IllegalStateException("CTAP2 response missing credential (key 1)")
        return GetAssertionResponse(
            credentialId = asBytes(credentialId, "credential.id"),
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
}
