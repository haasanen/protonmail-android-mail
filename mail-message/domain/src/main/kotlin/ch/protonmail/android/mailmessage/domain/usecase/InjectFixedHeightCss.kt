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

package ch.protonmail.android.mailmessage.domain.usecase

import javax.inject.Inject

class InjectFixedHeightCss @Inject constructor() {

    operator fun invoke(messageBody: String): String {
        val headClosingRegex = "</head>".toRegex(RegexOption.IGNORE_CASE)
        val headClosingMatch = headClosingRegex.find(messageBody)

        return if (headClosingMatch != null) {
            // Preserve the original case of the closing tag, we don't want <HEAD> and </head> for instance
            messageBody.replaceFirst(headClosingMatch.value, "$heightCssOverrideString${headClosingMatch.value}")
        } else {
            val headOpeningRegex = "<head>".toRegex(RegexOption.IGNORE_CASE)
            val headOpeningMatch = headOpeningRegex.find(messageBody)

            if (headOpeningMatch != null) {
                messageBody.replaceFirst(headOpeningMatch.value, "${headOpeningMatch.value}$heightCssOverrideString")
            } else {
                // Fallback: prepend to the entire body if no head tag exists
                "$heightCssOverrideString$messageBody"
            }
        }
    }

    private companion object {

        val heightCssOverrideString = """
            <style>
                html, body { height: auto !important; }
            </style>
        """.trimIndent()
    }
}
