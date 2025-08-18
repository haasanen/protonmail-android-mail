/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsettings.domain.usecase

import ch.protonmail.android.mailsession.domain.model.Percent
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsettings.domain.model.StorageQuota
import ch.protonmail.android.mailsettings.domain.model.StorageQuotaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class ObserveStorageQuotaUseCase @Inject constructor(
    private val userSessionRepository: UserSessionRepository
) {

    operator fun invoke(): Flow<StorageQuotaResult> =
        userSessionRepository.observePrimaryUserId().flatMapLatest { userId: UserId? ->
            userId ?: return@flatMapLatest flowOf(StorageQuotaResult.Error.FailedToRetrievePrimaryUserId)

            userSessionRepository.observeUser(userId).map { userResult ->
                userResult.fold(
                    ifLeft = { StorageQuotaResult.Error.FailedToRetrieveUser },
                    ifRight = { user ->
                        StorageQuotaResult.Success(
                            StorageQuota(
                                usagePercent = user.usagePercent,
                                maxStorage = user.maxStorage,
                                isAboveAlertThreshold = user.usagePercent >= STORAGE_ALERT_THRESHOLD
                            )
                        )
                    }
                )
            }
        }
}

private val STORAGE_ALERT_THRESHOLD: Percent = Percent(80.0)
