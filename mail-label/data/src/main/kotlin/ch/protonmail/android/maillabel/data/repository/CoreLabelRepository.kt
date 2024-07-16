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

import ch.protonmail.android.maillabel.domain.model.LabelWithSystemLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.mapSuccessValueOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.entity.NewLabel
import javax.inject.Inject
import me.proton.core.label.domain.repository.LabelRepository as CoreLibsLabelRepository

@Deprecated("Core Libs are deprecated in favor of rust lib", ReplaceWith("RustLabelRepository"))
class CoreLabelRepository @Inject constructor(
    private val labelRepository: CoreLibsLabelRepository
) : LabelRepository {

    override fun observeLabels(
        userId: UserId,
        type: LabelType,
        refresh: Boolean
    ): Flow<DataResult<List<Label>>> = labelRepository.observeLabels(userId, type, refresh)

    override fun observeCustomLabels(userId: UserId): Flow<List<Label>> =
        labelRepository.observeLabels(userId, LabelType.MessageLabel, false)
            .mapSuccessValueOrNull()
            .mapLatest { it.orEmpty() }

    override fun observeCustomFolders(userId: UserId): Flow<List<Label>> =
        labelRepository.observeLabels(userId, LabelType.MessageFolder, false)
            .mapSuccessValueOrNull()
            .mapLatest { it.orEmpty() }

    override fun observeSystemLabels(userId: UserId): Flow<List<LabelWithSystemLabelId>> =
        labelRepository.observeLabels(userId, LabelType.SystemFolder, false)
            .mapSuccessValueOrNull()
            .mapLatest { labels ->
                labels?.map {
                    LabelWithSystemLabelId(
                        it,
                        SystemLabelId.enumOf(it.labelId.id)
                    )
                }.orEmpty()
            }

    override suspend fun getLabels(
        userId: UserId,
        type: LabelType,
        refresh: Boolean
    ): List<Label> = labelRepository.getLabels(userId, type, refresh)

    override suspend fun getLabel(
        userId: UserId,
        type: LabelType,
        labelId: LabelId,
        refresh: Boolean
    ): Label? = labelRepository.getLabel(userId, type, labelId, refresh)

    override suspend fun createLabel(userId: UserId, label: NewLabel) = labelRepository.createLabel(userId, label)

    override suspend fun updateLabel(userId: UserId, label: Label) = labelRepository.updateLabel(userId, label)

    override suspend fun updateLabelIsExpanded(
        userId: UserId,
        type: LabelType,
        labelId: LabelId,
        isExpanded: Boolean
    ) = labelRepository.updateLabelIsExpanded(userId, type, labelId, isExpanded)

    override suspend fun deleteLabel(
        userId: UserId,
        type: LabelType,
        labelId: LabelId
    ) = labelRepository.deleteLabel(userId, type, labelId)

    override fun markAsStale(userId: UserId, type: LabelType) = labelRepository.markAsStale(userId, type)
}
