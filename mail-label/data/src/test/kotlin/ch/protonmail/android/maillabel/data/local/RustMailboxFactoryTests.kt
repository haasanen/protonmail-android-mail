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

package ch.protonmail.android.maillabel.data.local

import arrow.core.Either
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.usecase.CreateAllMailMailbox
import ch.protonmail.android.maillabel.data.usecase.CreateMailbox
import ch.protonmail.android.maillabel.data.wrapper.MailboxWrapper
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.usecase.GetSelectedMailLabelId
import ch.protonmail.android.mailsession.data.usecase.ExecuteWithUserSession
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class RustMailboxFactoryTests {

    private val executeWithUserSession = mockk<ExecuteWithUserSession>()
    private val createMailbox = mockk<CreateMailbox>()
    private val createAllMailMailbox = mockk<CreateAllMailMailbox>()
    private val getSelectedMailLabelId = mockk<GetSelectedMailLabelId>()

    private lateinit var rustMailboxFactory: RustMailboxFactory

    @BeforeTest
    fun setup() {
        rustMailboxFactory = RustMailboxFactory(
            executeWithUserSession,
            createMailbox,
            createAllMailMailbox,
            getSelectedMailLabelId
        )
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should create new mailbox when creating a new label (deprecated)`() = runTest {
        // Given
        val userId = userId1
        val mockedSession = mockk<MailUserSessionWrapper>()
        val mockedWrapper = mockk<MailboxWrapper>()
        val localLabelId = localLabelId1
        val labelId = labelId1

        expectMockSession(userId, mockedSession)
        coEvery { getSelectedMailLabelId().labelId } returns labelId
        every { createMailbox(mockedSession, localLabelId) } returns mockedWrapper.right()

        // When + Then
        val mailbox = rustMailboxFactory.create(userId)
        assertEquals(mockedWrapper.right(), mailbox)

        val newMailbox = rustMailboxFactory.create(userId)
        assertEquals(mockedWrapper.right(), newMailbox)

        verify(exactly = 2) { createMailbox(mockedSession, localLabelId) }
        confirmVerified(createMailbox)
    }

    @Test
    fun `should create new mailbox when the user changes (deprecated)`() = runTest {
        // Given
        val userId = userId1
        val newUserId = userId2
        val localLabelId = localLabelId1
        val labelId = labelId1

        expectMockSession(userId, mockedUser1Session)
        expectMockSession(newUserId, mockedUser2Session)
        coEvery { getSelectedMailLabelId().labelId } returns labelId
        every { createMailbox(mockedUser1Session, localLabelId) } returns mockedUser1Mailbox.right()
        every { createMailbox(mockedUser2Session, localLabelId) } returns mockedUser2Mailbox.right()

        val mailbox = rustMailboxFactory.create(userId)
        assertEquals(mockedUser1Mailbox.right(), mailbox)

        // When
        val newMailbox = rustMailboxFactory.create(newUserId)
        assertEquals(mockedUser2Mailbox.right(), newMailbox)

        // Then
        assertNotEquals(mailbox, newMailbox)
        verify(exactly = 1) { createMailbox(mockedUser1Session, localLabelId) }
        verify(exactly = 1) { createMailbox(mockedUser2Session, localLabelId) }
        confirmVerified(createMailbox)
    }

    @Test
    fun `should create new mailbox when the label changes (deprecated)`() = runTest {
        // Given
        val userId = userId1
        val mockedSession = mockk<MailUserSessionWrapper>()

        expectMockSession(userId, mockedSession)
        coEvery { getSelectedMailLabelId().labelId } returns labelId1
        every { createMailbox(mockedSession, localLabelId1) } returns labelId1MailboxWrapper.right()

        val mailbox = rustMailboxFactory.create(userId)
        assertEquals(labelId1MailboxWrapper.right(), mailbox)

        // When
        coEvery { getSelectedMailLabelId().labelId } returns labelId2
        every { createMailbox(mockedSession, localLabelId2) } returns labelId2MailboxWrapper.right()
        val newMailbox = rustMailboxFactory.create(userId)
        assertEquals(labelId2MailboxWrapper.right(), newMailbox)

        // Then
        assertNotEquals(mailbox, newMailbox)
        verify(exactly = 1) { createMailbox(mockedSession, localLabelId1) }
        verify(exactly = 1) { createMailbox(mockedSession, localLabelId2) }
        confirmVerified(createMailbox)
    }

    @Test
    fun `should create new mailbox when the label changes()`() = runTest {
        // Given
        val userId = userId1
        val mockedSession = mockk<MailUserSessionWrapper>()

        expectMockSession(userId, mockedSession)
        every { createMailbox(mockedSession, localLabelId1) } returns labelId1MailboxWrapper.right()

        val mailbox = rustMailboxFactory.create(userId, localLabelId1)
        assertEquals(labelId1MailboxWrapper.right(), mailbox)

        // When
        every { createMailbox(mockedSession, localLabelId2) } returns labelId2MailboxWrapper.right()
        val newMailbox = rustMailboxFactory.create(userId, localLabelId2)
        assertEquals(labelId2MailboxWrapper.right(), newMailbox)

        // Then
        assertNotEquals(mailbox, newMailbox)
        verify(exactly = 1) { createMailbox(mockedSession, localLabelId1) }
        verify(exactly = 1) { createMailbox(mockedSession, localLabelId2) }
        confirmVerified(createMailbox)
    }

    @Test
    fun `should forward the AllMail Mailbox creation to the corresponding UC`() = runTest {
        // Given
        val userId = userId1
        val mockedSession = mockk<MailUserSessionWrapper>()
        val mockedWrapper = mockk<MailboxWrapper>()

        expectMockSession(userId, mockedSession)
        every { createAllMailMailbox(mockedSession) } returns mockedWrapper.right()

        // When
        val mailbox = rustMailboxFactory.createAllMail(userId)

        // Then
        assertEquals(mockedWrapper.right(), mailbox)
        verify(exactly = 1) { createAllMailMailbox(mockedSession) }
        confirmVerified(createAllMailMailbox)
    }

    private fun expectMockSession(userId: UserId, mockSession: MailUserSessionWrapper) {
        coEvery {
            executeWithUserSession(
                userId,
                any<suspend (MailUserSessionWrapper) -> Either<DataError, Either<DataError, MailboxWrapper>>>()
            )
        } coAnswers {
            val action =
                secondArg<suspend (MailUserSessionWrapper) -> Either<DataError, Either<DataError, MailboxWrapper>>>()
            action(mockSession).right()
        }
    }

    private companion object {

        val userId1 = UserId("userid-1")
        val userId2 = UserId("userid-2")

        val mockedUser1Session = mockk<MailUserSessionWrapper>()
        val mockedUser2Session = mockk<MailUserSessionWrapper>()
        val mockedUser1Mailbox = mockk<MailboxWrapper>()
        val mockedUser2Mailbox = mockk<MailboxWrapper>()

        val labelId1MailboxWrapper = mockk<MailboxWrapper>()
        val labelId2MailboxWrapper = mockk<MailboxWrapper>()

        val labelId1 = LabelId("123")
        val labelId2 = LabelId("456")
        val localLabelId1 = LocalLabelId(123u)
        val localLabelId2 = LocalLabelId(456u)
    }
}
