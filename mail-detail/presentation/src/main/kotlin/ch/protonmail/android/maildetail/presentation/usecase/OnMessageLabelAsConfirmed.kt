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

package ch.protonmail.android.maildetail.presentation.usecase

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maildetail.domain.usecase.LabelMessage
import ch.protonmail.android.maillabel.presentation.model.LabelSelectedState
import ch.protonmail.android.maillabel.presentation.model.LabelUiModelWithSelectedState
import ch.protonmail.android.mailmessage.domain.model.LabelSelectionList
import ch.protonmail.android.mailmessage.domain.model.MessageId
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class OnMessageLabelAsConfirmed @Inject constructor(
    private val labelMessage: LabelMessage
) {

    suspend operator fun invoke(
        userId: UserId,
        messageId: MessageId,
        labelUiModelsWithSelectedState: List<LabelUiModelWithSelectedState>,
        archiveSelected: Boolean
    ): Either<DataError, Unit> {
        val selectedLabels = labelUiModelsWithSelectedState
            .filter { it.selectedState == LabelSelectedState.Selected }
            .map { it.labelUiModel.id.labelId }
        val partiallySelectedLabels = labelUiModelsWithSelectedState
            .filter { it.selectedState == LabelSelectedState.PartiallySelected }
            .map { it.labelUiModel.id.labelId }

        return labelMessage(
            userId = userId,
            messageId = messageId,
            updatedSelection = LabelSelectionList(selectedLabels, partiallySelectedLabels),
            shouldArchive = archiveSelected
        )
    }
}
