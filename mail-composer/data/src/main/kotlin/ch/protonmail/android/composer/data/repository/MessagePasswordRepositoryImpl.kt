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

package ch.protonmail.android.composer.data.repository

import arrow.core.Either
import ch.protonmail.android.composer.data.local.RustDraftDataSource
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.MessagePassword
import ch.protonmail.android.mailcomposer.domain.model.MessagePasswordError
import ch.protonmail.android.mailcomposer.domain.repository.MessagePasswordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessagePasswordRepositoryImpl @Inject constructor(
    private val draftDataSource: RustDraftDataSource
) : MessagePasswordRepository {

    override suspend fun isPasswordProtected(): Either<DataError, Boolean> = draftDataSource.isPasswordProtected()

    override suspend fun savePassword(password: MessagePassword): Either<MessagePasswordError, Unit> =
        draftDataSource.setMessagePassword(password)

    override suspend fun removePassword(): Either<MessagePasswordError, Unit> = draftDataSource.removeMessagePassword()

    override suspend fun getPassword(): Either<DataError, MessagePassword?> = draftDataSource.getMessagePassword()


    override fun observePasswordUpdatedSignal(): Flow<Unit> = draftDataSource.observePasswordUpdatedSignal()
}
