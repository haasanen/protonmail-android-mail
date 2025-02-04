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

package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.sample.RecipientSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class StoreDraftWithRecipientsTest {


    private val draftRepository = mockk<DraftRepository>()

    private val storeDraftWithRecipients = StoreDraftWithRecipients(draftRepository)

    @Test
    fun `save draft with TO recipient`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipient = RecipientSample.Bob
        coEvery { draftRepository.saveToRecipient(userId, messageId, recipient) } returns Unit.right()

        // When
        val actualEither = storeDraftWithRecipients(userId, messageId, to = listOf(recipient))

        // Then
        coVerify { draftRepository.saveToRecipient(userId, messageId, recipient) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when save draft with TO recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipient = RecipientSample.Bob
        val expected = DataError.Local.SaveDraftError.DuplicateRecipient
        coEvery { draftRepository.saveToRecipient(userId, messageId, recipient) } returns expected.left()
        // When
        val actualEither = storeDraftWithRecipients(userId, messageId, to = listOf(recipient))

        // Then
        assertEquals(expected.left(), actualEither)
    }

    @Test
    fun `save draft with CC recipient`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipient = RecipientSample.Alice
        coEvery { draftRepository.saveCcRecipient(userId, messageId, recipient) } returns Unit.right()

        // When
        val actualEither = storeDraftWithRecipients(userId, messageId, cc = listOf(recipient))

        // Then
        coVerify { draftRepository.saveCcRecipient(userId, messageId, recipient) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when save draft with CC recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipient = RecipientSample.Alice
        val expected = DataError.Local.SaveDraftError.SaveFailed
        coEvery { draftRepository.saveCcRecipient(userId, messageId, recipient) } returns expected.left()
        // When
        val actualEither = storeDraftWithRecipients(userId, messageId, cc = listOf(recipient))

        // Then
        assertEquals(expected.left(), actualEither)
    }

    @Test
    fun `save draft with BCC recipient`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipient = RecipientSample.Doe
        coEvery { draftRepository.saveBccRecipient(userId, messageId, recipient) } returns Unit.right()

        // When
        val actualEither = storeDraftWithRecipients(userId, messageId, bcc = listOf(recipient))

        // Then
        coVerify { draftRepository.saveBccRecipient(userId, messageId, recipient) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when save draft with BCC recipient fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val recipient = RecipientSample.Doe
        val expected = DataError.Local.SaveDraftError.SaveFailed
        coEvery { draftRepository.saveBccRecipient(userId, messageId, recipient) } returns expected.left()
        // When
        val actualEither = storeDraftWithRecipients(userId, messageId, bcc = listOf(recipient))

        // Then
        assertEquals(expected.left(), actualEither)
    }
}
