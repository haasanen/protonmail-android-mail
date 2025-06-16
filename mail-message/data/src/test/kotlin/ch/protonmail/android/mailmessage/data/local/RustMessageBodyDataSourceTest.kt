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

package ch.protonmail.android.mailmessage.data.local

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalMimeType
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.wrapper.MailboxWrapper
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
import ch.protonmail.android.mailmessage.data.wrapper.DecryptedMessageWrapper
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import uniffi.proton_mail_uniffi.BodyOutput
import uniffi.proton_mail_uniffi.MessageBanner
import uniffi.proton_mail_uniffi.TransformOpts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RustMessageBodyDataSourceTest {

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val rustMailboxFactory: RustMailboxFactory = mockk()
    private val createRustMessageBodyAccessor = mockk<CreateRustMessageBodyAccessor>()

    private val testDispatcher = StandardTestDispatcher()

    private val dataSource = RustMessageBodyDataSource(
        createRustMessageBodyAccessor,
        rustMailboxFactory,
        testDispatcher
    )

    @Test
    fun `get message body should return message body`() = runTest(testDispatcher) {
        // Given
        val userId = UserIdTestData.userId
        val mailSession = mockk<MailUserSessionWrapper>()
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val mailbox = mockk<MailboxWrapper>()
        val transformOpts = mockk<TransformOpts>(relaxed = true)
        val bodyBanners = emptyList<MessageBanner>()
        val bodyOutput = BodyOutput(
            "message body",
            false,
            0uL,
            0uL,
            0uL,
            0uL,
            transformOpts,
            bodyBanners
        )
        val localMimeType = LocalMimeType.TEXT_PLAIN
        val decryptedMessageBodyWrapper = mockk<DecryptedMessageWrapper> {
            coEvery { body(any()) } returns bodyOutput.right()
            coEvery { mimeType() } returns localMimeType
        }

        coEvery { userSessionRepository.getUserSession(userId) } returns mailSession
        coEvery { rustMailboxFactory.createAllMail(userId) } returns mailbox.right()
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } returns decryptedMessageBodyWrapper.right()

        // When
        val result = dataSource.getMessageBody(userId, messageId, MessageBodyTransformations.MessageDetailsDefaults)

        // Then
        coVerify { rustMailboxFactory.createAllMail(userId) }
        coVerify { createRustMessageBodyAccessor(mailbox, messageId) }
        assertTrue(result.isRight())
    }

    @Test
    fun `get message body should handle error`() = runTest(testDispatcher) {
        // Given
        val userId = UserIdTestData.userId
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val mailbox = mockk<MailboxWrapper>()
        val expectedError = DataError.Local.NoDataCached
        coEvery { rustMailboxFactory.createAllMail(userId) } returns mailbox.right()
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } returns expectedError.left()
        // When
        val result = dataSource.getMessageBody(userId, messageId, MessageBodyTransformations.MessageDetailsDefaults)

        // Then
        coVerify { rustMailboxFactory.createAllMail(userId) }
        assertEquals(expectedError.left(), result)
    }

}
