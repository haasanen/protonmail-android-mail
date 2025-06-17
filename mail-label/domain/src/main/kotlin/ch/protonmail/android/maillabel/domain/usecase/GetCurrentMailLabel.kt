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

package ch.protonmail.android.maillabel.domain.usecase

import java.util.concurrent.ConcurrentHashMap
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabels
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetCurrentMailLabel @Inject constructor(
    private val observeMailLabels: ObserveMailLabels,
    private val getSelectedMailLabelId: GetSelectedMailLabelId
) {
    private val labelCache = ConcurrentHashMap<UserId, MailLabels>()

    suspend operator fun invoke(userId: UserId): MailLabel? {
        val selectedId = getSelectedMailLabelId()

        val cached = labelCache[userId]?.allById?.get(selectedId)
        if (cached != null) {
            return cached
        }

        // Fetch and cache if not found
        val mailLabels = observeMailLabels(userId).first()
        labelCache[userId] = mailLabels
        return mailLabels.allById[selectedId]
    }
}
