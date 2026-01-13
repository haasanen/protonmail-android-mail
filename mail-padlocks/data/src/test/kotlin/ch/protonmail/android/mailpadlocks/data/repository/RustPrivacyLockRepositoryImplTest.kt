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

package ch.protonmail.android.mailpadlocks.data.repository

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.data.mapper.LocalPrivacyLock
import ch.protonmail.android.mailcommon.data.mapper.LocalPrivacyLockColor
import ch.protonmail.android.mailcommon.data.mapper.LocalPrivacyLockIcon
import ch.protonmail.android.mailcommon.data.mapper.LocalPrivacyLockTooltip
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.local.MessageBodyDataSource
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailpadlocks.domain.PrivacyLock
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockColor
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockIcon
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockTooltip
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RustPrivacyLockRepositoryImplTest {

    private val testUserId = UserId("test-user-id")
    private val testMessageId = MessageId("123")

    private val mockMessageBodyDataSource = mockk<MessageBodyDataSource>()

    private val repository = RustPrivacyLockRepositoryImpl(
        messageBodyDataSource = mockMessageBodyDataSource
    )

    @Test
    fun `returns PrivacyLock Value when data source returns non-null lock`() = runTest {
        // Given
        val localPrivacyLock = mockk<LocalPrivacyLock> {
            coEvery { icon } returns LocalPrivacyLockIcon.CLOSED_LOCK
            coEvery { color } returns LocalPrivacyLockColor.GREEN
            coEvery { tooltip } returns LocalPrivacyLockTooltip.ZERO_ACCESS
        }
        coEvery {
            mockMessageBodyDataSource.getPrivacyLock(testUserId, any())
        } returns localPrivacyLock.right()

        // When
        val result = repository.getPrivacyLock(testUserId, testMessageId)

        // Then
        assertTrue(result.isRight())
        val privacyLock = result.getOrNull()
        assertTrue(privacyLock is PrivacyLock.Value)
        assertEquals(PrivacyLockIcon.ClosedLock, privacyLock.icon)
        assertEquals(PrivacyLockColor.Green, privacyLock.color)
        assertEquals(PrivacyLockTooltip.ZeroAccess, privacyLock.tooltip)
    }

    @Test
    fun `returns PrivacyLock None when data source returns null`() = runTest {
        // Given
        coEvery {
            mockMessageBodyDataSource.getPrivacyLock(testUserId, any())
        } returns null.right()

        // When
        val result = repository.getPrivacyLock(testUserId, testMessageId)

        // Then
        assertTrue(result.isRight())
        assertEquals(PrivacyLock.None, result.getOrNull())
    }

    @Test
    fun `propagates error when data source returns error`() = runTest {
        // Given
        val expectedError = DataError.Local.NoDataCached
        coEvery {
            mockMessageBodyDataSource.getPrivacyLock(testUserId, any<LocalMessageId>())
        } returns expectedError.left()

        // When
        val result = repository.getPrivacyLock(testUserId, testMessageId)

        // Then
        assertTrue(result.isLeft())
        assertEquals(expectedError, result.swap().getOrNull())
    }
}
