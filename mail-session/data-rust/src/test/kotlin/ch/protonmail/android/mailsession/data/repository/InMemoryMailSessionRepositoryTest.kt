package ch.protonmail.android.mailsession.data.repository

import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import uniffi.proton_mail_uniffi.MailSession
import kotlin.test.Test
import kotlin.test.assertEquals

class InMemoryMailSessionRepositoryTest {

    private val mailSessionRepository = InMemoryMailSessionRepository()

    @Test
    fun `returns existing mail session when present`() = runTest {
        // Given
        val expected = mockk<MailSession>()
        mailSessionRepository.setMailSession(expected)

        // When
        val actual = mailSessionRepository.getMailSession()

        // Then
        assertEquals(expected, actual)
    }

}
