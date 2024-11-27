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

package ch.protonmail.android.composer.data.mapper

import ch.protonmail.android.composer.data.local.LocalDraft
import ch.protonmail.android.composer.data.wrapper.DraftWrapper
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.domain.model.Recipient

fun LocalDraft.toDraftFields() = DraftFields(
    sender = SenderEmail(this.sender),
    subject = Subject(this.subject),
    body = DraftBody(this.body),
    recipientsTo = RecipientsTo(this.recipientsTo.toRecipients()),
    recipientsCc = RecipientsCc(this.recipientsCc.toRecipients()),
    recipientsBcc = RecipientsBcc(this.recipientsBcc.toRecipients()),
    originalHtmlQuote = null
)

fun DraftWrapper.toLocalDraft() = LocalDraft(
    subject = this.subject(),
    sender = this.sender(),
    body = this.body(),
    recipientsTo = this.recipientsTo(),
    recipientsCc = this.recipientsCc(),
    recipientsBcc = this.recipientsBcc()
)

@MissingRustApi
// Hardcoded values in the mapping
private fun List<String>.toRecipients(): List<Recipient> = this.map {
    Recipient(
        address = it,
        name = "",
        isProton = false
    )
}
