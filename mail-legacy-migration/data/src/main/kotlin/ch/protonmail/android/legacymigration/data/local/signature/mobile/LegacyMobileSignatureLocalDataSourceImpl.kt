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

package ch.protonmail.android.legacymigration.data.local.signature.mobile

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.domain.LegacyDBCoroutineScope
import ch.protonmail.android.legacymigration.domain.model.LegacyMobileSignaturePreference
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.mailcommon.data.mapper.safeEdit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class LegacyMobileSignatureLocalDataSourceImpl @Inject constructor(
    dataStoreProvider: LegacyMobileSignatureDataStoreProvider,
    @LegacyDBCoroutineScope private val coroutineScope: CoroutineScope
) : LegacyMobileSignatureLocalDataSource {

    private val dataStore = dataStoreProvider.legacyMobileSignatureDataStore

    override suspend fun getMobileSignaturePreference(
        userId: UserId
    ): Either<MigrationError, LegacyMobileSignaturePreference> {
        return try {
            val prefs = dataStore.data.first()

            val enabled = prefs[getMobileFooterEnabledPrefKey(userId)]
            val value = prefs[getMobileFooterValuePrefKey(userId)]

            if (enabled != null && value != null) {
                LegacyMobileSignaturePreference(value = value, enabled = enabled).right()
            } else {
                MigrationError.SignatureFailure.FailedToReadMobileSignaturePreference.left()
            }
        } catch (_: Exception) {
            Timber.e("Legacy migration: Failed to read mobile signature preference for ${userId.id}")
            MigrationError.SignatureFailure.FailedToReadMobileSignaturePreference.left()
        }
    }

    override fun clearPreferences() {
        coroutineScope.launch {
            dataStore.safeEdit { prefs ->
                prefs.clear()
            }
        }
    }

    companion object {

        fun getMobileFooterEnabledPrefKey(userId: UserId) = booleanPreferencesKey(
            "${userId.id}-mobileFooterEnabledPrefKey"
        )

        fun getMobileFooterValuePrefKey(userId: UserId) = stringPreferencesKey(
            "${userId.id}-mobileFooterValuePrefKey"
        )
    }
}
