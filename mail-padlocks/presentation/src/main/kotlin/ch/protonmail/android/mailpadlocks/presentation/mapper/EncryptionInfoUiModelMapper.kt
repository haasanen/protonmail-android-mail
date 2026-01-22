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

package ch.protonmail.android.mailpadlocks.presentation.mapper

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.mailpadlocks.domain.PrivacyLock
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockColor
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockIcon
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockTooltip
import ch.protonmail.android.mailpadlocks.presentation.R
import ch.protonmail.android.mailpadlocks.presentation.model.EncryptionInfoUiModel
import ch.protonmail.android.mailpadlocks.presentation.model.TooltipDescription
import kotlinx.collections.immutable.toPersistentList

object EncryptionInfoUiModelMapper {

    // Reusable addendum descriptions
    private object Addendum {

        val AlwaysE2ee = TooltipDescription(text = R.string.padlocks_proton_always_e2ee)

        val NonEncryptedCopy = TooltipDescription(text = R.string.padlocks_non_encrypted_copy)

        val SenderVerifiedContact = TooltipDescription(
            text = R.string.padlocks_sender_verified_contact,
            linkText = R.string.padlocks_sender_verified_contact_link_text,
            linkUrl = R.string.padlocks_verified_contact_link
        )

        val RecipientVerifiedContact = TooltipDescription(
            text = R.string.padlocks_recipient_verified_contact,
            linkText = R.string.padlocks_recipient_verified_contact_link_text,
            linkUrl = R.string.padlocks_verified_contact_link
        )

        val RecipientsVerifiedContacts = TooltipDescription(
            text = R.string.padlocks_recipients_verified_contacts,
            linkText = R.string.padlocks_recipients_verified_contacts_link_text,
            linkUrl = R.string.padlocks_verified_contact_link
        )

        val SenderVerificationFailed = TooltipDescription(
            text = R.string.padlocks_sender_verification_failed,
            linkText = R.string.padlocks_sender_verification_failed_link_text,
            linkUrl = R.string.padlocks_sender_verification_failed_link
        )

        val SenderVerificationFailedNoSignature = TooltipDescription(
            text = R.string.padlocks_sender_verification_failed_no_signature,
            linkText = R.string.padlocks_sender_verification_failed_no_signature_link_text,
            linkUrl = R.string.padlocks_sender_verification_failed_link
        )

        val ZeroAccess = TooltipDescription(
            text = R.string.padlocks_zero_access_addendum,
            linkText = R.string.padlocks_zero_access_addendum_link_text,
            linkUrl = R.string.padlocks_stored_with_zero_access_encryption_link
        )
    }

    fun fromPrivacyLock(privacyLock: PrivacyLock): EncryptionInfoUiModel = when (privacyLock) {
        is PrivacyLock.None -> EncryptionInfoUiModel.NoLock
        is PrivacyLock.Value -> {
            val icon = resolveIcon(privacyLock.icon)
            val color = resolveColor(privacyLock.color)
            val tooltipValues = resolveTooltip(privacyLock.tooltip)

            EncryptionInfoUiModel.WithLock(
                icon = icon,
                color = color,
                title = tooltipValues.title,
                descriptions = tooltipValues.descriptions.toPersistentList()
            )
        }
    }

    @DrawableRes
    private fun resolveIcon(privacyLockIcon: PrivacyLockIcon): Int {
        return when (privacyLockIcon) {
            PrivacyLockIcon.ClosedLock -> R.drawable.ic_lock_filled
            PrivacyLockIcon.ClosedLockWithTick -> R.drawable.ic_lock_check_filled
            PrivacyLockIcon.ClosedLockWithPen -> R.drawable.ic_lock_pen_filled
            PrivacyLockIcon.ClosedLockWarning -> R.drawable.ic_lock_exclamation_filled
            PrivacyLockIcon.OpenLockWithPen -> R.drawable.ic_lock_open_pen_filled
            PrivacyLockIcon.OpenLockWithTick -> R.drawable.ic_lock_open_check_filled
            PrivacyLockIcon.OpenLockWarning -> R.drawable.ic_lock_open_exclamation_filled
        }
    }

    @ColorRes
    private fun resolveColor(privacyLockColor: PrivacyLockColor): Int {
        return when (privacyLockColor) {
            PrivacyLockColor.Black -> R.color.padlock_black
            PrivacyLockColor.Green -> R.color.padlock_green
            PrivacyLockColor.Blue -> R.color.padlock_blue
        }
    }

    @Suppress("MethodTooLong", "LongMethod", "MaxLineLength")
    private fun resolveTooltip(privacyLockTooltip: PrivacyLockTooltip): TooltipValues {
        return when (privacyLockTooltip) {
            PrivacyLockTooltip.None -> throw UnsupportedOperationException()

            PrivacyLockTooltip.SendE2e -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_send_e2ee_description,
                    linkText = R.string.padlocks_proton_send_e2ee_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_send_e2ee,
                    descriptions = listOf(mainDescription, Addendum.AlwaysE2ee)
                )
            }

