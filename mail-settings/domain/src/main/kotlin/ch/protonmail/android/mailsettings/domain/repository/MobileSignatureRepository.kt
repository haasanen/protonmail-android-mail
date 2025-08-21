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

package ch.protonmail.android.mailsettings.domain.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsettings.domain.model.MobileSignaturePreference
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId

interface MobileSignatureRepository {
    fun observeMobileSignature(userId: UserId): Flow<MobileSignaturePreference>
    suspend fun getMobileSignature(userId: UserId): Either<DataError, MobileSignaturePreference>
    suspend fun setMobileSignature(userId: UserId, signature: String): Either<DataError, Unit>
    suspend fun setMobileSignatureEnabled(userId: UserId, enabled: Boolean): Either<DataError, Unit>
}
