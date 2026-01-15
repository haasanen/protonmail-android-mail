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

package ch.protonmail.android.mailcomposer.domain.model

import ch.protonmail.android.mailpadlocks.domain.PrivacyLock
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DraftFieldsTest {

    private val sut = DraftFields(
        sender = SenderEmail("A sender email"),
        subject = Subject("A test subject"),
        bodyFields = BodyFields(
            DraftHead("Draft head"),
            DraftBody("A draft body")
        ),
        mimeType = DraftMimeType.Html,
        recipientsTo = RecipientsTo(emptyList()),
        recipientsCc = RecipientsCc(emptyList()),
        recipientsBcc = RecipientsBcc(emptyList())
    )

    private val recipient = DraftRecipient.SingleRecipient("Name", "you@proton.ch", privacyLock = PrivacyLock.None)

    @Test
    fun `when has no recipientsTo or recipientsCc or recipientsBcc then hasRecipient is false`() {
        assertFalse(sut.hasAnyRecipient())
    }

    @Test
    fun `when has recipientsTo then hasRecipient is true`() {
        val result = sut.copy(recipientsTo = RecipientsTo(listOf(recipient)))
        assertTrue(result.hasAnyRecipient())
    }

    @Test
    fun `when has recipientsBcc then hasRecipient is true`() {
        val result = sut.copy(recipientsBcc = RecipientsBcc(listOf(recipient)))
        assertTrue(result.hasAnyRecipient())
    }

    @Test
    fun `when has recipientsCc then hasRecipient is true`() {
        val result = sut.copy(recipientsCc = RecipientsCc(listOf(recipient)))
        assertTrue(result.hasAnyRecipient())
    }
}
