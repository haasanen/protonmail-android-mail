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

package ch.protonmail.android.mailtrackingprotection.presentation

import ch.protonmail.android.mailtrackingprotection.domain.model.BlockedTracker
import ch.protonmail.android.mailtrackingprotection.domain.model.CleanedLink
import ch.protonmail.android.mailtrackingprotection.presentation.model.TrackersUiModel

object TrackersUiModelSample {

    private val blockedTracker = BlockedTracker(
        "tracker.com",
        listOf(
            "tracker.com/track1",
            "tracker.com/track2",
            "tracker.com/track3"
        )
    )

    @Suppress("MaxLineLength")
    private val blockedTracker1 = BlockedTracker(
        "tracker1.com",
        listOf(
            "tracker1.com/track1",
            "tracker.com/Lorem%20ipsum%20is%20a%20/dummy%20or%20placeholder%20text%20/commonly%20used%20in%20graphic%20design%2C%20/publishing%2C%20and%20web%20development.%20Its%20purpose%20is%20to%20permit%20a%20page%20layout%20to%20be%20designed%2C%20independently%20of%20the%20copy%20that%20will%20subsequently%20populate%20it%2C%20or%20to%20demonstrate%20various%20fonts%20of%20a%20typeface%20without%20meaningful%20text%20that%20could%20be%20distracting.%20Lorem%20ipsum%20is%20typically%20a%20corrupted%20version%20of%20De%20finibus%20bonorum%20et%20malorum%2C%20a%201st-century%20BC%20text%20by%20the%20Roman%20statesman%20and%20philosopher%20Cicero%2C%20with%20words%20altered%2C%20added%2C%20and%20removed%20to%20make%20it%20nonsensical%20and%20improper%20Latin"
        )
    )

    private val cleanedLink = CleanedLink(
        "https://website.com/page/tracker",
        "https://website.com/page"
    )

    val oneTrackerBlocked = TrackersUiModel(
        blocked = listOf(blockedTracker),
        links = emptyList()
    )

    val trackersAndLinks = TrackersUiModel(
        blocked = listOf(blockedTracker, blockedTracker1),
        links = listOf(cleanedLink)
    )

}

