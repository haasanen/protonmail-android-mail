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

package me.proton.android.core.auth.presentation.passmanagement

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal class PasswordManagementScreenPreviewProvider :
    PreviewParameterProvider<PasswordManagementState> {

    override val values: Sequence<PasswordManagementState> = Preview.entries.map { it.uiState }.asSequence()

    enum class Preview(val uiState: PasswordManagementState) {
        LOADING(
            uiState = PasswordManagementState.Loading
        ),
        LOGIN_PASSWORD(
            uiState = stubUserInput(PasswordManagementState.Tab.LOGIN)
        ),
        MAILBOX_PASSWORD(
            uiState = stubUserInput(PasswordManagementState.Tab.MAILBOX)
        ),
        TWO_PASSWORD(
            uiState = stubUserInput(PasswordManagementState.Tab.MAILBOX, isMailboxAvailable = true)
        )
    }
}

private fun stubUserInput(selectedTab: PasswordManagementState.Tab, isMailboxAvailable: Boolean = false) =
    PasswordManagementState.UserInput(
        selectedTab = selectedTab,
        loginPassword = LoginPasswordState(
            current = "current123",
            new = "newPassword123",
            confirmNew = "newPassword123",
            validationError = ValidationError.PasswordEmpty,
            isAvailable = true,
            currentPasswordNeeded = true
        ),
        mailboxPassword = MailboxPasswordState(
            isAvailable = isMailboxAvailable
        )
    )
