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

package ch.protonmail.android.mailsession.data.keychain

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import uniffi.proton_mail_uniffi.OsKeyChain
import uniffi.proton_mail_uniffi.OsKeyChainEntryKind
import javax.inject.Inject

class AndroidKeyChain @Inject constructor(
    @ApplicationContext private val context: Context
) : OsKeyChain {

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun delete(kind: OsKeyChainEntryKind) {
        sharedPreferences.edit { remove(kind.name) }
    }

    override fun load(kind: OsKeyChainEntryKind): String? {
        val key = sharedPreferences.getString(kind.name, null)
        Timber.d("get key: $key")
        return key
    }

    override fun store(kind: OsKeyChainEntryKind, key: String) {
        sharedPreferences.edit { putString(kind.name, key) }
    }

    companion object {
        private const val PREFS_NAME = "OsKeyChainPrefs"
    }
}
