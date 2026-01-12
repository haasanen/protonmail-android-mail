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

package ch.protonmail.android.mailtrackingprotection.presentation.model

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Stable
data class BlockedElementsUiModel(
    val trackers: TrackersUiModel,
    val links: CleanedLinksUiModel
)

@Stable
data class TrackersUiModel(
    val items: ImmutableList<BlockedTrackerUiModel>,
    val isExpandable: Boolean
) {

    companion object {

        val Empty = TrackersUiModel(persistentListOf(), false)
    }
}

@Stable
data class BlockedTrackerUiModel(val domain: String, val urls: List<String>)

@Stable
data class CleanedLinksUiModel(
    val items: ImmutableList<CleanedLinkUiModel>,
    val isExpandable: Boolean
)

@Stable
data class CleanedLinkUiModel(
    val original: OriginalLinkValue,
    val cleaned: CleanedLinkValue
)

@JvmInline
value class OriginalLinkValue(val value: String)

@JvmInline
value class CleanedLinkValue(val value: String)

