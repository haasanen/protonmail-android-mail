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
import ch.protonmail.android.mailsettings.data.local.MobileSignatureDataSource
import ch.protonmail.android.mailsettings.domain.model.MobileSignaturePreference
import ch.protonmail.android.mailsettings.domain.repository.MobileSignatureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileSignatureRepositoryImpl @Inject constructor(
    private val dataSource: MobileSignatureDataSource
) : MobileSignatureRepository {

    private val restartTrigger = MutableSharedFlow<Unit>(replay = 1)

    override suspend fun getMobileSignature(userId: UserId): Either<DataError, MobileSignaturePreference> =
        dataSource.getMobileSignature(userId)

    override fun observeMobileSignature(userId: UserId): Flow<MobileSignaturePreference> =
        restartTrigger.flatMapLatest {
            getLatestMobileSignature(userId).map { either ->
                either.fold(
                    { error ->
                        Timber.e("Unable to get mobile signature $error, returning default.")
                        MobileSignaturePreference.Empty
                    },
                    { it }
                )
            }
        }.apply { restartTrigger.tryEmit(Unit) }

    private fun getLatestMobileSignature(userId: UserId): Flow<Either<DataError, MobileSignaturePreference>> = flow {
        emit(dataSource.getMobileSignature(userId))
    }

    override suspend fun setMobileSignature(userId: UserId, signature: String): Either<DataError, Unit> =
        dataSource.setMobileSignature(userId, signature)
            .onLeft { Timber.e("Failed to set mobile signature: $it") }
            .onRight { restartTrigger.emit(Unit) }

    override suspend fun setMobileSignatureEnabled(userId: UserId, enabled: Boolean): Either<DataError, Unit> =
        dataSource.setMobileSignatureEnabled(userId, enabled)
            .onLeft { Timber.e("Failed to set mobile signature enabled=$enabled: $it") }
            .onRight { restartTrigger.emit(Unit) }

}
