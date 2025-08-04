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

package ch.protonmail.android.mailupselling.domain

import ch.protonmail.android.mailupselling.domain.cache.AvailableUpgradesCache
import ch.protonmail.android.mailupselling.domain.usecase.ResetPlanUpgradesCache
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

internal class ResetPlanUpgradesCacheTest {

    private val upgradesCache = mockk<AvailableUpgradesCache>()
    private val resetUpgradesCache = ResetPlanUpgradesCache(upgradesCache)

    @Test
    fun `should forward the call to the upgrade cache`() = runTest {
        // Given
        every { upgradesCache.invalidateAll() } just runs

        // When
        resetUpgradesCache()

        // Then
        verify(exactly = 1) { upgradesCache.invalidateAll() }
        confirmVerified(upgradesCache)
    }
}
