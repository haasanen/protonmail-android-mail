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

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.model.NewLabel
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class CreateFolder @Inject constructor(private val labelRepository: LabelRepository) {

    suspend operator fun invoke(
        userId: UserId,
        name: String,
        color: String,
        parentId: LabelId?,
        notifications: Boolean
    ): Either<DataError, Unit> = Either.catch {
        labelRepository.createLabel(userId, buildNewLabel(name, color, parentId, notifications))
    }.mapLeft {
        when (it) {
            is UnknownHostException -> DataError.Remote.NoNetwork
            is SocketTimeoutException -> DataError.Remote.Unreachable
            else -> {
                Timber.e("Unknown error while creating folder: $it")
                DataError.Remote.Unknown
            }
        }
    }

    private fun buildNewLabel(
        name: String,
        color: String,
        parentId: LabelId?,
        notifications: Boolean
    ): NewLabel {
        return NewLabel(
            parentId = parentId,
            name = name,
            type = LabelType.MessageFolder,
            color = color,
            isNotified = notifications,
            isExpanded = null,
            isSticky = null
        )
    }
}
