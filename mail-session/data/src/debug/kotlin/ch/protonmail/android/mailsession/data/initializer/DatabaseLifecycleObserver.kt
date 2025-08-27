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

package ch.protonmail.android.mailsession.data.initializer

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.LifecycleOwner
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsDebugInspectDbEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import ch.protonmail.android.mailsession.data.initializer.DatabaseLifecycleObserverImpl.DatabaseIdentifier.Companion.AccountDatabaseIdentifier
import ch.protonmail.android.mailsession.domain.annotations.DatabasesBaseDirectory
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider

class DatabaseLifecycleObserverImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    @AppScope private val scope: CoroutineScope,
    @DatabasesBaseDirectory private val dbBaseDirectory: Provider<File>,
    @IsDebugInspectDbEnabled private val isDebugInspectDbEnabled: FeatureFlag<Boolean>
) : DatabaseLifecycleObserver {

    private val databases = ConcurrentHashMap<String, SQLiteDatabase>()
    private val databasesJob = SupervisorJob()

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        scope.launch {
            if (isDebugInspectDbEnabled.get()) {
                Timber.tag(TAG).i("Initializing database inspector")
                setupDatabaseWatchers()
                initializeDatabase(AccountDatabaseIdentifier)
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Timber.tag(TAG).i("Destroying database inspector")

        databasesJob.cancel()

        databases.forEach { (id, _) ->
            teardownDatabase(id)
        }

        databases.clear()
    }

    private fun setupDatabaseWatchers() {
        scope.launch(Dispatchers.IO + databasesJob) {
            runCatching {
                userSessionRepository
                    .observeAccounts()
                    .distinctUntilChanged()
                    .map { accounts ->
                        accounts.map { UserIdStatePair(it.userId, it.state) }
                    }
                    .collect { statePairs ->
                        processDatabaseStatePairs(statePairs)
                    }
            }.onFailure {
                Timber.tag(TAG).e(it, "Error in database watcher")
            }
        }
    }

    private suspend fun processDatabaseStatePairs(statePairs: List<UserIdStatePair>) {
        withContext(Dispatchers.IO) {
            statePairs.forEach { pair ->
                when (pair.state) {
                    AccountState.Disabled -> {
                        Timber.tag(TAG).d("Account disabled, tearing down database for ${pair.userId.id}")
                        teardownDatabase(pair.userId.id)
                    }

                    AccountState.Ready -> {
                        Timber.tag(TAG).d("Account ready, initializing database for ${pair.userId.id}")
                        initializeDatabase(DatabaseIdentifier(pair.userId))
                    }

                    else -> {
                        Timber.tag(TAG).v("Ignoring account state ${pair.state} for ${pair.userId.id}")
                    }
                }
            }
        }
    }

    private fun initializeDatabase(identifier: DatabaseIdentifier) {
        val databaseId = identifier.id

        if (databases.containsKey(databaseId)) {
            Timber.tag(TAG).d("Database $databaseId is already open")
            return
        }

        val databasePath = getDatabaseFilePath(databaseId)
        val databaseFile = File(databasePath)

        if (!databaseFile.exists()) {
            Timber.tag(TAG).d("Database file does not exist: $databasePath")
            return
        }

        runCatching {
            Timber.tag(TAG).i("Opening database for identifier $databaseId")

            // Do not open it in WRITE mode, as it might cause a DB lock on Rust side.
            val database = SQLiteDatabase.openDatabase(
                databasePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )

            databases[databaseId] = database
            Timber.tag(TAG).i("Successfully opened database for $databaseId")
        }.onFailure {
            Timber.tag(TAG).e(it, "Failed to open database for $databaseId")
        }
    }

    private fun teardownDatabase(databaseId: String) {
        val database = databases[databaseId]

        if (database == null) {
            Timber.tag(TAG).d("No database found to close for $databaseId")
            return
        }

        runCatching {
            if (database.isOpen) {
                Timber.tag(TAG).i("Closing database for $databaseId")
                database.close()
                Timber.tag(TAG).i("Database closed for $databaseId")
            } else {
                Timber.tag(TAG).d("Database for $databaseId is already closed")
            }
        }.onFailure {
            Timber.tag(TAG).e(it, "Error closing database for $databaseId")
            databases.remove(databaseId)
        }
    }

    private fun getDatabaseFilePath(databaseId: String) =
        dbBaseDirectory.get().absolutePath + File.separator + getDatabaseName(databaseId)

    private fun getDatabaseName(name: String): String = "$name.db"

    private data class UserIdStatePair(
        val userId: UserId,
        val state: AccountState
    )

    @JvmInline
    private value class DatabaseIdentifier private constructor(val id: String) {

        companion object {

            val AccountDatabaseIdentifier = DatabaseIdentifier("account")
            operator fun invoke(userId: UserId) = DatabaseIdentifier(userId.id)
        }
    }

    companion object {

        private val TAG = "mail-debug-db-observer"
    }
}
