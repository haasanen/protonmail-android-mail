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

package ch.protonmail.android.mailmailbox.presentation.usecase

import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import ch.protonmail.android.mailmailbox.domain.repository.InMemoryMailboxRepository
import ch.protonmail.android.mailmailbox.presentation.mailbox.usecase.ShouldShowRatingBooster
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class ShouldShowRatingBoosterTest {

    private val inMemoryMailboxRepository: InMemoryMailboxRepository = mockk()
    private val showRatingBooster: FeatureFlag<Boolean> = mockk()
    private lateinit var shouldShowRatingBooster: ShouldShowRatingBooster

    private val testUserId = UserId("user-123")

    private val threshold = ShouldShowRatingBooster.SCREEN_VIEW_COUNT_THRESHOLD // 2

    @Before
    fun setUp() {
        shouldShowRatingBooster = ShouldShowRatingBooster(
            inMemoryMailboxRepository,
            showRatingBooster
        )
    }

    @Test
    fun `when flag is enabled and screen count is below threshold then show is false`() = runTest {
        // Given
        val screenViewCount = threshold - 1 // 1
        every { inMemoryMailboxRepository.observeScreenViewCount() } returns flowOf(screenViewCount)
        coEvery { showRatingBooster.get() } returns true

        // When
        val result = shouldShowRatingBooster(testUserId).toList(destination = mutableListOf())

        // Then
        assertEquals(listOf(false), result)
    }

    @Test
    fun `when flag is enabled and screen count is at threshold then show is true`() = runTest {
        // Given
        val screenViewCount = threshold // 2
        every { inMemoryMailboxRepository.observeScreenViewCount() } returns flowOf(screenViewCount)
        coEvery { showRatingBooster.get() } returns true

        // When
        val result = shouldShowRatingBooster(testUserId).toList(destination = mutableListOf())

        // Then
        assertEquals(listOf(true), result)
    }

    @Test
    fun `when flag is disabled and screen count is above threshold then show is false`() = runTest {
        // Given
        val screenViewCount = threshold + 1 // 3
        every { inMemoryMailboxRepository.observeScreenViewCount() } returns flowOf(screenViewCount)
        coEvery { showRatingBooster.get() } returns false

        // When
        val result = shouldShowRatingBooster(testUserId).toList(destination = mutableListOf())

        // Then
        assertEquals(listOf(false), result)
    }

    @Test
    fun `when flow emits multiple counts then the output flow correctly maps each value`() = runTest {
        // Given
        val screenViewCounts = flowOf(1, 2, 0, 2)
        every { inMemoryMailboxRepository.observeScreenViewCount() } returns screenViewCounts
        coEvery { showRatingBooster.get() } returns true

        // When
        val result = shouldShowRatingBooster(testUserId).toList(destination = mutableListOf())

        // Then
        val expected = listOf(false, true, false, true)
        assertEquals(expected, result)
    }
}
