package ch.protonmail.android.mailsettings.data.local

import app.cash.turbine.test
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsettings.data.usecase.CreateRustUserMailSettings
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.mailsettings.rust.LocalMailSettingsTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
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
            val userId = UserIdTestData.userId
            coEvery { userSessionRepository.getUserSession(userId) } returns null

            // When
            mailSettingsDataSource.observeMailSettings(userId).test {
                // Then
                loggingTestRule.assertErrorLogged("rust-settings: trying to load settings with a null session")
                expectNoEvents()
            }
        }

    @Test
    fun `observe mail settings emits items when updated by the rust library`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expected = LocalMailSettingsTestData.mailSettings
        val expectedUpdated = LocalMailSettingsTestData.mailSettings.copy(displayName = "updated display name")
        val mailSettingsCallbackSlot = slot<MailSettingsUpdated>()
        val userSessionMock = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock
        val liveQueryMock = mockk<MailUserSettings> {
            every { value() } returns expected
        }
        every { createRustMailSettings(userSessionMock, capture(mailSettingsCallbackSlot)) } returns liveQueryMock

        mailSettingsDataSource.observeMailSettings(userId).test {
            // Given
            assertEquals(expected, awaitItem()) // Initial value
            every { liveQueryMock.value() } returns expectedUpdated
            // When
            mailSettingsCallbackSlot.captured.onUpdated()

            // Then
            assertEquals(expectedUpdated, awaitItem())
        }
    }

    @Test
    fun `observe mail settings emits initial value from the rust library`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val expected = LocalMailSettingsTestData.mailSettings
        val mailSettingsCallbackSlot = slot<MailSettingsUpdated>()
        val userSessionMock = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns userSessionMock
        val liveQueryMock = mockk<MailUserSettings> {
            every { value() } returns expected
        }
        every { createRustMailSettings(userSessionMock, capture(mailSettingsCallbackSlot)) } returns liveQueryMock

        mailSettingsDataSource.observeMailSettings(userId).test {

            // Then
            assertEquals(expected, awaitItem())
        }
    }


}
