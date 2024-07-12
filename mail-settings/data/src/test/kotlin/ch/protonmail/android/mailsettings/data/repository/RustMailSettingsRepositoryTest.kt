package ch.protonmail.android.mailsettings.data.repository

import app.cash.turbine.test
import ch.protonmail.android.mailsettings.data.local.MailSettingsDataSource
import ch.protonmail.android.testdata.mailsettings.MailSettingsTestData
import ch.protonmail.android.testdata.mailsettings.rust.LocalMailSettingsTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.MailSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class RustMailSettingsRepositoryTest {

    private val mailSettingsDataSource = mockk<MailSettingsDataSource>()

    private val mailSettingsRepository = RustMailSettingsRepository(mailSettingsDataSource)

    @Test
    fun `observe mail settings from rust data source`() = runTest {
        // Given
        val localMailSettings = LocalMailSettingsTestData.mailSettings
        val expectedMailSettings = MailSettingsTestData.mailSettingsFromRust

        every { mailSettingsDataSource.observeMailSettings() } returns flowOf(localMailSettings)

        // When
        mailSettingsRepository.getMailSettingsFlow(UserId("anyuser")).test {
            // Then
            assertEquals(expectedMailSettings, resultOrNull(awaitItem()))
            awaitComplete()
        }
    }

    @Test
    fun `get mail settings from rust data source`() = runTest {
        // Given
        val localMailSettings = LocalMailSettingsTestData.mailSettings
        val expectedMailSettings = MailSettingsTestData.mailSettingsFromRust

        every { mailSettingsDataSource.observeMailSettings() } returns flowOf(localMailSettings)

        // When
        val actual = mailSettingsRepository.getMailSettings(UserId("anyuser"))

        // Then
        assertEquals(expectedMailSettings, actual)
    }

    private fun resultOrNull(dataResult: DataResult<MailSettings>): MailSettings? = when (dataResult) {
        is DataResult.Success -> dataResult.value
        else -> null
    }
}
