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

package ch.protonmail.android.legacymigration.data.local

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import ch.protonmail.android.legacymigration.domain.model.LegacySignaturePreference
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import java.io.IOException
import ch.protonmail.android.legacymigration.data.local.signature.LegacySignatureDataStoreProvider
import ch.protonmail.android.legacymigration.data.local.signature.LegacySignatureLocalDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import me.proton.core.user.domain.entity.AddressId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LegacySignatureLocalDataSourceImplTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val preferences = mockk<Preferences>()
    private val dataStore = mockk<DataStore<Preferences>> {
        every { data } returns flowOf(preferences)
    }

    private val dataStoreProvider = mockk<LegacySignatureDataStoreProvider> {
        every { legacySignatureDataStore } returns dataStore
    }

    private val dataSource = LegacySignatureLocalDataSourceImpl(
        dataStoreProvider, CoroutineScope(coroutineRule.testDispatcher)
    )

    private val addressId = AddressId("test-address")
    // Test-local replica of the (private) key naming in the impl:
    private val enabledKey = LegacySignatureLocalDataSourceImpl.getSignatureEnabledPrefKey(addressId)

    @Test
    fun `getSignaturePreference returns preference when preference is available`() = runTest {
        // Given
        every { preferences[enabledKey] } returns true

        // When
        val result = dataSource.getSignaturePreference(addressId)

        // Then
        assertEquals(LegacySignaturePreference(isEnabled = true).right(), result)
    }

    @Test
    fun `getSignaturePreference returns error when enabled key is missing`() = runTest {
        // Given
        every { preferences[enabledKey] } returns null

        // When
        val result = dataSource.getSignaturePreference(addressId)

        // Then
        assertEquals(
            MigrationError.SignatureFailure.FailedToReadMobileSignaturePreference.left(),
            result
        )
    }

    @Test
    fun `getSignaturePreference returns error when exception is thrown`() = runTest {
        // Given
        every { dataStore.data } returns flow { throw IOException("IO Exception") }

        // When
        val result = dataSource.getSignaturePreference(addressId)

        // Then
        assertEquals(
            MigrationError.SignatureFailure.FailedToReadMobileSignaturePreference.left(),
            result
        )
    }
}
