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
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.model.MessageId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StoreDraftWithBodyTest {


    private val draftRepository = mockk<DraftRepository>()

    private val storeDraftWithBody = StoreDraftWithBody(draftRepository)

    @Test
    fun `save draft with body`() = runTest {
        // Given
        val messageId = MessageId("messageId")
        val body = DraftBody("Body of this email")
        givenSaveDraftSucceeds(messageId, body)

        // When
        val actualEither = storeDraftWithBody(body)

        // Then
        coVerify { draftRepository.saveBody(body) }
        assertEquals(messageId.right(), actualEither)
    }

    @Test
    fun `returns error when save draft body fails`() = runTest {
        // Given
        val body = DraftBody("Body of this email")
        val expected = DataError.Local.SaveDraftError.NoRustDraftAvailable
        givenSaveDraftFails(body, expected)
        // When
        val actualEither = storeDraftWithBody(body)

        // Then
        assertEquals(expected.left(), actualEither)
    }

    private fun givenSaveDraftSucceeds(messageId: MessageId, body: DraftBody) {
        coEvery { draftRepository.saveBody(body) } returns messageId.right()
    }

    private fun givenSaveDraftFails(body: DraftBody, expected: DataError) {
        coEvery { draftRepository.saveBody(body) } returns expected.left()
    }
}
