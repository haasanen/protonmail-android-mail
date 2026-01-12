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

import ch.protonmail.android.mailtrackingprotection.domain.model.BlockedPrivacyItems
import ch.protonmail.android.mailtrackingprotection.domain.model.BlockedTracker
import ch.protonmail.android.mailtrackingprotection.domain.model.CleanedLink
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedTrackerUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinkUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinkValue
import ch.protonmail.android.mailtrackingprotection.presentation.model.OriginalLinkValue
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BlockedElementsUiModelMapperTest {

    @Test
    fun `toUiModel converts non-empty list of trackers correctly`() {
        // Given
        val trackers = listOf(
            BlockedTracker("tracker1.com", listOf("https://tracker1.com/pixel1", "https://tracker1.com/pixel2")),
            BlockedTracker("tracker2.com", listOf("https://tracker2.com/pixel"))
        )
        val privacyItems = BlockedPrivacyItems(trackers = trackers, urls = emptyList())

        val expectedItems = persistentListOf(
            BlockedTrackerUiModel("tracker1.com", listOf("https://tracker1.com/pixel1", "https://tracker1.com/pixel2")),
            BlockedTrackerUiModel("tracker2.com", listOf("https://tracker2.com/pixel"))
        )

        // When
        val result = BlockedElementsUiModelMapper.toUiModel(privacyItems)

        // Then
        assertTrue(result.trackers.isExpandable)
        assertEquals(expectedItems, result.trackers.items)
        assertFalse(result.links.isExpandable)
        assertTrue(result.links.items.isEmpty())
    }

    @Test
    fun `toUiModel converts empty privacy items correctly`() {
        // Given
        val privacyItems = BlockedPrivacyItems(trackers = emptyList(), urls = emptyList())

        // When
        val result = BlockedElementsUiModelMapper.toUiModel(privacyItems)

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
        val privacyItems = BlockedPrivacyItems(trackers = trackers, urls = emptyList())

        val expectedItems = persistentListOf(
            BlockedTrackerUiModel("single-tracker.com", listOf("https://single-tracker.com/pixel"))
        )

        // When
        val result = BlockedElementsUiModelMapper.toUiModel(privacyItems)

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
        val privacyItems = BlockedPrivacyItems(trackers = trackers, urls = emptyList())

        val expectedItems = persistentListOf(
            BlockedTrackerUiModel("empty-urls-tracker.com", emptyList())
        )

        // When
        val result = BlockedElementsUiModelMapper.toUiModel(privacyItems)

        // Then
        assertTrue(result.trackers.isExpandable)
        assertEquals(expectedItems, result.trackers.items)
    }

    @Test
    fun `toUiModel converts non-empty list of cleaned links correctly`() {
        // Given
        val cleanedLinks = listOf(
            CleanedLink("https://example.com?utm_source=test", "https://example.com"),
            CleanedLink("https://other.com?utm_campaign=promo", "https://other.com")
        )
        val privacyItems = BlockedPrivacyItems(trackers = emptyList(), urls = cleanedLinks)

        val expectedItems = persistentListOf(
            CleanedLinkUiModel(
                OriginalLinkValue("https://example.com?utm_source=test"),
                CleanedLinkValue("https://example.com")
            ),
            CleanedLinkUiModel(
                OriginalLinkValue("https://other.com?utm_campaign=promo"),
                CleanedLinkValue("https://other.com")
            )
        )

        // When
        val result = BlockedElementsUiModelMapper.toUiModel(privacyItems)

        // Then
        assertFalse(result.trackers.isExpandable)
        assertTrue(result.trackers.items.isEmpty())
        assertTrue(result.links.isExpandable)
        assertEquals(expectedItems, result.links.items)
    }

    @Test
    fun `toUiModel handles single cleaned link correctly`() {
        // Given
        val cleanedLinks = listOf(
            CleanedLink("https://example.com?utm_source=test", "https://example.com")
        )
        val privacyItems = BlockedPrivacyItems(trackers = emptyList(), urls = cleanedLinks)

        val expectedItems = persistentListOf(
            CleanedLinkUiModel(
                OriginalLinkValue("https://example.com?utm_source=test"),
                CleanedLinkValue("https://example.com")
            )
        )

        // When
        val result = BlockedElementsUiModelMapper.toUiModel(privacyItems)

        // Then
        assertTrue(result.links.isExpandable)
        assertEquals(expectedItems, result.links.items)
    }

    @Test
    fun `toUiModel converts both trackers and links correctly`() {
        // Given
        val trackers = listOf(
            BlockedTracker("tracker.com", listOf("https://tracker.com/pixel"))
        )
        val cleanedLinks = listOf(
            CleanedLink("https://example.com?utm_source=test", "https://example.com")
        )
        val privacyItems = BlockedPrivacyItems(trackers = trackers, urls = cleanedLinks)

        val expectedTrackers = persistentListOf(
            BlockedTrackerUiModel("tracker.com", listOf("https://tracker.com/pixel"))
        )
        val expectedLinks = persistentListOf(
            CleanedLinkUiModel(
                OriginalLinkValue("https://example.com?utm_source=test"),
                CleanedLinkValue("https://example.com")
            )
        )

        // When
        val result = BlockedElementsUiModelMapper.toUiModel(privacyItems)

        // Then
        assertTrue(result.trackers.isExpandable)
        assertEquals(expectedTrackers, result.trackers.items)
        assertTrue(result.links.isExpandable)
        assertEquals(expectedLinks, result.links.items)
    }
}
