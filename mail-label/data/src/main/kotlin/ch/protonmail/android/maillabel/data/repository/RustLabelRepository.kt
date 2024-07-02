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

package ch.protonmail.android.maillabel.data.repository

import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.entity.NewLabel

@SuppressWarnings("NotImplementedDeclaration")
class RustLabelRepository : LabelRepository {

    override fun observeLabels(
        userId: UserId,
        type: LabelType,
        refresh: Boolean
    ): Flow<DataResult<List<Label>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLabels(
        userId: UserId,
        type: LabelType,
        refresh: Boolean
    ): List<Label> {
        TODO("Not yet implemented")
    }

    override suspend fun getLabel(
        userId: UserId,
        type: LabelType,
        labelId: LabelId,
        refresh: Boolean
    ): Label? {
        TODO("Not yet implemented")
    }

    override suspend fun createLabel(userId: UserId, label: NewLabel) {
        TODO("Not yet implemented")
    }

    override suspend fun updateLabel(userId: UserId, label: Label) {
        TODO("Not yet implemented")
    }

    override suspend fun updateLabelIsExpanded(
        userId: UserId,
        type: LabelType,
        labelId: LabelId,
        isExpanded: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLabel(
        userId: UserId,
        type: LabelType,
        labelId: LabelId
    ) {
        TODO("Not yet implemented")
    }

    override fun markAsStale(userId: UserId, type: LabelType) {
        TODO("Not yet implemented")
    }
}
