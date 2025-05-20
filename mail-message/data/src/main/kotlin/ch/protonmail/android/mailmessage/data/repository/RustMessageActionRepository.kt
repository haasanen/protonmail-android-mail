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

package ch.protonmail.android.mailmessage.data.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.AvailableActions
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.mailmessage.data.local.RustMessageDataSource
import ch.protonmail.android.mailmessage.data.mapper.toAllBottomBarActions
import ch.protonmail.android.mailmessage.data.mapper.toAvailableActions
import ch.protonmail.android.mailmessage.data.mapper.toLabelAsActions
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.data.mapper.toMailLabels
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageActionRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class RustMessageActionRepository @Inject constructor(
    private val rustMessageDataSource: RustMessageDataSource
) : MessageActionRepository {

    override suspend fun getAvailableActions(
        userId: UserId,
        labelId: LabelId,
        messageId: MessageId
    ): Either<DataError, AvailableActions> {
        val availableActions = rustMessageDataSource.getAvailableActions(
            userId,
            labelId.toLocalLabelId(),
            messageId.toLocalMessageId()
        )
        Timber.v("rust-message: Available actions: $availableActions \n for messages $messageId")

        return availableActions.map { it.toAvailableActions() }
    }

    override suspend fun getSystemMoveToLocations(
        userId: UserId,
        labelId: LabelId,
        messageIds: List<MessageId>
    ): Either<DataError, List<MailLabel.System>> {
        val moveToActions = rustMessageDataSource.getAvailableSystemMoveToActions(
            userId,
            labelId.toLocalLabelId(),
            messageIds.map { it.toLocalMessageId() }
        )

        return moveToActions.map { it.toMailLabels() }
    }

    override suspend fun getAvailableLabelAsActions(
        userId: UserId,
        labelId: LabelId,
        messageIds: List<MessageId>
    ): Either<DataError, LabelAsActions> {
        val labelAsActions = rustMessageDataSource.getAvailableLabelAsActions(
            userId,
            labelId.toLocalLabelId(),
            messageIds.map { it.toLocalMessageId() }
        )

        return labelAsActions.map { it.toLabelAsActions() }
    }

    override suspend fun getAllBottomBarActions(
        userId: UserId,
        labelId: LabelId,
        messageIds: List<MessageId>
    ): Either<DataError, AllBottomBarActions> {
        val allActions = rustMessageDataSource.getAllAvailableBottomBarActions(
            userId,
            labelId.toLocalLabelId(),
            messageIds.map { it.toLocalMessageId() }
        )
        Timber.v("rust-message: All bottombar actions: $allActions \n for messages $messageIds")

        return allActions.map { it.toAllBottomBarActions() }
    }
}
