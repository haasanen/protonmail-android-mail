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

package ch.protonmail.android.mailcrashrecord.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import arrow.core.Either
import ch.protonmail.android.mailcommon.data.mapper.safeData
import ch.protonmail.android.mailcommon.data.mapper.safeEdit
import ch.protonmail.android.mailcommon.domain.model.PreferencesError
import ch.protonmail.android.mailcrashrecord.data.CrashRecordDataStoreProvider
import ch.protonmail.android.mailcrashrecord.domain.model.MessageBodyWebViewCrash
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CrashRecordLocalDataSourceImpl @Inject constructor(
    private val crashRecordDataStoreProvider: CrashRecordDataStoreProvider
) : CrashRecordLocalDataSource {

    private val messageBodyWebViewCrashKey = booleanPreferencesKey(
        CrashRecordDataStoreProvider.MESSAGE_BODY_WEB_VIEW_CRASH_KEY
    )

    override suspend fun get(): Either<PreferencesError, MessageBodyWebViewCrash> {
        return crashRecordDataStoreProvider.crashRecordDataStore.safeData.map { prefsEither ->
            prefsEither.map { prefs ->
                val hasWebViewCrashed = prefs[messageBodyWebViewCrashKey] ?: DEFAULT_VALUE
                MessageBodyWebViewCrash(hasWebViewCrashed)
            }
        }.first()
    }

    override suspend fun save(crash: MessageBodyWebViewCrash): Either<PreferencesError, Unit> {
        return crashRecordDataStoreProvider.crashRecordDataStore.safeEdit { mutablePreferences ->
            mutablePreferences[messageBodyWebViewCrashKey] = crash.hasCrashed
        }.map { }
    }
}

private const val DEFAULT_VALUE = false
