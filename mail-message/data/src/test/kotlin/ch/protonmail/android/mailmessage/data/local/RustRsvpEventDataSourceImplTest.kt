/*
 * Copyright (c) 2025 Proton Technologies AG
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

import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpAnswer
import ch.protonmail.android.mailcommon.data.mapper.LocalRsvpEvent
import ch.protonmail.android.maillabel.data.local.RustMailboxFactory
import ch.protonmail.android.maillabel.data.wrapper.MailboxWrapper
import ch.protonmail.android.mailmessage.data.usecase.CreateRustEventServiceAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustEventServiceProviderAccessor
import ch.protonmail.android.mailmessage.data.usecase.CreateRustMessageBodyAccessor
import ch.protonmail.android.mailmessage.data.wrapper.DecryptedMessageWrapper
import ch.protonmail.android.mailmessage.data.wrapper.RsvpEventServiceProviderWrapper
import ch.protonmail.android.mailmessage.data.wrapper.RsvpEventServiceWrapper
import ch.protonmail.android.testdata.message.rust.LocalMessageIdSample
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RustRsvpEventDataSourceImplTest {

    private val rustMailboxFactory: RustMailboxFactory = mockk()
    private val createRustMessageBodyAccessor = mockk<CreateRustMessageBodyAccessor>()
    private val createRustEventServiceProviderAccessor = mockk<CreateRustEventServiceProviderAccessor>()
    private val createRustEventServiceAccessor = mockk<CreateRustEventServiceAccessor>()

    private val testDispatcher = StandardTestDispatcher()

    private val dataSource = RustRsvpEventDataSourceImpl(
        createRustMessageBodyAccessor,
        createRustEventServiceProviderAccessor,
        createRustEventServiceAccessor,
        testDispatcher,
        rustMailboxFactory
    )

    @Test
    fun `identify rsvp should return whether the message contains an event`() = runTest(testDispatcher) {
        // Given
        val userId = UserIdTestData.userId
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val mailbox = mockk<MailboxWrapper>()
        val decryptedMessageBodyWrapper = mockk<DecryptedMessageWrapper>()
        val eventServiceProviderWrapper = mockk<RsvpEventServiceProviderWrapper>()
        coEvery { rustMailboxFactory.createAllMail(userId) } returns mailbox.right()
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } returns decryptedMessageBodyWrapper.right()
        coEvery {
            createRustEventServiceProviderAccessor(decryptedMessageBodyWrapper, messageId)
        } returns eventServiceProviderWrapper

        // When
        val actual = dataSource.identifyRsvp(userId, messageId)

        // Then
        assertEquals(true.right(), actual)
    }

    @Test
    fun `get rsvp event should get the rsvp event`() = runTest(testDispatcher) {
        // Given
        val userId = UserIdTestData.userId
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val mailbox = mockk<MailboxWrapper>()
        val decryptedMessageBodyWrapper = mockk<DecryptedMessageWrapper>()
        val eventServiceProviderWrapper = mockk<RsvpEventServiceProviderWrapper>()
        val rsvpEvent = mockk<LocalRsvpEvent>()
        val eventServiceWrapper = mockk<RsvpEventServiceWrapper> {
            every { this@mockk.get() } returns rsvpEvent.right()
        }
        coEvery { rustMailboxFactory.createAllMail(userId) } returns mailbox.right()
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } returns decryptedMessageBodyWrapper.right()
        coEvery {
            createRustEventServiceProviderAccessor(decryptedMessageBodyWrapper, messageId)
        } returns eventServiceProviderWrapper
        coEvery { createRustEventServiceAccessor(eventServiceProviderWrapper, messageId) } returns eventServiceWrapper

        // When
        val actual = dataSource.getRsvpEvent(userId, messageId)

        // Then
        assertEquals(rsvpEvent.right(), actual)
    }

    @Test
    fun `answer rsvp event should answer the rsvp event`() = runTest(testDispatcher) {
        // Given
        val userId = UserIdTestData.userId
        val messageId = LocalMessageIdSample.AugWeatherForecast
        val mailbox = mockk<MailboxWrapper>()
        val decryptedMessageBodyWrapper = mockk<DecryptedMessageWrapper>()
        val eventServiceProviderWrapper = mockk<RsvpEventServiceProviderWrapper>()
        val eventServiceWrapper = mockk<RsvpEventServiceWrapper> {
            coEvery { this@mockk.answer(LocalRsvpAnswer.YES) } returns Unit.right()
        }
        coEvery { rustMailboxFactory.createAllMail(userId) } returns mailbox.right()
        coEvery { createRustMessageBodyAccessor(mailbox, messageId) } returns decryptedMessageBodyWrapper.right()
        coEvery {
            createRustEventServiceProviderAccessor(decryptedMessageBodyWrapper, messageId)
        } returns eventServiceProviderWrapper
        coEvery { createRustEventServiceAccessor(eventServiceProviderWrapper, messageId) } returns eventServiceWrapper

        // When
        val actual = dataSource.answerRsvpEvent(userId, messageId, LocalRsvpAnswer.YES)

        // Then
        assertEquals(Unit.right(), actual)
    }
}
