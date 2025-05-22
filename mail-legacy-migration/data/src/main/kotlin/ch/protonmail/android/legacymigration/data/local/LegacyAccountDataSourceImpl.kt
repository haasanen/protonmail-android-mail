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

package ch.protonmail.android.legacymigration.data.local

import ch.protonmail.android.legacymigration.data.local.rawSql.LegacyDbReader
import ch.protonmail.android.legacymigration.domain.LegacyDBCoroutineScope
import ch.protonmail.android.legacymigration.domain.model.LegacySessionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

class LegacyAccountDataSourceImpl @Inject constructor(
    private val dbReader: LegacyDbReader,
    @LegacyDBCoroutineScope private val dbCoroutineScope: CoroutineScope
) : LegacyAccountDataSource {

    override suspend fun getSession(sessionId: SessionId): LegacySessionInfo? =
        withContext(dbCoroutineScope.coroutineContext) {
            dbReader.readLegacySessionInfo(sessionId)
        }

    override suspend fun getPrimaryUserId(): UserId? = withContext(dbCoroutineScope.coroutineContext) {
        dbReader.readLatestPrimaryUserId()
    }

    override suspend fun getSessions(): List<LegacySessionInfo> = withContext(dbCoroutineScope.coroutineContext) {
        dbReader.readAuthenticatedSessions()
    }
}
