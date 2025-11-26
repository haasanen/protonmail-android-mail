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

package ch.protonmail.android.mailcrashrecord.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject

class CrashRecordDataStoreProvider @Inject constructor(
    context: Context
) {

    private val Context.crashRecordDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "crashRecordDataStore"
    )
    val crashRecordDataStore = context.crashRecordDataStore

    internal companion object {

        const val MESSAGE_BODY_WEB_VIEW_CRASH_KEY = "MessageBodyWebViewCrashKey"
    }
}
