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

package ch.protonmail.android.mailcomposer.presentation.model

/**
 * @displayBody is the body wrapped with an HTML template to allow injecting css and javascript; used to display only;
 * @body is used to expose back to the viewModel any changes applied by the user (no template, user-content only);
 */
data class ComposerFields(
    val sender: SenderUiModel,
    val displayBody: DraftDisplayBodyUiModel,
    val body: String
) {
    companion object {
        val initial = ComposerFields(
            sender = SenderUiModel(""),
            displayBody = DraftDisplayBodyUiModel(""),
            body = ""
        )
    }
}
