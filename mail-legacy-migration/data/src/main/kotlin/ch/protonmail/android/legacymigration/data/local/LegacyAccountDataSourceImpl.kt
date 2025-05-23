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

import android.content.Context
import ch.protonmail.android.legacymigration.data.local.rawSql.LegacyDbReader
import ch.protonmail.android.legacymigration.domain.LegacyDBCoroutineScope
import ch.protonmail.android.legacymigration.domain.model.LegacyDatabase
import ch.protonmail.android.legacymigration.domain.model.LegacySessionInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class LegacyAccountDataSourceImpl @Inject constructor(
    private val dbReader: LegacyDbReader,
    @LegacyDBCoroutineScope private val dbCoroutineScope: CoroutineScope,
    @ApplicationContext private val context: Context
) : LegacyAccountDataSource {

    override suspend fun getSession(sessionId: SessionId): LegacySessionInfo? = safeLegacyDbRead(
        coroutineContext = dbCoroutineScope.coroutineContext,
        description = "readLegacySessionInfo",
        fallback = null
    ) {
        dbReader.readLegacySessionInfo(sessionId)
    }

    override suspend fun getPrimaryUserId(): UserId? = safeLegacyDbRead(
        coroutineContext = dbCoroutineScope.coroutineContext,
        description = "readLatestPrimaryUserId",
        fallback = null
    ) {
        dbReader.readLatestPrimaryUserId()
    }

    override suspend fun getSessions(): List<LegacySessionInfo> = safeLegacyDbRead(
        coroutineContext = dbCoroutineScope.coroutineContext,
        description = "readAuthenticatedSessions",
        fallback = emptyList()
    ) {
        dbReader.readAuthenticatedSessions()
    }

    override suspend fun legacyDbExists(): Boolean {
        val dbFile = context.getDatabasePath(LegacyDatabase.Name)
        if (!dbFile.exists()) {
            Timber.d("Legacy migration: Db file does not exist!")
            return false
        }

        return safeLegacyDbRead(
            coroutineContext = dbCoroutineScope.coroutineContext,
            description = "legacyDbHasExpectedTables",
            fallback = false
        ) {
            dbReader.legacyDbHasExpectedTables().also { tablesExist ->
                if (tablesExist) {
                    Timber.d("Legacy migration: Legacy DB has expected tables")
                } else {
                    Timber.w("Legacy migration: Legacy DB missing expected tables")
                }
            }
        }
    }
}

suspend inline fun <T> safeLegacyDbRead(
    coroutineContext: CoroutineContext,
    description: String,
    fallback: T,
    crossinline block: suspend () -> T
): T = withContext(coroutineContext) {
    runCatching { block() }
        .onFailure { Timber.w(it, "Legacy migration: Failed db operation: $description") }
        .getOrElse { fallback }
}
