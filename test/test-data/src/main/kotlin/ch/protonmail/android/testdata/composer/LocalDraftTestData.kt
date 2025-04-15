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

import ch.protonmail.android.composer.data.local.LocalDraft
import ch.protonmail.android.mailcommon.domain.sample.UserAddressSample
import ch.protonmail.android.mailmessage.domain.sample.RecipientSample

object LocalDraftTestData {

    val BasicLocalDraft = buildExpectedLocalDraftFields(
        expectedSenderEmail = UserAddressSample.PrimaryAddress.email
    )
    val JobApplicationDraft = buildExpectedLocalDraftFields(
        expectedSubject = "Application for some role",
        expectedSenderEmail = "anyone@proton.me",
        expectedDraftBody = "Hello, hire me",
        recipientsTo = emptyList(),
        recipientsCc = emptyList(),
        recipientsBcc = emptyList()
    )
    val JobApplicationDraftWithRecipients = buildExpectedLocalDraftFields(
        expectedSubject = "Application for some role",
        expectedSenderEmail = "anyone@proton.me",
        expectedDraftBody = "Hello, hire me",
        recipientsTo = listOf(RecipientSample.Alice.address),
        recipientsCc = listOf(RecipientSample.Bob.address),
        recipientsBcc = listOf(RecipientSample.Billing.address, RecipientSample.Doe.address)
    )

    private fun buildExpectedLocalDraftFields(
        expectedSubject: String = "Subject for the message",
        expectedSenderEmail: String = "drafts@proton.me",
        expectedDraftBody: String = "I am plaintext",
        recipientsTo: List<String> = listOf(RecipientSample.NamelessRecipient.address),
        recipientsCc: List<String> = listOf(RecipientSample.NamelessRecipient.address),
        recipientsBcc: List<String> = listOf(RecipientSample.NamelessRecipient.address)
    ) = LocalDraft(
        expectedSenderEmail,
        expectedSubject,
        expectedDraftBody,
        recipientsTo,
        recipientsCc,
        recipientsBcc
    )
}
