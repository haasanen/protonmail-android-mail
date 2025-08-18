/*
 * Copyright (c) 2025 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.humanverification.presentation.legacy

import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import android.annotation.SuppressLint
import android.util.Base64
import okhttp3.OkHttpClient
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Inits given okhttp builder with leaf SPKI pinning. Accepts certificate chain iff leaf certificate
 * SPKI matches one of the [pins].
 *
 * @param builder builder to introduce pinning to.
 * @param pins list of pins (base64, SHA-256). When empty, pinning will be disabled and default
 *   certificate verification will be used (should be used only for testing).
 */
internal fun initSPKIleafPinning(builder: OkHttpClient.Builder, pins: List<String>) {
    if (pins.isNotEmpty()) {
        val trustManager = LeafSPKIPinningTrustManager(pins)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        builder.sslSocketFactory(sslContext.socketFactory, trustManager)
        builder.hostnameVerifier { _, _ ->
            // Verification is based solely on SPKI pinning of leaf certificate
            true
        }
    }
}

@SuppressLint("CustomX509TrustManager")
internal class LeafSPKIPinningTrustManager(pinnedSPKIHashes: List<String>) : X509TrustManager {

    private val pins: List<PublicKeyPin> = pinnedSPKIHashes.map { PublicKeyPin.fromSha256HashBase64(it) }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        if (PublicKeyPin.fromCertificate(chain.first()) !in pins)
            throw CertificateException("Pin verification failed")
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
        throw CertificateException("Client certificates not supported!")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOfNulls(0)
}

/**
 * SHA-256 hash of the certificate's Subject Public Key Info,
 * as described in the HPKP RFC https://tools.ietf.org/html/rfc7469s.
 */
data class PublicKeyPin(
    private val sha256Hash: ByteArray
) {

    override fun equals(other: Any?): Boolean =
        this === other || other is PublicKeyPin && sha256Hash.contentEquals(other.sha256Hash)

    override fun hashCode(): Int = sha256Hash.contentHashCode()

    companion object {
        private const val PIN_LENGTH = 32

        fun fromCertificate(certificate: Certificate): PublicKeyPin {
            val digest = MessageDigest.getInstance("SHA-256").apply { reset() }
            val sha256Hash = digest.digest(certificate.publicKey.encoded)
            return PublicKeyPin(sha256Hash)
        }

        fun fromSha256HashBase64(sha256HashBase64: String): PublicKeyPin {
            val sha256Hash = Base64.decode(sha256HashBase64, Base64.DEFAULT)
            require(sha256Hash.size == PIN_LENGTH) { "Invalid pin: length is not $PIN_LENGTH bytes" }
            return PublicKeyPin(sha256Hash)
        }
    }
}
