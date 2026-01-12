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
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedElementsUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.BlockedTrackerUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinkUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinkValue
import ch.protonmail.android.mailtrackingprotection.presentation.model.CleanedLinksUiModel
import ch.protonmail.android.mailtrackingprotection.presentation.model.OriginalLinkValue
import ch.protonmail.android.mailtrackingprotection.presentation.model.TrackersUiModel
import kotlinx.collections.immutable.toImmutableList

internal object BlockedElementsUiModelMapper {

    fun toUiModel(items: BlockedPrivacyItems): BlockedElementsUiModel {
        val trackersUiModel = TrackersUiModel(
            items = items.trackers.map { it.toUiModel() }.toImmutableList(),
            isExpandable = items.trackers.isNotEmpty()
        )

        val linksUiModel = CleanedLinksUiModel(
            items = items.urls.map { it.toUiModel() }.toImmutableList(),
            isExpandable = items.urls.isNotEmpty()
        )

        return BlockedElementsUiModel(trackersUiModel, linksUiModel)
    }

    private fun BlockedTracker.toUiModel() = BlockedTrackerUiModel(domain, urls)

    private fun CleanedLink.toUiModel(): CleanedLinkUiModel {
        val originalValue = OriginalLinkValue(original)
        val cleanedValue = CleanedLinkValue(cleaned)
        return CleanedLinkUiModel(originalValue, cleanedValue)
    }
}
