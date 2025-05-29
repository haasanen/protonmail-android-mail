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
import ch.protonmail.android.legacymigration.data.usecase.DecryptLegacySerializableValue
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockBiometricsPreference
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockPin
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockPreference
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LegacyAutoLockLocalDataSourceImpl @Inject constructor(
    private val dataStoreProvider: LegacyAutoLockDataStoreProvider,
    private val decryptLegacySerializableValue: DecryptLegacySerializableValue
) : LegacyAutoLockLocalDataSource {

    private val dataStore = dataStoreProvider.legacyAutoLockDataStore
    private val hasAutoLockKey = stringPreferencesKey(LEGACY_AUTO_LOCK_ENABLED_PREF_KEY)
    private val pinKey = stringPreferencesKey(LEGACY_PIN_CODE_PREF_KEY)
    private val autoLockBiometricsKey = stringPreferencesKey(LEGACY_AUTO_LOCK_BIOMETRICS_PREF_KEY)

    override suspend fun autoLockEnabled(): Either<MigrationError, LegacyAutoLockPreference> {
        return runCatching {
            dataStore.data.first()[hasAutoLockKey]
        }.fold(
            onSuccess = { encryptedValue ->
                encryptedValue?.let {
                    decryptLegacySerializableValue<LegacyAutoLockPreference>(encryptedValue)
                        .mapLeft { MigrationError.AutoLockFailure.FailedToDecryptAutoLockEnabled }
                }
                    ?: MigrationError.AutoLockFailure.FailedToReadAutoLockEnabled.left()

            },
            onFailure = {
                MigrationError.AutoLockFailure.FailedToReadAutoLockEnabled.left()
            }
        )
    }

    override suspend fun hasAutoLockPinCode(): Boolean = runCatching {
        dataStore.data.first()[pinKey]?.isNotEmpty() == true
    }.getOrDefault(false)

    override fun observeAutoLockPinCode(): Flow<Either<MigrationError, LegacyAutoLockPin>> {
        return dataStore.data
            .map { prefs ->
                prefs[pinKey]
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { encryptedPin ->
                        decryptLegacySerializableValue<LegacyAutoLockPin>(encryptedPin)
                            .mapLeft { MigrationError.AutoLockFailure.FailedToReadAutoLockPin }
                    }
                    ?: MigrationError.AutoLockFailure.FailedToReadAutoLockPin.left()
            }
            .catch {
                emit(MigrationError.AutoLockFailure.FailedToReadAutoLockPin.left())
            }
    }

    override suspend fun hasAutoLockBiometricPreference(): Boolean = runCatching {
        dataStore.data.first()[autoLockBiometricsKey]?.isNotEmpty() == true
    }.getOrDefault(false)

    @Suppress("MaxLineLength")
    override fun observeAutoLockBiometricsPreference(): Flow<Either<MigrationError, LegacyAutoLockBiometricsPreference>> {
        return dataStore.data
            .map { prefs ->
                prefs[autoLockBiometricsKey]
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { encryptedValue ->
                        decryptLegacySerializableValue<LegacyAutoLockBiometricsPreference>(encryptedValue)
                            .mapLeft { MigrationError.AutoLockFailure.FailedToDecryptBiometricPreference }
                    }
                    ?: MigrationError.AutoLockFailure.FailedToReadBiometricPreference.left()
            }
            .catch {
                emit(MigrationError.AutoLockFailure.FailedToReadBiometricPreference.left())
            }
    }

    private companion object {
        const val LEGACY_PIN_CODE_PREF_KEY = "pinCodePrefKey"
        const val LEGACY_AUTO_LOCK_BIOMETRICS_PREF_KEY = "autoLockBiometricsKey"
        const val LEGACY_AUTO_LOCK_ENABLED_PREF_KEY = "hasAutoLockPrefKey"
    }
}
