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

import java.io.IOException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.data.local.signature.mobile.LegacyMobileSignatureDataStoreProvider
import ch.protonmail.android.legacymigration.data.local.signature.mobile.LegacyMobileSignatureLocalDataSourceImpl
import ch.protonmail.android.legacymigration.domain.model.LegacyMobileSignaturePreference
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import me.proton.core.domain.entity.UserId
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class LegacyMobileSignatureLocalDataSourceImplTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val preferences = mockk<Preferences>()
    private val dataStore = mockk<DataStore<Preferences>> {
        every { data } returns flowOf(preferences)
    }

    private val dataStoreProvider = mockk<LegacyMobileSignatureDataStoreProvider> {
        every { legacyMobileSignatureDataStore } returns dataStore
    }

    private val dataSource = LegacyMobileSignatureLocalDataSourceImpl(
        dataStoreProvider,
        CoroutineScope(coroutineRule.testDispatcher)
    )

    private val userId = UserId("test-user")
    private val enabledKey = LegacyMobileSignatureLocalDataSourceImpl.getMobileFooterEnabledPrefKey(userId)
    private val valueKey = LegacyMobileSignatureLocalDataSourceImpl.getMobileFooterValuePrefKey(userId)

    @Test
    fun `getMobileSignaturePreference returns preference when both keys are present`() = runTest {
        // Given
        val signatureValue = "Sent from Proton"
        every { preferences[enabledKey] } returns true
        every { preferences[valueKey] } returns signatureValue

        // When
        val result = dataSource.getMobileSignaturePreference(userId)

        // Then
        assertEquals(
            LegacyMobileSignaturePreference(signatureValue, true).right(),
            result
        )
    }

    @Test
    fun `getMobileSignaturePreference returns error when enabled key is missing`() = runTest {
        // Given
        every { preferences[enabledKey] } returns null
        every { preferences[valueKey] } returns "Some Signature"

        // When
        val result = dataSource.getMobileSignaturePreference(userId)

        // Then
        assertEquals(
            MigrationError.SignatureFailure.FailedToReadMobileSignaturePreference.left(),
            result
        )
    }

    @Test
    fun `getMobileSignaturePreference returns error when value key is missing`() = runTest {
        // Given
        every { preferences[enabledKey] } returns true
        every { preferences[valueKey] } returns null

        // When
        val result = dataSource.getMobileSignaturePreference(userId)

        // Then
        assertEquals(
            MigrationError.SignatureFailure.FailedToReadMobileSignaturePreference.left(),
            result
        )
    }

    @Test
    fun `getMobileSignaturePreference returns error when exception is thrown`() = runTest {
        // Given
        every { dataStore.data } returns flow { throw IOException("IO Exception") }

        // When
        val result = dataSource.getMobileSignaturePreference(userId)

        // Then
        assertEquals(
            MigrationError.SignatureFailure.FailedToReadMobileSignaturePreference.left(),
            result
        )
    }
}
