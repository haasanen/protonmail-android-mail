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

import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import ch.protonmail.android.maillabel.domain.model.LabelId
import javax.inject.Inject

/**
 * Returns the list of local labelIds where only messages can be displayed
 */
class ObserveMessageOnlyLabelIds @Inject constructor(
    private val labelRepository: LabelRepository
) {

    private val messagesOnlyLabelsIds = listOf(
        SystemLabelId.Drafts,
        SystemLabelId.AllDrafts,
        SystemLabelId.Sent,
        SystemLabelId.AllSent
    ).map { it.labelId }

    operator fun invoke(userId: UserId): Flow<List<LabelId>> = labelRepository.observeSystemLabels(userId)
        .mapLatest { systemLabels ->
            systemLabels
                .filter { it.systemLabelId.labelId in messagesOnlyLabelsIds }
                .map { it.label.labelId }

        }
}
