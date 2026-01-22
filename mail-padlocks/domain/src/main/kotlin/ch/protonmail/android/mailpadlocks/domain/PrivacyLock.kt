/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailpadlocks.domain

sealed interface PrivacyLock {

    data object None : PrivacyLock

    data class Value(
        val icon: PrivacyLockIcon,
        val color: PrivacyLockColor,
        val tooltip: PrivacyLockTooltip
    ) : PrivacyLock
}

enum class PrivacyLockIcon {
    ClosedLock,
    ClosedLockWithTick,
    ClosedLockWithPen,
    ClosedLockWarning,
    OpenLockWithPen,
    OpenLockWithTick,
    OpenLockWarning
}

enum class PrivacyLockColor {
    Black,
    Green,
    Blue
}

enum class PrivacyLockTooltip {
    None,
    SendE2e,
    SendE2eEo,
    SendE2eExternal,
    SendE2eVerifiedRecipient,
    SendSignOnly,
    SendZeroAccessEncryptionDisabled,
    ZeroAccess,
    ZeroAccessSentByProton,
    ReceiveE2e,
    ReceiveE2eExternal,
    ReceiveE2eVerifiedRecipient,
    ReceiveE2eVerificationFailed,
    ReceiveE2eVerificationFailedNoSignature,
    ReceiveSignOnlyVerifiedRecipient,
    ReceiveSignOnlyVerificationFailed,
    SentE2eVerifiedRecipients,
    SentProtonVerifiedRecipients,
    SentE2e,
    SentRecipientE2eVerifiedRecipient,
    SentRecipientProtonMailVerifiedRecipient,
    SentRecipientE2e,
    SentRecipientProtonMail,
    SentRecipientE2ePgpVerifiedRecipient,
    SentRecipientProtonMailPgpVerifiedRecipient,
    SentRecipientE2ePgpRecipient,
    SentRecipientProtonMailPgpRecipient,
    SentRecipientPgpSigned
}
