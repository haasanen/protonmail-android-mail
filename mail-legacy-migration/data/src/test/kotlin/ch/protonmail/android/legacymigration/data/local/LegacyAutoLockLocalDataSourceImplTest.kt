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

package ch.protonmail.android.legacymigration.data.local

import java.io.IOException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.data.local.autolock.LegacyAutoLockDataStoreProvider
import ch.protonmail.android.legacymigration.data.local.autolock.LegacyAutoLockLocalDataSourceImpl
import ch.protonmail.android.legacymigration.data.usecase.DecryptLegacySerializableValue
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockBiometricsPreference
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockIntervalPreference
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockPin
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockPreference
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class LegacyAutoLockLocalDataSourceImplTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val preferences = mockk<Preferences>()
    private val dataStore = mockk<DataStore<Preferences>> {
        every { data } returns flowOf(preferences)
    }

    private val keyStoreCrypto = mockk<KeyStoreCrypto>()
    private val decryptLegacySerializableValue = DecryptLegacySerializableValue(keyStoreCrypto)
    private val dataStoreProvider = mockk<LegacyAutoLockDataStoreProvider> {
        every { legacyAutoLockDataStore } returns dataStore
    }

    private val dataSource = LegacyAutoLockLocalDataSourceImpl(
        dataStoreProvider = dataStoreProvider,
        decryptLegacySerializableValue = decryptLegacySerializableValue,
        coroutineScope = CoroutineScope(coroutineRule.testDispatcher)
    )

    private val pinKey = stringPreferencesKey("pinCodePrefKey")
    private val biometricKey = stringPreferencesKey("autoLockBiometricsKey")
    private val autoLockEnabledKey = stringPreferencesKey("hasAutoLockPrefKey")
    private val autoLockIntervalKey = stringPreferencesKey("autoLockIntervalPrefKey")

    @Test
    fun `autoLockEnabled returns decrypted preference when key is present and valid`() = runTest {
        // Given
        val encrypted = "encrypted-value"
        val decryptedJson = "true"
        val expected = LegacyAutoLockPreference(isEnabled = true)

        every { preferences[autoLockEnabledKey] } returns encrypted
        coEvery { keyStoreCrypto.decrypt(encrypted) } returns decryptedJson

        // When
        val result = dataSource.autoLockEnabled()

        // Then
        assertEquals(expected.right(), result)
    }

    @Test
    fun `autoLockEnabled returns failure when key is missing`() = runTest {
        // Given
        every { preferences[autoLockEnabledKey] } returns null

        // When
        val result = dataSource.autoLockEnabled()

        // Then
        assertEquals(MigrationError.AutoLockFailure.FailedToReadAutoLockEnabled.left(), result)
    }

    @Test
    fun `autoLockEnabled returns decryption failure when crypto throws`() = runTest {
        // Given
        val encrypted = "invalid-encrypted"
        every { preferences[autoLockEnabledKey] } returns encrypted
        coEvery { keyStoreCrypto.decrypt(encrypted) } throws IOException("decryption failed")

        // When
        val result = dataSource.autoLockEnabled()

        // Then
        assertEquals(MigrationError.AutoLockFailure.FailedToDecryptAutoLockEnabled.left(), result)
    }

    @Test
    fun `observeAutoLockPinCode emits decrypted pin when key exists`() = runTest {
        // Given
        val encrypted = "encryptedPin"
        val decryptedJson = "\"1234\""
        val expected = LegacyAutoLockPin("1234")

        every { preferences[pinKey] } returns encrypted
        coEvery { keyStoreCrypto.decrypt(encrypted) } returns decryptedJson

        // When / Then
        dataSource.observeAutoLockPinCode().test {
            assertEquals(expected.right(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeAutoLockPinCode emits error when key is missing`() = runTest {
        // Given
        every { preferences[pinKey] } returns null

        // When / Then
        dataSource.observeAutoLockPinCode().test {
            assertEquals(MigrationError.AutoLockFailure.FailedToReadAutoLockPin.left(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasAutoLockPinCode returns true when non-empty value is stored`() = runTest {
        // Given
        every { preferences[pinKey] } returns "encryptedPin"

        // When
        val result = dataSource.hasAutoLockPinCode()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasAutoLockPinCode returns false when key is missing`() = runTest {
        // Given
        every { preferences[pinKey] } returns null

        // When
        val result = dataSource.hasAutoLockPinCode()

        // Then
        assertFalse(result)
    }

    @Test
    fun `getAutoLockBiometricsPreference returns true when value is present and true`() = runTest {
        // Given
        every { preferences[biometricKey] } returns "encryptedBiometric"
        coEvery { keyStoreCrypto.decrypt("encryptedBiometric") } returns "\"true\""
        val expected = LegacyAutoLockBiometricsPreference(true).right()

        // When
        val result = dataSource.getAutoLockBiometricsPreference()

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `getAutoLockBiometricsPreference returns false when value is present and false`() = runTest {
        // Given
        every { preferences[biometricKey] } returns "encryptedBiometric"
        coEvery { keyStoreCrypto.decrypt("encryptedBiometric") } returns "\"false\""
        val expected = LegacyAutoLockBiometricsPreference(false).right()

        // When
        val result = dataSource.getAutoLockBiometricsPreference()

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `getAutoLockBiometricsPreference returns error when key is missing`() = runTest {
        // Given
        every { preferences[biometricKey] } returns null
        val expected = MigrationError.AutoLockFailure.EmptyBiometricPreferenceRead.left()

        // When
        val result = dataSource.getAutoLockBiometricsPreference()

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `getAutoLockInterval returns duration when value is present and valid`() = runTest {
        // Given
        every { preferences[autoLockIntervalKey] } returns "encryptedInterval"
        coEvery { keyStoreCrypto.decrypt("encryptedInterval") } returns "\"FiveMinutes\""
        val expected = LegacyAutoLockIntervalPreference(5.minutes).right()

        // When
        val result = dataSource.getAutoLockInterval()

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `getAutoLockInterval returns Immediately as default duration when value is not present`() = runTest {
        // Given
        every { preferences[autoLockIntervalKey] } returns null
        val expected = LegacyAutoLockIntervalPreference(0.seconds).right()

        // When
        val result = dataSource.getAutoLockInterval()

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `getAutoLockInterval returns error when value fails decrypting`() = runTest {
        // Given
        every { preferences[autoLockIntervalKey] } returns "encryptedInterval"
        coEvery { keyStoreCrypto.decrypt("encryptedInterval") } throws IOException("decryption failed")
        val expected = MigrationError.AutoLockFailure.FailedToDecryptAutoLockInterval.left()

        // When
        val result = dataSource.getAutoLockInterval()

        // Then
        assertEquals(expected, result)
    }

}
