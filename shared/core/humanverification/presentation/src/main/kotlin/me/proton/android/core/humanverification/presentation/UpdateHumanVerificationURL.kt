/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.android.core.humanverification.presentation

import java.net.URLEncoder
import me.proton.android.core.humanverification.domain.HumanVerificationExternalInput
import me.proton.core.domain.entity.Product
import javax.inject.Inject

class UpdateHumanVerificationURL @Inject constructor(
    private val humanVerificationExternalInput: HumanVerificationExternalInput,
    private val isDarkModeEnabled: IsDarkModeEnabled,
    private val product: Product
) {

    operator fun invoke(
        url: String,
        defaultCountry: String?,
        recoveryPhone: String?,
        locale: String?
    ): String {
        val parameters = listOfNotNull(
            "theme" to if (isDarkModeEnabled()) "1" else "2",
            recoveryPhone?.let { "defaultPhone" to it },
            locale?.let { "locale" to it },
            defaultCountry?.let { "defaultCountry" to it },
            humanVerificationExternalInput.recoveryEmail?.let { "defaultEmail" to it },
            if (product == Product.Vpn) "vpn" to "true" else null
        ).joinToString(prefix = "&", separator = "&") { (key, value) ->
            "$key=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
        }.removeSuffix("&")
        return "$url$parameters"
    }
}
