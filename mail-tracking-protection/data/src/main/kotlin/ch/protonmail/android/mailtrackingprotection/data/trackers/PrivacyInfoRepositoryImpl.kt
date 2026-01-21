/*
* Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailtrackingprotection.data.trackers

import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.mapper.toLocalMessageId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailtrackingprotection.data.mapper.toDomainPrivacyInfo
import ch.protonmail.android.mailtrackingprotection.data.wrapper.PrivacyInfoState
import ch.protonmail.android.mailtrackingprotection.data.wrapper.RustPrivacyInfoWrapper
import ch.protonmail.android.mailtrackingprotection.domain.model.PrivacyItemsResult
import ch.protonmail.android.mailtrackingprotection.domain.repository.PrivacyInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class PrivacyInfoRepositoryImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val dataSource: RustPrivacyInfoDataSource
) : PrivacyInfoRepository {

    override fun observePrivacyItemsForMessage(
        userId: UserId,
        messageId: MessageId
    ): Flow<Either<DataError, PrivacyItemsResult>> = flow {
        val userSession = userSessionRepository.getUserSession(userId) ?: run {
            emit(DataError.Local.NoUserSession.left())
            return@flow
        }

        val trackersWrapper = RustPrivacyInfoWrapper(userSession.getRustUserSession())

        emitAll(
            dataSource.observePrivacyInfo(trackersWrapper, messageId.toLocalMessageId()).map { either ->
                either.map { privacyInfoState -> privacyInfoState.toDomainResult() }
            }
        )
    }.distinctUntilChanged()

    private fun PrivacyInfoState.toDomainResult(): PrivacyItemsResult = when (this) {
        is PrivacyInfoState.Pending -> PrivacyItemsResult.Pending
        is PrivacyInfoState.Detected -> PrivacyItemsResult.Detected(info.toDomainPrivacyInfo())
        is PrivacyInfoState.Disabled -> PrivacyItemsResult.Disabled
    }
}

