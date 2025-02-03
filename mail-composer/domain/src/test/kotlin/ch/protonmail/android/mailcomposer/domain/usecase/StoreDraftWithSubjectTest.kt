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
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import kotlin.test.assertEquals

class StoreDraftWithSubjectTest {

    private val draftRepository = mockk<DraftRepository>()

    private val storeDraftWithSubject = StoreDraftWithSubject(draftRepository)

    @Test
    fun `save draft with subject`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val subject = Subject("Subject of this email")
        givenSaveDraftSucceeds(userId, messageId, subject)

        // When
        val actualEither = storeDraftWithSubject(userId, messageId, subject)

        // Then
        coVerify { draftRepository.saveSubject(userId, messageId, subject) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when save draft subject fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.build()
        val subject = Subject("Subject of this email")
        val expected = DataError.Local.SaveDraftError.NoRustDraftAvailable
        givenSaveDraftFails(userId, messageId, subject, expected)
        // When
        val actualEither = storeDraftWithSubject(userId, messageId, subject)

        // Then
        assertEquals(expected.left(), actualEither)
    }

    private fun givenSaveDraftSucceeds(
        userId: UserId,
        messageId: MessageId,
        subject: Subject
    ) {
        coEvery { draftRepository.saveSubject(userId, messageId, subject) } returns Unit.right()
    }

    private fun givenSaveDraftFails(
        userId: UserId,
        messageId: MessageId,
        subject: Subject,
        expected: DataError
    ) {
        coEvery { draftRepository.saveSubject(userId, messageId, subject) } returns expected.left()
    }

}
