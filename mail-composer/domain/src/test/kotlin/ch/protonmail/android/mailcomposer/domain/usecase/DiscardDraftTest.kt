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

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DiscardDraftTest {

    private val draftRepository = mockk<DraftRepository>()

    private val discardDraft = DiscardDraft(draftRepository)

    @Test
    fun `should call a repository method when use case is called`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        coEvery { draftRepository.discardDraft(userId, messageId) } returns Unit.right()

        // When
        val result = discardDraft(userId, messageId)

        // Then
        coVerify { draftRepository.discardDraft(userId, messageId) }
        assertEquals(Unit.right(), result)
    }
}
