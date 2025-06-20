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

package ch.protonmail.android.legacymigration.domain.usecase

import ch.protonmail.android.legacymigration.domain.repository.LegacyAutoLockRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class ClearLegacyAutoLockPreferencesTest {

    private val legacyAutoLockRepository = mockk<LegacyAutoLockRepository>()
    private lateinit var clearLegacyAutoLockPreferences: ClearLegacyAutoLockPreferences

    @BeforeTest
    fun setup() {
        clearLegacyAutoLockPreferences = ClearLegacyAutoLockPreferences(legacyAutoLockRepository)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should proxy the call via the repository`() = runTest {
        // Given
        coEvery { legacyAutoLockRepository.clearPreferences() } just runs

        // When
        clearLegacyAutoLockPreferences.invoke()

        // Then
        coVerify { legacyAutoLockRepository.clearPreferences() }
        confirmVerified(legacyAutoLockRepository)
    }
}
