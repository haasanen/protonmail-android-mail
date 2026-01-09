/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailtrackingprotection.presentation.mapper

import ch.protonmail.android.mailtrackingprotection.domain.model.BlockedTracker
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedTrackerUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TrackersUiModelMapperTest {

    @Test
    fun `toUiModel converts non-empty list of trackers correctly`() {
        // Given
        val trackers = listOf(
            BlockedTracker("tracker1.com", listOf("https://tracker1.com/pixel1", "https://tracker1.com/pixel2")),
            BlockedTracker("tracker2.com", listOf("https://tracker2.com/pixel"))
        )
        val expectedItems = persistentListOf(
            BlockedTrackerUiModel("tracker1.com", listOf("https://tracker1.com/pixel1", "https://tracker1.com/pixel2")),
            BlockedTrackerUiModel("tracker2.com", listOf("https://tracker2.com/pixel"))
        )

        // When
        val result = TrackersUiModelMapper.toUiModel(trackers)

        // Then
        assertTrue(result.trackers.isExpandable)
        assertEquals(expectedItems, result.trackers.items)
        assertFalse(result.links.isExpandable)
        assertTrue(result.links.items.isEmpty())
    }

    @Test
    fun `toUiModel converts empty list of trackers correctly`() {
        // Given
        val trackers = emptyList<BlockedTracker>()

        // When
        val result = TrackersUiModelMapper.toUiModel(trackers)

        // Then
        assertFalse(result.trackers.isExpandable)
        assertTrue(result.trackers.items.isEmpty())
        assertFalse(result.links.isExpandable)
        assertTrue(result.links.items.isEmpty())
    }

    @Test
    fun `toUiModel handles single tracker correctly`() {
        // Given
        val trackers = listOf(
            BlockedTracker("single-tracker.com", listOf("https://single-tracker.com/pixel"))
        )
        val expectedItems = persistentListOf(
            BlockedTrackerUiModel("single-tracker.com", listOf("https://single-tracker.com/pixel"))
        )

        // When
        val result = TrackersUiModelMapper.toUiModel(trackers)

        // Then
        assertTrue(result.trackers.isExpandable)
        assertEquals(expectedItems, result.trackers.items)
    }

    @Test
    fun `toUiModel handles tracker with empty urls list`() {
        // Given
        val trackers = listOf(
            BlockedTracker("empty-urls-tracker.com", emptyList())
        )
        val expectedItems = persistentListOf(
            BlockedTrackerUiModel("empty-urls-tracker.com", emptyList())
        )

        // When
        val result = TrackersUiModelMapper.toUiModel(trackers)

        // Then
        assertTrue(result.trackers.isExpandable)
        assertEquals(expectedItems, result.trackers.items)
    }
}
