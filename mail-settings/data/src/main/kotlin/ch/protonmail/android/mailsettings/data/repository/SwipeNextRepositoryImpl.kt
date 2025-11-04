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

package ch.protonmail.android.mailsettings.data.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsettings.data.local.SwipeNextDataSource
import ch.protonmail.android.mailsettings.domain.model.SwipeNextPreference
import ch.protonmail.android.mailsettings.domain.repository.SwipeNextRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class SwipeNextRepositoryImpl @Inject constructor(
    private val dataSource: SwipeNextDataSource
) : SwipeNextRepository {

    private val restartTrigger = MutableSharedFlow<Unit>(replay = 1)

    override suspend fun observeSwipeNext(userId: UserId): Flow<Either<DataError, SwipeNextPreference>> = restartTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                val result = dataSource.getSwipeNext(userId)
                emit(result)
            }
        }

    override suspend fun getSwipeNext(userId: UserId): Either<DataError, SwipeNextPreference> =
        dataSource.getSwipeNext(userId)

    override suspend fun setSwipeNextEnabled(userId: UserId, enabled: Boolean): Either<DataError, Unit> =
        dataSource.setSwipeNextEnabled(userId, enabled).onRight {
            restartTrigger.emit(Unit)
        }
}
