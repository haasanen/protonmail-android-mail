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
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HumanVerificationInput(
    val baseUrl: String,
    val path: String,
    val query: List<Pair<String, String>>?,
    val extraHeaders: List<Pair<String, String>>? = null,
    val verificationToken: String,
    val verificationMethods: List<String>,
    val defaultCountry: String? = null,
    val recoveryPhone: String? = null
) : Parcelable {

    fun buildUrl(recoveryEmail: String? = null): String {
        val defaultEmail = recoveryEmail?.let { "defaultEmail" to it }
        val parameters = listOfNotNull(
            "embed" to "true",
            "token" to verificationToken,
            "methods" to verificationMethods.joinToString(","),
            defaultEmail
        ).joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
        }
        val pathCleared = if (path.startsWith("/")) path else "/$path"
        return "$baseUrl$pathCleared?$parameters"
    }
}
