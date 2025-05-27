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

package ch.protonmail.android.legacymigration.data.local.autolock

import androidx.datastore.preferences.core.stringPreferencesKey
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockPin
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import javax.inject.Inject

class LegacyAutoLockLocalDataSourceImpl @Inject constructor(
    private val dataStoreProvider: LegacyAutoLockDataStoreProvider,
    private val keyStoreCrypto: KeyStoreCrypto
) : LegacyAutoLockLocalDataSource {

    private val dataStore = dataStoreProvider.legacyAutoLockDataStore
    private val pinKey = stringPreferencesKey(LEGACY_PIN_CODE_PREF_KEY)

    override suspend fun hasAutoLockPinCode(): Boolean = runCatching {
        dataStore.data.first()[pinKey]?.isNotEmpty() == true
    }.getOrDefault(false)

    override fun observeAutoLockPinCode(): Flow<Either<MigrationError, LegacyAutoLockPin>> {
        return dataStore.data
            .map { prefs ->
                prefs[pinKey]
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { runCatching { LegacyAutoLockPin(keyStoreCrypto.decrypt(it)) }.getOrElse { null } }
                    ?.right()
                    ?: MigrationError.AutoLockFailure.FailedToReadAutoLockPin.left()
            }
            .catch {
                emit(MigrationError.AutoLockFailure.FailedToReadAutoLockPin.left())
            }
    }

    private companion object {
        const val LEGACY_PIN_CODE_PREF_KEY = "pinCodePrefKey"
    }
}
