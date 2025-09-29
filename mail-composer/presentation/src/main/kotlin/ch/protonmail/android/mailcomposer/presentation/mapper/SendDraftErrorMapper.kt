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

package ch.protonmail.android.mailcomposer.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.SendDraftError
import ch.protonmail.android.mailcomposer.presentation.R

internal object SendDraftErrorMapper {

    fun toTextUiModel(sendDraftError: SendDraftError): TextUiModel {

        val textRes = when (sendDraftError) {
            SendDraftError.AlreadySent -> R.string.composer_error_send_draft_already_sent
            SendDraftError.AttachmentsError -> R.string.composer_error_send_draft_attachments_error
            SendDraftError.PackageError -> R.string.composer_error_send_draft_package_error
            SendDraftError.ExpirationTimeTooSoon -> R.string.composer_error_send_draft_expiration_time_too_soon
            SendDraftError.ExternalPasswordDecryptError ->
                R.string.composer_error_send_draft_external_password_decrypt_error

            SendDraftError.InvalidRecipient -> R.string.composer_error_send_draft_invalid_recipient
            SendDraftError.InvalidSenderAddress -> R.string.composer_error_send_draft_invalid_sender
            SendDraftError.MessageIsTooLarge -> R.string.composer_error_send_draft_message_too_large
            is SendDraftError.Other -> R.string.composer_error_send_draft_generic
            SendDraftError.ScheduleSendError -> R.string.composer_error_send_draft_schedule_send_error
            SendDraftError.MessageNotExisting -> R.string.composer_error_send_draft_message_does_not_exist
        }

        return TextUiModel.TextRes(textRes)
    }
}
