/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package ch.protonmail.android.mailsettings.data.repository

import androidx.datastore.preferences.core.stringPreferencesKey
import arrow.core.Either
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.safeData
import ch.protonmail.android.mailcommon.data.mapper.safeEdit
import ch.protonmail.android.mailcommon.domain.model.PreferencesError
import ch.protonmail.android.mailsettings.data.MailSettingsDataStoreProvider
import ch.protonmail.android.mailsettings.domain.model.BackgroundSyncInterval
import ch.protonmail.android.mailsettings.domain.repository.BackgroundSyncIntervalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BackgroundSyncIntervalRepositoryImpl @Inject constructor(
    private val dataStoreProvider: MailSettingsDataStoreProvider
) : BackgroundSyncIntervalRepository {

    private val backgroundSyncIntervalKey = stringPreferencesKey("backgroundSyncIntervalPrefKey")

    override fun observe(): Flow<Either<PreferencesError, BackgroundSyncInterval>> =
        dataStoreProvider.backgroundSyncDataStore.safeData.map { preferences ->
            preferences.map { prefs ->
                val name = prefs[backgroundSyncIntervalKey] ?: DefaultValue
                runCatching { BackgroundSyncInterval.valueOf(name) }
                    .getOrDefault(BackgroundSyncInterval.REAL_TIME)
            }
        }

    override suspend fun update(interval: BackgroundSyncInterval): Either<PreferencesError, Unit> {
        return dataStoreProvider.backgroundSyncDataStore.safeEdit { preferences ->
            preferences[backgroundSyncIntervalKey] = interval.name
        }.map { Unit.right() }
    }

    private companion object {

        const val DefaultValue = "REAL_TIME"
    }
}
