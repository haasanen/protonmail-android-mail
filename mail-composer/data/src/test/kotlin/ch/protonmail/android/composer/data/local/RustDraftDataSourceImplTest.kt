package ch.protonmail.android.composer.data.local

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.composer.data.usecase.CreateRustDraft
import ch.protonmail.android.composer.data.usecase.OpenRustDraft
import ch.protonmail.android.composer.data.wrapper.DraftWrapper
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import ch.protonmail.android.testdata.composer.LocalDraftTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import uniffi.proton_mail_uniffi.DraftCreateMode
import kotlin.test.Test
import kotlin.test.assertEquals

class RustDraftDataSourceImplTest {

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val createRustDraft = mockk<CreateRustDraft>()
    private val openRustDraft = mockk<OpenRustDraft>()

    private val mockUserSession = mockk<MailUserSessionWrapper>()

    private val dataSource = RustDraftDataSourceImpl(
        userSessionRepository,
        createRustDraft,
        openRustDraft
    )

    @Test
    fun `open draft returns error when there is no user session`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val expected = DataError.Local.Unknown
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val actual = dataSource.open(userId, messageId)

        // Then
        assertEquals(actual, expected.left())
    }

    @Test
    fun `open draft returns Local Draft data when opened successfully`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.RustJobApplication
        val localMessageId = MessageIdSample.RustJobApplication.toLocalMessageId()
        val expected = LocalDraftTestData.JobApplicationDraft
        val expectedDraftWrapper = expectDraftWrapperReturns(expected.subject, expected.sender, expected.body)
        coEvery { userSessionRepository.getUserSession(userId) } returns mockUserSession
        coEvery { openRustDraft(mockUserSession, localMessageId) } returns expectedDraftWrapper.right()

        // When
        val actual = dataSource.open(userId, messageId)

        // Then
        assertEquals(actual, expected.right())
    }

    @Test
    fun `data source holds instance of the draft when opened successfully`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.RustJobApplication
        val localMessageId = MessageIdSample.RustJobApplication.toLocalMessageId()
        val expected = LocalDraftTestData.JobApplicationDraft
        val expectedDraftWrapper = expectDraftWrapperReturns(expected.subject, expected.sender, expected.body)
        coEvery { userSessionRepository.getUserSession(userId) } returns mockUserSession
        coEvery { openRustDraft(mockUserSession, localMessageId) } returns expectedDraftWrapper.right()
        assertNull(dataSource.rustDraftWrapper)

        // When
        dataSource.open(userId, messageId)

        // Then
        assertEquals(dataSource.rustDraftWrapper, expectedDraftWrapper)
    }

    @Test
    fun `create draft returns error when there is no user session`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val action = DraftAction.Reply(messageId)
        val expected = DataError.Local.Unknown
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val actual = dataSource.create(userId, action)

        // Then
        assertEquals(actual, expected.left())
    }

    @Test
    fun `create draft returns error when draft create mode is not supported`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val action = DraftAction.ComposeToAddresses(emptyList())
        val expected = DataError.Local.UnsupportedOperation
        coEvery { userSessionRepository.getUserSession(userId) } returns mockUserSession

        // When
        val actual = dataSource.create(userId, action)

        // Then
        assertEquals(actual, expected.left())
    }

    @Test
    fun `create draft returns Local Draft data when created successfully`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.RustJobApplication
        val action = DraftAction.ReplyAll(messageId)
        val localDraftCreateMode = DraftCreateMode.ReplyAll(messageId.toLocalMessageId())
        val expected = LocalDraftTestData.JobApplicationDraft
        val subject = expected.subject
        val sender = expected.sender
        val body = expected.body
        val expectedDraftWrapper = expectDraftWrapperReturns(subject, sender, body)
        coEvery { userSessionRepository.getUserSession(userId) } returns mockUserSession
        coEvery { createRustDraft(mockUserSession, localDraftCreateMode) } returns expectedDraftWrapper.right()

        // When
        val actual = dataSource.create(userId, action)

        // Then
        assertEquals(actual, expected.right())
    }

    @Test
    fun `data source holds instance of the draft when created successfully`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.RustJobApplication
        val action = DraftAction.Forward(messageId)
        val localDraftCreateMode = DraftCreateMode.Forward(messageId.toLocalMessageId())
        val expected = LocalDraftTestData.JobApplicationDraft
        val expectedDraftWrapper = expectDraftWrapperReturns(expected.subject, expected.sender, expected.body)
        coEvery { userSessionRepository.getUserSession(userId) } returns mockUserSession
        coEvery { createRustDraft(mockUserSession, localDraftCreateMode) } returns expectedDraftWrapper.right()
        assertNull(dataSource.rustDraftWrapper)

        // When
        dataSource.create(userId, action)

        // Then
        assertEquals(dataSource.rustDraftWrapper, expectedDraftWrapper)
    }

    private fun expectDraftWrapperReturns(
        subject: String,
        sender: String,
        body: String
    ) = mockk<DraftWrapper> {
        every { subject() } returns subject
        every { sender() } returns sender
        every { body() } returns body
        every { recipientsTo() } returns mockk()
        every { recipientsCc() } returns mockk()
        every { recipientsBcc() } returns mockk()
    }
}
