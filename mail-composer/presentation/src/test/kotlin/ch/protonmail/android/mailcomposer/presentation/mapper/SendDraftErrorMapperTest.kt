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

import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.SendDraftError
import ch.protonmail.android.mailcomposer.presentation.R
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class SendDraftErrorMapperTest(
    @Suppress("unused") private val testName: String,
    private val error: SendDraftError,
    private val expected: TextUiModel
) {

    @Test
    fun `should map send draft error correctly`() {
        val actual = SendDraftErrorMapper.toTextUiModel(error)
        assertEquals(expected, actual)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "BadRequest returns Text with message",
                SendDraftError.BadRequest("Server error message"),
                TextUiModel.Text("Server error message")
            ),
            arrayOf(
                "AlreadySent returns already sent error",
                SendDraftError.AlreadySent,
                TextUiModel.TextRes(R.string.composer_error_send_draft_already_sent)
            ),
            arrayOf(
                "AttachmentsError returns attachments error",
                SendDraftError.AttachmentsError,
                TextUiModel.TextRes(R.string.composer_error_send_draft_attachments_error)
            ),
            arrayOf(
                "PackageError returns package error",
                SendDraftError.PackageError,
                TextUiModel.TextRes(R.string.composer_error_send_draft_package_error)
            ),
            arrayOf(
                "ExpirationTimeTooSoon returns expiration time error",
                SendDraftError.ExpirationTimeTooSoon,
                TextUiModel.TextRes(R.string.composer_error_send_draft_expiration_time_too_soon)
            ),
            arrayOf(
                "ExternalPasswordDecryptError returns password decrypt error",
                SendDraftError.ExternalPasswordDecryptError,
                TextUiModel.TextRes(R.string.composer_error_send_draft_external_password_decrypt_error)
            ),
            arrayOf(
                "InvalidRecipient returns invalid recipient error",
                SendDraftError.InvalidRecipient,
                TextUiModel.TextRes(R.string.composer_error_send_draft_invalid_recipient)
            ),
            arrayOf(
                "InvalidSenderAddress returns invalid sender error",
                SendDraftError.InvalidSenderAddress,
                TextUiModel.TextRes(R.string.composer_error_send_draft_invalid_sender)
            ),
            arrayOf(
                "MessageIsTooLarge returns message too large error",
                SendDraftError.MessageIsTooLarge,
                TextUiModel.TextRes(R.string.composer_error_send_draft_message_too_large)
            ),
            arrayOf(
                "ScheduleSendError returns schedule send error",
                SendDraftError.ScheduleSendError,
                TextUiModel.TextRes(R.string.composer_error_send_draft_schedule_send_error)
            ),
            arrayOf(
                "MessageNotExisting returns message does not exist error",
                SendDraftError.MessageNotExisting,
                TextUiModel.TextRes(R.string.composer_error_send_draft_message_does_not_exist)
            ),
            arrayOf(
                "Other returns generic error",
                SendDraftError.Other(DataError.Local.Unknown),
                TextUiModel.TextRes(R.string.composer_error_send_draft_generic)
            )
        )
    }
}
