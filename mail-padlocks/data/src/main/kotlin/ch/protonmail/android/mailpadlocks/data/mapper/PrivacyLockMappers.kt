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

package ch.protonmail.android.mailpadlocks.data.mapper

import ch.protonmail.android.mailcommon.data.mapper.LocalPrivacyLock
import ch.protonmail.android.mailcommon.data.mapper.LocalPrivacyLockColor
import ch.protonmail.android.mailcommon.data.mapper.LocalPrivacyLockIcon
import ch.protonmail.android.mailcommon.data.mapper.LocalPrivacyLockTooltip
import ch.protonmail.android.mailpadlocks.domain.PrivacyLock
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockColor
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockIcon
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockTooltip

fun LocalPrivacyLock.toPrivacyLock(): PrivacyLock.Value = PrivacyLock.Value(
    icon = icon.toPrivacyLockIcon(),
    color = color.toPrivacyLockColor(),
    tooltip = tooltip.toPrivacyLockTooltip()
)

fun LocalPrivacyLockColor.toPrivacyLockColor() = when (this) {
    LocalPrivacyLockColor.BLACK -> PrivacyLockColor.Black
    LocalPrivacyLockColor.GREEN -> PrivacyLockColor.Green
    LocalPrivacyLockColor.BLUE -> PrivacyLockColor.Blue
}

fun LocalPrivacyLockIcon.toPrivacyLockIcon() = when (this) {
    LocalPrivacyLockIcon.CLOSED_LOCK -> PrivacyLockIcon.ClosedLock
    LocalPrivacyLockIcon.CLOSED_LOCK_WITH_TICK -> PrivacyLockIcon.ClosedLockWithTick
    LocalPrivacyLockIcon.CLOSED_LOCK_WITH_PEN -> PrivacyLockIcon.ClosedLockWithPen
    LocalPrivacyLockIcon.CLOSED_LOCK_WARNING -> PrivacyLockIcon.ClosedLockWarning
    LocalPrivacyLockIcon.OPEN_LOCK_WITH_PEN -> PrivacyLockIcon.OpenLockWithPen
    LocalPrivacyLockIcon.OPEN_LOCK_WITH_TICK -> PrivacyLockIcon.OpenLockWithTick
    LocalPrivacyLockIcon.OPEN_LOCK_WARNING -> PrivacyLockIcon.OpenLockWarning
}

fun LocalPrivacyLockTooltip.toPrivacyLockTooltip() = when (this) {
    LocalPrivacyLockTooltip.NONE -> PrivacyLockTooltip.None
    LocalPrivacyLockTooltip.SEND_E2E -> PrivacyLockTooltip.SendE2e
    LocalPrivacyLockTooltip.SEND_E2E_VERIFIED_RECIPIENT -> PrivacyLockTooltip.SendE2eVerifiedRecipient
    LocalPrivacyLockTooltip.SEND_SIGN_ONLY -> PrivacyLockTooltip.SendSignOnly
    LocalPrivacyLockTooltip.SEND_ZERO_ACCESS_ENCRYPTION_DISABLED -> PrivacyLockTooltip.SendZeroAccessEncryptionDisabled
    LocalPrivacyLockTooltip.ZERO_ACCESS -> PrivacyLockTooltip.ZeroAccess
    LocalPrivacyLockTooltip.ZERO_ACCESS_SENT_BY_PROTON -> PrivacyLockTooltip.ZeroAccessSentByProton
    LocalPrivacyLockTooltip.RECEIVE_E2E -> PrivacyLockTooltip.ReceiveE2e
    LocalPrivacyLockTooltip.RECEIVE_E2E_VERIFIED_RECIPIENT -> PrivacyLockTooltip.ReceiveE2eVerifiedRecipient
    LocalPrivacyLockTooltip.RECEIVE_E2E_VERIFICATION_FAILED -> PrivacyLockTooltip.ReceiveE2eVerificationFailed
    LocalPrivacyLockTooltip.RECEIVE_E2E_VERIFICATION_FAILED_NO_SIGNATURE ->
        PrivacyLockTooltip.ReceiveE2eVerificationFailedNoSignature

    LocalPrivacyLockTooltip.RECEIVE_SIGN_ONLY_VERIFIED_RECIPIENT -> PrivacyLockTooltip.ReceiveSignOnlyVerifiedRecipient
    LocalPrivacyLockTooltip.RECEIVE_SIGN_ONLY_VERIFICATION_FAILED ->
        PrivacyLockTooltip.ReceiveSignOnlyVerificationFailed

    LocalPrivacyLockTooltip.SENT_E2E_VERIFIED_RECIPIENTS -> PrivacyLockTooltip.SentE2eVerifiedRecipients
    LocalPrivacyLockTooltip.SENT_PROTON_VERIFIED_RECIPIENTS -> PrivacyLockTooltip.SentProtonVerifiedRecipients
    LocalPrivacyLockTooltip.SENT_E2E -> PrivacyLockTooltip.SentE2e
    LocalPrivacyLockTooltip.SENT_RECIPIENT_E2E_VERIFIED_RECIPIENT ->
        PrivacyLockTooltip.SentRecipientE2eVerifiedRecipient

    LocalPrivacyLockTooltip.SENT_RECIPIENT_PROTON_MAIL_VERIFIED_RECIPIENT ->
        PrivacyLockTooltip.SentRecipientProtonMailVerifiedRecipient

    LocalPrivacyLockTooltip.SENT_RECIPIENT_E2E -> PrivacyLockTooltip.SentRecipientE2e
    LocalPrivacyLockTooltip.SENT_RECIPIENT_PROTON_MAIL -> PrivacyLockTooltip.SentRecipientProtonMail
    LocalPrivacyLockTooltip.SENT_RECIPIENT_E2E_PGP_VERIFIED_RECIPIENT ->
        PrivacyLockTooltip.SentRecipientE2ePgpVerifiedRecipient

    LocalPrivacyLockTooltip.SENT_RECIPIENT_PROTON_MAIL_PGP_VERIFIED_RECIPIENT ->
        PrivacyLockTooltip.SentRecipientProtonMailPgpVerifiedRecipient

    LocalPrivacyLockTooltip.SENT_RECIPIENT_E2E_PGP_RECIPIENT ->
        PrivacyLockTooltip.SentRecipientE2ePgpRecipient

    LocalPrivacyLockTooltip.SENT_RECIPIENT_PROTON_MAIL_PGP_RECIPIENT ->
        PrivacyLockTooltip.SentRecipientProtonMailPgpRecipient

    LocalPrivacyLockTooltip.SENT_RECIPIENT_PGP_SIGNED -> PrivacyLockTooltip.SentRecipientPgpSigned
}
