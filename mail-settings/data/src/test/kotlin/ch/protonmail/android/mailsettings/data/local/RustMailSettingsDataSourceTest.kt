package ch.protonmail.android.mailsettings.data.local

import app.cash.turbine.test
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsettings.data.usecase.CreateRustUserMailSettings
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.mailsettings.rust.LocalMailSettingsTestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.MailSettingsUpdated
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailUserSettings
import kotlin.test.assertEquals

class RustMailSettingsDataSourceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val loggingTestRule = LoggingTestRule()

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val createRustMailSettings = mockk<CreateRustUserMailSettings>()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val mailSettingsDataSource = RustMailSettingsDataSource(
        userSessionRepository,
        createRustMailSettings,
        testCoroutineScope
    )

    @Test
    fun `observe mail settings fails and logs error when session is invalid`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Given
            every { userSessionRepository.observeCurrentUserSession() } returns flowOf(null)

            // When
            mailSettingsDataSource.observeMailSettings().test {
                // Then
                loggingTestRule.assertErrorLogged("rust-settings: trying to load settings with a null session")
                expectNoEvents()
            }
        }

    @Test
    fun `observe mail settings emits items when returned by the rust library`() = runTest {
        // Given
        val expected = LocalMailSettingsTestData.mailSettings
        val mailSettingsCallbackSlot = slot<MailSettingsUpdated>()
        val userSessionMock = mockk<MailUserSession>()
        every { userSessionRepository.observeCurrentUserSession() } returns flowOf(userSessionMock)
        val liveQueryMock = mockk<MailUserSettings> {
            every { value() } returns expected
        }
        every { createRustMailSettings(userSessionMock, capture(mailSettingsCallbackSlot)) } returns liveQueryMock

        mailSettingsDataSource.observeMailSettings().test {
            // When
            mailSettingsCallbackSlot.captured.onUpdated()

            // Then
            assertEquals(expected, awaitItem())
        }
    }

}