            PrivacyLockTooltip.SendE2eEo -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_send_e2ee_description,
                    linkText = R.string.padlocks_proton_send_e2ee_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_send_e2ee,
                    descriptions = listOf(mainDescription)
                )
            }

            PrivacyLockTooltip.SendE2eExternal -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_send_e2ee_description,
                    linkText = R.string.padlocks_proton_send_e2ee_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_send_e2ee,
                    descriptions = listOf(mainDescription)
                )
            }

            PrivacyLockTooltip.SendE2eVerifiedRecipient -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_send_e2ee_verified_recipient_description,
                    linkText = R.string.padlocks_proton_send_e2ee_verified_recipient_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_send_e2ee_verified_recipient,
                    descriptions = listOf(mainDescription, Addendum.RecipientVerifiedContact)
                )
            }

            PrivacyLockTooltip.SendSignOnly -> {
                val description = TooltipDescription(
                    text = R.string.padlocks_proton_send_sign_only_description,
                    linkText = R.string.padlocks_proton_send_sign_only_description_link_text,
                    linkUrl = R.string.padlocks_signed_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_send_sign_only,
                    descriptions = listOf(description)
                )
            }

            PrivacyLockTooltip.SendZeroAccessEncryptionDisabled -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_send_zero_access_encryption_disabled_description,
                    linkText = R.string.padlocks_proton_send_zero_access_encryption_disabled_description_link_text,
                    linkUrl = R.string.padlocks_stored_with_zero_access_encryption_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_send_zero_access_encryption_disabled,
                    descriptions = listOf(mainDescription, Addendum.ZeroAccess)
                )
            }

            PrivacyLockTooltip.ZeroAccess -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_zero_access_description,
                    linkText = R.string.padlocks_proton_zero_access_description_link_text,
                    linkUrl = R.string.padlocks_stored_with_zero_access_encryption_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_zero_access,
                    descriptions = listOf(mainDescription, Addendum.NonEncryptedCopy)
                )
            }

            PrivacyLockTooltip.ZeroAccessSentByProton -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_zero_access_sent_by_proton_description,
                    linkText = R.string.padlocks_proton_zero_access_sent_by_proton_description_link_text,
                    linkUrl = R.string.padlocks_stored_with_zero_access_encryption_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_zero_access_sent_by_proton,
                    descriptions = listOf(mainDescription, Addendum.NonEncryptedCopy)
                )
            }

            PrivacyLockTooltip.ReceiveE2e -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_receive_e2ee_description,
                    linkText = R.string.padlocks_proton_receive_e2ee_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_receive_e2ee,
                    descriptions = listOf(mainDescription, Addendum.AlwaysE2ee)
                )
            }

            PrivacyLockTooltip.ReceiveE2eExternal -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_receive_e2ee_description,
                    linkText = R.string.padlocks_proton_receive_e2ee_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_receive_e2ee,
                    descriptions = listOf(mainDescription)
                )
            }

            PrivacyLockTooltip.ReceiveE2eVerifiedRecipient -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_receive_e2ee_verified_recipient_description,
                    linkText = R.string.padlocks_proton_receive_e2ee_verified_recipient_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_receive_e2ee_verified_recipient,
                    descriptions = listOf(mainDescription, Addendum.SenderVerifiedContact)
                )
            }

            PrivacyLockTooltip.ReceiveE2eVerificationFailed -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_receive_e2ee_verification_failed_description,
                    linkText = R.string.padlocks_proton_receive_e2ee_verification_failed_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_receive_e2ee_verification_failed,
                    descriptions = listOf(mainDescription, Addendum.SenderVerificationFailed)
                )
            }

            PrivacyLockTooltip.ReceiveE2eVerificationFailedNoSignature -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_receive_e2ee_verification_failed_no_signature_description,
                    linkText = R.string.padlocks_proton_receive_e2ee_verification_failed_no_signature_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_receive_e2ee_verification_failed_no_signature,
                    descriptions = listOf(mainDescription, Addendum.SenderVerificationFailedNoSignature)
                )
            }

            PrivacyLockTooltip.ReceiveSignOnlyVerifiedRecipient -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_receive_sign_only_verification_failed_description,
                    linkText = R.string.padlocks_proton_receive_sign_only_verification_failed_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_receive_sign_only_verification_failed,
                    descriptions = listOf(mainDescription, Addendum.SenderVerifiedContact)
                )
            }

            PrivacyLockTooltip.ReceiveSignOnlyVerificationFailed -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_receive_sign_only_verified_recipient_description,
                    linkText = R.string.padlocks_proton_receive_sign_only_verified_recipient_description_link_text,
                    linkUrl = R.string.padlocks_stored_with_zero_access_encryption_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_receive_sign_only_verified_recipient,
                    descriptions = listOf(mainDescription, Addendum.SenderVerificationFailed)
                )
            }

            PrivacyLockTooltip.SentE2eVerifiedRecipients -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_sent_e2ee_description,
                    linkText = R.string.padlocks_proton_sent_e2ee_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_e2ee_verified_recipients,
                    descriptions = listOf(mainDescription, Addendum.RecipientsVerifiedContacts)
                )
            }

            PrivacyLockTooltip.SentProtonVerifiedRecipients -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_sent_proton_verified_recipients_description,
                    linkText = R.string.padlocks_proton_sent_proton_verified_recipients_description_link,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_proton_verified_recipients,
                    descriptions = listOf(mainDescription, Addendum.RecipientsVerifiedContacts)
                )
            }

            PrivacyLockTooltip.SentE2e -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_sent_e2ee_description,
                    linkText = R.string.padlocks_proton_sent_e2ee_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )


                TooltipValues(
                    title = R.string.padlocks_proton_sent_e2ee,
                    descriptions = listOf(mainDescription)
                )
            }

            PrivacyLockTooltip.SentRecipientE2eVerifiedRecipient -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_sent_recipient_e2ee_verified_recipient_description,
                    linkText = R.string.padlocks_proton_sent_recipient_e2ee_verified_recipient_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_recipient_e2ee_verified_recipient,
                    descriptions = listOf(mainDescription, Addendum.RecipientVerifiedContact)
                )
            }

            PrivacyLockTooltip.SentRecipientProtonMailVerifiedRecipient -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_sent_recipient_proton_mail_verified_recipient_description,
                    linkText = R.string.padlocks_proton_sent_recipient_proton_mail_verified_recipient_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_recipient_proton_mail_verified_recipient,
                    descriptions = listOf(mainDescription, Addendum.RecipientVerifiedContact)
                )
            }

            PrivacyLockTooltip.SentRecipientE2e -> {
                val description = TooltipDescription(
                    text = R.string.padlocks_proton_sent_recipient_e2ee_verified_recipient_description,
                    linkText = R.string.padlocks_proton_sent_recipient_e2ee_verified_recipient_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_recipient_e2ee,
                    descriptions = listOf(description)
                )
            }

            PrivacyLockTooltip.SentRecipientProtonMail -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_sent_recipient_proton_mail_recipient_description,
                    linkText = R.string.padlocks_proton_sent_recipient_proton_mail_recipient_description_link_text,
                    linkUrl = R.string.padlocks_stored_with_zero_access_encryption_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_recipient_proton_mail,
                    descriptions = listOf(mainDescription, Addendum.NonEncryptedCopy)
                )
            }

            PrivacyLockTooltip.SentRecipientE2ePgpVerifiedRecipient -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_sent_recipient_e2ee_pgp_verified_recipient_description,
                    linkText = R.string.padlocks_proton_sent_recipient_e2ee_pgp_verified_recipient_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_recipient_e2ee_pgp_verified_recipient,
                    descriptions = listOf(mainDescription, Addendum.RecipientVerifiedContact)
                )
            }

            PrivacyLockTooltip.SentRecipientProtonMailPgpVerifiedRecipient -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_sent_recipient_proton_pgp_verified_recipient_description,
                    linkText = R.string.padlocks_proton_sent_recipient_proton_pgp_verified_recipient_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_recipient_proton_pgp_verified_recipient,
                    descriptions = listOf(mainDescription, Addendum.RecipientVerifiedContact)
                )
            }

            PrivacyLockTooltip.SentRecipientE2ePgpRecipient -> {
                val description = TooltipDescription(
                    text = R.string.padlocks_proton_sent_recipient_e2ee_pgp_recipient_description,
                    linkText = R.string.padlocks_proton_sent_recipient_e2ee_pgp_recipient_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_recipient_e2ee_pgp_recipient,
                    descriptions = listOf(description)
                )
            }

            PrivacyLockTooltip.SentRecipientProtonMailPgpRecipient -> {
                val mainDescription = TooltipDescription(
                    text = R.string.padlocks_proton_sent_recipient_proton_mail_pgp_recipient_description,
                    linkText = R.string.padlocks_proton_sent_recipient_proton_mail_pgp_recipient_description_link_text,
                    linkUrl = R.string.padlocks_e2ee_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_recipient_proton_mail_pgp_recipient,
                    descriptions = listOf(mainDescription, Addendum.NonEncryptedCopy)
                )
            }

            PrivacyLockTooltip.SentRecipientPgpSigned -> {
                val description = TooltipDescription(
                    text = R.string.padlocks_proton_send_recipient_pgp_signed_description,
                    linkText = R.string.padlocks_proton_send_recipient_pgp_signed_description_link_text,
                    linkUrl = R.string.padlocks_signed_link
                )

                TooltipValues(
                    title = R.string.padlocks_proton_sent_recipient_pgp_signed,
                    descriptions = listOf(description)
                )
            }
        }
    }

    private data class TooltipValues(
        @StringRes val title: Int,
        val descriptions: List<TooltipDescription>
    )
}
