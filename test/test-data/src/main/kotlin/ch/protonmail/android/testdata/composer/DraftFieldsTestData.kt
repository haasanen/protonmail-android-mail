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

package ch.protonmail.android.testdata.composer

import ch.protonmail.android.mailcommon.domain.sample.UserAddressSample
import ch.protonmail.android.mailcomposer.domain.model.BodyFields
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.DraftHead
import ch.protonmail.android.mailcomposer.domain.model.DraftMimeType
import ch.protonmail.android.mailcomposer.domain.model.DraftRecipient
import ch.protonmail.android.mailcomposer.domain.model.DraftRecipientValidity
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailmessage.domain.sample.RecipientSample

object DraftFieldsTestData {

    val BasicDraftFields = buildExpectedDraftFields()
    val EmptyDraftWithPrimarySender = buildExpectedDraftFields(
        expectedSubject = "",
        expectedSenderEmail = UserAddressSample.PrimaryAddress.email,
        expectedDraftBody = "",
        recipientsToAddresses = emptyList(),
        recipientsCcAddresses = emptyList(),
        recipientsBccAddresses = emptyList()
    )

    private fun buildExpectedDraftFields(
        expectedSubject: String = "Subject for the message",
        expectedSenderEmail: String = UserAddressSample.PrimaryAddress.email,
        expectedDraftBody: String = "I am plaintext",
        expectedDraftHead: String = "",
        expectedMimeType: DraftMimeType = DraftMimeType.Html,
        recipientsToAddresses: List<String> = listOf(RecipientSample.NamelessRecipient.address),
        recipientsCcAddresses: List<String> = listOf(RecipientSample.NamelessRecipient.address),
        recipientsBccAddresses: List<String> = listOf(RecipientSample.NamelessRecipient.address)
    ) = DraftFields(
        SenderEmail(expectedSenderEmail),
        Subject(expectedSubject),
        BodyFields(
            DraftHead(expectedDraftHead),
            DraftBody(expectedDraftBody)
        ),
        expectedMimeType,
        RecipientsTo(recipientsToAddresses.toDraftRecipient()),
        RecipientsCc(recipientsCcAddresses.toDraftRecipient()),
        RecipientsBcc(recipientsBccAddresses.toDraftRecipient())
    )

    private fun List<String>.toDraftRecipient() = this.map {
        DraftRecipient.SingleRecipient(
            name = "",
            address = it,
            validity = DraftRecipientValidity.Validating
        )
    }
}
