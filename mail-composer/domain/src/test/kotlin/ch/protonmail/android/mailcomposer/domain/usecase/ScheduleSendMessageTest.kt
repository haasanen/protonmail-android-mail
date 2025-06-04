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
import ch.protonmail.android.mailcomposer.domain.repository.DraftRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ScheduleSendMessageTest {

    private val draftRepository = mockk<DraftRepository>()

    private val scheduleSendMessage = ScheduleSendMessage(draftRepository)

    @Test
    fun `schedule send message proxies call to repository`() = runTest {
        // Given
        val time = Instant.fromEpochSeconds(123)
        val expected = Unit.right()
        coEvery { draftRepository.scheduleSend(time) } returns expected

        // When
        val actual = scheduleSendMessage(time)

        // Then
        assertEquals(expected, actual)
    }

    @Test
    fun `schedule send message returns failure when repository fails`() = runTest {
        // Given
        val time = Instant.fromEpochSeconds(123)
        val expected = DataError.Local.DbWriteFailed.left()
        coEvery { draftRepository.scheduleSend(time) } returns expected

        // When
        val actual = scheduleSendMessage(time)

        // Then
        assertEquals(expected, actual)
    }
}
