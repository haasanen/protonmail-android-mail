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

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.local.LabelDataSource
import ch.protonmail.android.maillabel.data.mapper.toLabel
import ch.protonmail.android.maillabel.data.mapper.toLabelId
import ch.protonmail.android.maillabel.data.mapper.toLabelWithSystemLabelId
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.data.mapper.toLocalSystemLabel
import ch.protonmail.android.maillabel.data.mapper.toSystemLabel
import ch.protonmail.android.maillabel.domain.model.Label
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.model.LabelWithSystemLabelId
import ch.protonmail.android.maillabel.domain.model.NewLabel
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

@SuppressWarnings("NotImplementedDeclaration")
class RustLabelRepository @Inject constructor(
    private val labelDataSource: LabelDataSource
) : LabelRepository {

    @Deprecated(
        "Deprecated to ease the introduction of dynamic system folders",
        replaceWith = ReplaceWith(
            "One of labelRepository.observeCustomLabels || .observeCustomFolders || .observeSystemFolders"
        )
    )
    override fun observeLabels(
        userId: UserId,
        type: LabelType,
        refresh: Boolean
    ): Flow<DataResult<List<Label>>> {
        return when (type) {
            LabelType.SystemFolder -> labelDataSource.observeSystemLabels(userId).map { localLabels ->
                localLabels.map { it.toLabelWithSystemLabelId().label }
            }.convertToDataResultFlow()

            LabelType.MessageLabel -> labelDataSource.observeMessageLabels(userId).map { localLabels ->
                localLabels.map { it.toLabel() }
            }.convertToDataResultFlow()

            LabelType.MessageFolder -> labelDataSource.observeMessageFolders(userId).map { localLabels ->
                localLabels.map { it.toLabel() }
            }.convertToDataResultFlow()

            else -> {
                Timber.w("rust-label: LabelRepository Unsupported label type: $type")
                flowOf(DataResult.Error.Local("Unsupported label type $type", null))
            }
        }
    }

    override fun observeCustomLabels(userId: UserId): Flow<List<Label>> =
        labelDataSource.observeMessageLabels(userId).map { customLabels ->
            customLabels.map { it.toLabel() }
        }

    override fun observeCustomFolders(userId: UserId): Flow<List<Label>> =
        labelDataSource.observeMessageFolders(userId).map { customFolders ->
            customFolders.map { it.toLabel() }
        }

    override fun observeSystemLabels(userId: UserId): Flow<List<LabelWithSystemLabelId>> =
        labelDataSource.observeSystemLabels(userId).map { systemLabels ->
            systemLabels.map { it.toLabelWithSystemLabelId() }
        }

    override suspend fun getLabels(
        userId: UserId,
        type: LabelType,
        refresh: Boolean
    ): List<Label> {
        observeLabels(userId, type, refresh).firstOrNull().let {
            return if (it is DataResult.Success) {
                it.value
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getLabel(
        userId: UserId,
        type: LabelType,
        labelId: LabelId,
        refresh: Boolean
    ): Label? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllMailLocalLabelId(userId: UserId): LabelId? =
        labelDataSource.getAllMailLabelId(userId).fold(
            ifLeft = { null },
            ifRight = { it.toLabelId() }
        )

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

    override suspend fun resolveSystemLabel(userId: UserId, labelId: LabelId): Either<DataError, SystemLabelId> {
        return labelDataSource.resolveSystemLabelByLocalId(userId = userId, labelId = labelId.toLocalLabelId()).map {
            it.toSystemLabel()
        }
    }

    override suspend fun resolveLocalIdBySystemLabel(
        userId: UserId,
        labelId: SystemLabelId
    ): Either<DataError, LabelId> {
        return labelDataSource.resolveLocalIdBySystemLabel(userId = userId, systemLabel = labelId.toLocalSystemLabel())
            .map { it.toLabelId() }
    }

    private fun Flow<List<Label>>.convertToDataResultFlow(): Flow<DataResult<List<Label>>> {
        return this.map { labels ->
            if (labels.isNotEmpty()) {
                DataResult.Success(ResponseSource.Remote, labels)
            } else {
                DataResult.Error.Local("No Labels Found", null)
            }
        }
    }
}
