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

package ch.protonmail.android.legacymigration.data.local.signature

import androidx.datastore.preferences.core.booleanPreferencesKey
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.domain.LegacyDBCoroutineScope
import ch.protonmail.android.legacymigration.domain.model.LegacySignaturePreference
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.mailcommon.data.mapper.safeEdit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.user.domain.entity.AddressId
import timber.log.Timber
import javax.inject.Inject

class LegacySignatureLocalDataSourceImpl @Inject constructor(
    dataStoreProvider: LegacySignatureDataStoreProvider,
    @LegacyDBCoroutineScope private val coroutineScope: CoroutineScope
) : LegacySignatureLocalDataSource {

    private val dataStore = dataStoreProvider.legacySignatureDataStore

    override suspend fun getSignaturePreference(
        addressId: AddressId
    ): Either<MigrationError, LegacySignaturePreference> {
        return try {
            val prefs = dataStore.data.first()

            val enabled = prefs[getSignatureEnabledPrefKey(addressId)]

            if (enabled != null) {
                LegacySignaturePreference(isEnabled = enabled).right()
            } else {
                MigrationError.SignatureFailure.FailedToReadMobileSignaturePreference.left()
            }
        } catch (_: Exception) {
            Timber.e("Legacy migration: Failed to read signature preference for ${addressId.id}")
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

        fun getSignatureEnabledPrefKey(addressId: AddressId) = booleanPreferencesKey(
            "${addressId.id}-signatureEnabledPrefKey"
        )
    }
}
