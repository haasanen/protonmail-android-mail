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

package ch.protonmail.android.mailupselling.presentation.usecase

import arrow.core.right
import ch.protonmail.android.mailupselling.domain.model.BlackFridayPhase
import ch.protonmail.android.mailupselling.domain.repository.BlackFridayRepository
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class SaveBlackFridayModalSeenTest {

    private val repository = mockk<BlackFridayRepository>()
    private lateinit var saveBlackFridayModalSeen: SaveBlackFridayModalSeen

    @BeforeTest
    fun setup() {
        saveBlackFridayModalSeen = SaveBlackFridayModalSeen(repository)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should proxy the call to the repository`() = runTest {
        // Given
        val phase = BlackFridayPhase.Active.Wave1
        coEvery { repository.saveSeen(any()) } returns Unit.right()

        // When
        saveBlackFridayModalSeen(phase)

        // Then
        coVerify(exactly = 1) { repository.saveSeen(phase) }
        confirmVerified(repository)
    }
}
