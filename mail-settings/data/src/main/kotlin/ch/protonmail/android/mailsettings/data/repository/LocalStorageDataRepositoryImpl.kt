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

package ch.protonmail.android.mailsettings.data.repository

import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailsettings.domain.model.ClearDataAction
import ch.protonmail.android.mailsettings.domain.repository.LocalStorageDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

@MissingRustApi
// Rust to expose methods to clean the local DB...
class LocalStorageDataRepositoryImpl @Inject constructor() : LocalStorageDataRepository {

    override fun observeMessageDataTotalRawSize(): Flow<Long> {
        Timber.w("rust-settings: Not implemented! Rust to expose methods to get used storage size")
        return flowOf(-1)
    }

    override suspend fun getAttachmentDataSizeForUserId(userId: UserId): Long {
        Timber.w("rust-settings: Not implemented! Rust to expose methods to get attachments size")
        return -1
    }

    override fun performClearData(userId: UserId, clearDataAction: ClearDataAction) {
        Timber.w("rust-settings: Not implemented! Rust to expose methods to clear data")
    }
}
