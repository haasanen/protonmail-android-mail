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

package ch.protonmail.android.mailattachments.data

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailattachments.data.local.RustAttachmentDataSourceImpl
import ch.protonmail.android.mailattachments.data.usecase.GetRustAttachment
import ch.protonmail.android.mailcommon.data.mapper.LocalAttachmentId
import ch.protonmail.android.mailcommon.data.mapper.LocalDecryptedAttachment
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Assert.assertEquals
import org.junit.Test

class RustAttachmentDataSourceImplTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userSessionRepository = mockk<UserSessionRepository>()
    private val getRustAttachment = mockk<GetRustAttachment>()
    private val rustAttachmentDataSource = RustAttachmentDataSourceImpl(
        userSessionRepository = userSessionRepository,
        getRustAttachment = getRustAttachment,
        ioDispatcher = testDispatcher
    )

    @Test
    fun `getAttachment returns no user session error for null session`() = runTest(testDispatcher) {
        // Given
        val userId = UserId("test-user-id")
        val attachmentId = LocalAttachmentId(1u)
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = rustAttachmentDataSource.getAttachment(userId, attachmentId)

        // Then
        assertEquals(DataError.Local.NoUserSession.left(), result)
    }

    @Test
    fun `getAttachment returns decrypted attachment for valid session`() = runTest(testDispatcher) {
        // Given
        val userId = UserId("test-user-id")
        val attachmentId = LocalAttachmentId(2u)
        val session = mockk<MailUserSessionWrapper>()
        val expectedAttachment = mockk<LocalDecryptedAttachment>()
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { getRustAttachment(session, attachmentId) } returns expectedAttachment.right()

        // When
        val result = rustAttachmentDataSource.getAttachment(userId, attachmentId)

        // Then
        assertEquals(expectedAttachment.right(), result)
        coVerify { userSessionRepository.getUserSession(userId) }
        coVerify { getRustAttachment(session, attachmentId) }
    }

    @Test
    fun `getAttachment returns data error when attachment fetching fails`() = runTest(testDispatcher) {
        // Given
        val userId = UserId("test-user-id")
        val attachmentId = LocalAttachmentId(1u)
        val session = mockk<MailUserSessionWrapper>()
        val expectedError = DataError.Local.CryptoError
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { getRustAttachment(session, attachmentId) } returns expectedError.left()

        // When
        val result = rustAttachmentDataSource.getAttachment(userId, attachmentId)

        // Then
        assertEquals(expectedError.left(), result)
        coVerify { userSessionRepository.getUserSession(userId) }
        coVerify { getRustAttachment(session, attachmentId) }
    }
}
