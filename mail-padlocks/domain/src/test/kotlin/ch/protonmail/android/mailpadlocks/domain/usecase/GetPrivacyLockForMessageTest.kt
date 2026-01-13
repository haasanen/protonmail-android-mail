/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailpadlocks.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailpadlocks.domain.PrivacyLock
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockColor
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockIcon
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockTooltip
import ch.protonmail.android.mailpadlocks.domain.repository.PrivacyLockRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GetPrivacyLockForMessageTest {

    private val testUserId = UserId("test-user-id")
    private val testMessageId = MessageId("message-123")

    private val mockRepository = mockk<PrivacyLockRepository>()

    private val useCase = GetPrivacyLockForMessage(
        privacyLockRepository = mockRepository
    )

    @Test
    fun `returns PrivacyLock Value when repository returns value`() = runTest {
        // Given
        val expectedLock = PrivacyLock.Value(
            icon = PrivacyLockIcon.ClosedLockWithTick,
            color = PrivacyLockColor.Green,
            tooltip = PrivacyLockTooltip.ReceiveE2eVerifiedRecipient
        )
        coEvery { mockRepository.getPrivacyLock(testUserId, testMessageId) } returns expectedLock.right()

        // When
        val result = useCase(testUserId, testMessageId)

        // Then
        assertTrue(result.isRight())
        assertEquals(expectedLock, result.getOrNull())
    }

    @Test
    fun `returns PrivacyLock None when repository returns none`() = runTest {
        // Given
        coEvery { mockRepository.getPrivacyLock(testUserId, testMessageId) } returns PrivacyLock.None.right()

        // When
        val result = useCase(testUserId, testMessageId)

        // Then
        assertTrue(result.isRight())
        assertEquals(PrivacyLock.None, result.getOrNull())
    }

    @Test
    fun `propagates error when repository returns error`() = runTest {
        // Given
        val expectedError = DataError.Remote.NoNetwork
        coEvery { mockRepository.getPrivacyLock(testUserId, testMessageId) } returns expectedError.left()

        // When
        val result = useCase(testUserId, testMessageId)

        // Then
        assertTrue(result.isLeft())
        assertEquals(expectedError, result.swap().getOrNull())
    }
}
