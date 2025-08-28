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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.Either
import arrow.core.flatMap
import ch.protonmail.android.mailcommon.domain.model.AllBottomBarActions
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions
import ch.protonmail.android.mailmessage.domain.repository.MessageActionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class ObserveAllMessageBottomBarActions @Inject constructor(
    private val actionRepository: MessageActionRepository,
    private val observeMessage: ObserveMessage
) {

    suspend operator fun invoke(
        userId: UserId,
        labelId: LabelId,
        messageId: MessageId,
        themeOptions: MessageThemeOptions
    ): Flow<Either<DataError, AllBottomBarActions>> = observeMessage(userId, messageId)
        .mapLatest { messageEither ->
            messageEither
                .flatMap {
                    actionRepository.getAllBottomBarActions(userId, labelId, messageId, themeOptions)
                }
        }
}
