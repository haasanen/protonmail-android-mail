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
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class StoreDraftWithSubjectTest {

    private val draftRepository = mockk<DraftRepository>()

    private val storeDraftWithSubject = StoreDraftWithSubject(draftRepository)

    @Test
    fun `save draft with subject`() = runTest {
        // Given
        val subject = Subject("Subject of this email")
        givenSaveDraftSucceeds(subject)

        // When
        val actualEither = storeDraftWithSubject(subject)

        // Then
        coVerify { draftRepository.saveSubject(subject) }
        assertEquals(Unit.right(), actualEither)
    }

    @Test
    fun `returns error when save draft subject fails`() = runTest {
        // Given
        val subject = Subject("Subject of this email")
        val expected = DataError.Local.SaveDraftError.NoRustDraftAvailable
        givenSaveDraftFails(subject, expected)
        // When
        val actualEither = storeDraftWithSubject(subject)

        // Then
        assertEquals(expected.left(), actualEither)
    }

    private fun givenSaveDraftSucceeds(subject: Subject) {
        coEvery { draftRepository.saveSubject(subject) } returns Unit.right()
    }

    private fun givenSaveDraftFails(subject: Subject, expected: DataError) {
        coEvery { draftRepository.saveSubject(subject) } returns expected.left()
    }

}
