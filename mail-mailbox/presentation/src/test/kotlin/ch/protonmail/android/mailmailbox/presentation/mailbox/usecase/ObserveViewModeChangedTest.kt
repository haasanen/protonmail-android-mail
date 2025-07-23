package ch.protonmail.android.mailmailbox.presentation.mailbox.usecase

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailsettings.domain.usecase.ObserveMailSettings
import ch.protonmail.android.testdata.mailsettings.MailSettingsTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.mailsettings.domain.entity.MailSettings
import org.junit.Test

class ObserveViewModeChangedTest {

    private val userId = UserIdSample.Primary

    private val settingsFlow = MutableSharedFlow<MailSettings>()
    private val observeMailSettings = mockk<ObserveMailSettings> {
        every { this@mockk.invoke(userId) } returns settingsFlow
    }

    private val observeViewModeChanged = ObserveViewModeChanged(observeMailSettings)

    @Test
    fun `triggers flow when initialized`() = runTest {
        observeViewModeChanged(userId).test {
            // When
            settingsFlow.emit(MailSettingsTestData.mailSettingsMessageViewMode)

            // Then
            awaitItem()
        }
    }

    @Test
    fun `triggers flow when mail settings emits an updated view mode`() = runTest {
        observeViewModeChanged(userId).test {
            settingsFlow.emit(MailSettingsTestData.mailSettingsMessageViewMode)
            awaitItem() // first emission due to initializing the use case

            // When
            settingsFlow.emit(MailSettingsTestData.mailSettingsConvoViewMode)

            // Then
            awaitItem()
        }
    }

    @Test
    fun `does not trigger flow when mail settings emits with no updates to view mode`() = runTest {
        observeViewModeChanged(userId).test {
            settingsFlow.emit(MailSettingsTestData.mailSettingsMessageViewMode)
            awaitItem() // first emission due to initializing the use case

            // When
            settingsFlow.emit(MailSettingsTestData.mailSettingsMessageViewMode)

            // Then
            expectNoEvents()
        }
    }
}
