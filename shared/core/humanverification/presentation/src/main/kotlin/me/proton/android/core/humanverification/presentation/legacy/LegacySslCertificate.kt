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

import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import android.net.http.SslCertificate
import android.os.Build

/**
 * SPKI pins for alternative Proton API leaf certificates (base64, SHA-256).
 */
internal val ALTERNATIVE_API_SPKI_PINS = listOf(
    "EU6TS9MO0L/GsDHvVc9D5fChYLNy5JdGYpJw0ccgetM=",
    "iKPIHPnDNqdkvOnTClQ8zQAIKG0XavaPkcEo0LBAABA=", // gitleaks:allow
    "MSlVrBCdL0hKyczvgYVSRNm88RicyY04Q2y5qrBt0xA=", // gitleaks:allow
    "C2UxW0T1Ckl9s+8cXfjXxlEqwAfPM4HiW2y3UdtBeCw="
)

internal fun SslCertificate.isTrustedByLeafSPKIPinning(): Boolean =
    getCompatX509Cert()?.isTrustedByLeafSPKIPinning() ?: false

internal fun X509Certificate.isTrustedByLeafSPKIPinning(): Boolean =
    LeafSPKIPinningTrustManager(ALTERNATIVE_API_SPKI_PINS).runCatching {
        checkServerTrusted(arrayOf(this@isTrustedByLeafSPKIPinning), "generic")
    }.isSuccess

internal fun SslCertificate.getCompatX509Cert(): X509Certificate? = when (Build.VERSION.SDK_INT) {
    in Build.VERSION_CODES.Q..Int.MAX_VALUE -> x509Certificate
    else -> {
        // Hidden API, there is no way to access this value otherwise.
        SslCertificate.saveState(this).getByteArray("x509-certificate")?.runCatching {
            CertificateFactory.getInstance("X.509").generateCertificate(inputStream())
        }?.getOrNull() as? X509Certificate
    }
}
