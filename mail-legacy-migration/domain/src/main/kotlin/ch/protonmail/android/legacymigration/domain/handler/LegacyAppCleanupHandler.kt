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

package ch.protonmail.android.legacymigration.domain.handler

import android.content.Context
import androidx.work.WorkManager
import ch.protonmail.android.legacymigration.domain.model.LegacyMigrationStatus
import ch.protonmail.android.legacymigration.domain.usecase.ObserveLegacyMigrationStatus
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LegacyAppCleanupHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    @AppScope private val appScope: CoroutineScope,
    private val observeLegacyMigrationStatus: ObserveLegacyMigrationStatus
) {

    fun performCleanup() {
        appScope.launch {

            if (isFirstLaunchAfterETUpgrade()) {
                Timber.d("Legacy migration: performing legacy app cleanup")
                cleanWorkManagerDb()
            } else {
                Timber.d("LegacyAppCleanupHandler: Not first launch after ET upgrade")
            }
        }
    }

    private suspend fun isFirstLaunchAfterETUpgrade(): Boolean =
        observeLegacyMigrationStatus().first() == LegacyMigrationStatus.NotDone

    private fun cleanWorkManagerDb() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
        workManager.pruneWork()
        Timber.d("Legacy migration: Cleaned up WorkManager legacy jobs")
    }
}
