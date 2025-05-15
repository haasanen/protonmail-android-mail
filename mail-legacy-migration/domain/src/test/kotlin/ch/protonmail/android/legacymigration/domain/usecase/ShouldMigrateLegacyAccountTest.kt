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

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShouldMigrateLegacyAccountTest {

    private val hasStoredAccount: HasStoredAccount = mockk()
    private val hasLegacyLoggedInAccount: HasLegacyLoggedInAccounts = mockk()

    private val useCase = ShouldMigrateLegacyAccount(hasStoredAccount, hasLegacyLoggedInAccount)

    @Test
    fun `returns true when no stored account and legacy account is logged in`() = runTest {
        // Given
        coEvery { hasStoredAccount() } returns false
        coEvery { hasLegacyLoggedInAccount() } returns true

        // When
        val result = useCase()

        // Then
        assertTrue(result)
    }

    @Test
    fun `returns false when stored account exists`() = runTest {
        // Given
        coEvery { hasStoredAccount() } returns true
        coEvery { hasLegacyLoggedInAccount() } returns true

        // When
        val result = useCase()

        // Then
        assertFalse(result)
    }

    @Test
    fun `returns false when no legacy account is logged in`() = runTest {
        // Given
        coEvery { hasStoredAccount() } returns false
        coEvery { hasLegacyLoggedInAccount() } returns false

        // When
        val result = useCase()

        // Then
        assertFalse(result)
    }

}
