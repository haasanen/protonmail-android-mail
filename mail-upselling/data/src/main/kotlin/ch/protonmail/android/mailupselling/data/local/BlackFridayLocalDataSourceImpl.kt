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

package ch.protonmail.android.mailupselling.data.local

import androidx.datastore.preferences.core.longPreferencesKey
import arrow.core.Either
import ch.protonmail.android.mailcommon.data.mapper.safeData
import ch.protonmail.android.mailcommon.data.mapper.safeEdit
import ch.protonmail.android.mailcommon.domain.model.PreferencesError
import ch.protonmail.android.mailupselling.data.BlackFridayDataStoreProvider
import ch.protonmail.android.mailupselling.domain.model.BlackFridayPhase
import ch.protonmail.android.mailupselling.domain.model.BlackFridaySeenPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Clock

class BlackFridayLocalDataSourceImpl @Inject constructor(
    private val dataStoreProvider: BlackFridayDataStoreProvider
) : BlackFridayLocalDataSource {

    private val wave1FirstSeenPrefKey = longPreferencesKey(
        BlackFridayDataStoreProvider.PHASE_1_SEEN_KEY
    )

    private val wave2FirstSeenPrefKey = longPreferencesKey(
        BlackFridayDataStoreProvider.PHASE_2_SEEN_KEY
    )

    override fun observePhaseEligibility(
        phase: BlackFridayPhase.Active
    ): Flow<Either<PreferencesError, BlackFridaySeenPreference>> =
        dataStoreProvider.blackFridayDataStore.safeData.map { prefsEither ->
            prefsEither.map { prefs ->
                val phaseSeen = prefs[phase.toPrefKey()] ?: 0L
                BlackFridaySeenPreference(phase, phaseSeen)
            }
        }

    override suspend fun saveSeen(phase: BlackFridayPhase.Active): Either<PreferencesError, Unit> =
        dataStoreProvider.blackFridayDataStore.safeEdit { mutablePreferences ->
            mutablePreferences[phase.toPrefKey()] = Clock.System.now().toEpochMilliseconds()
        }.map { }

    private fun BlackFridayPhase.Active.toPrefKey() = when (this) {
        BlackFridayPhase.Active.Wave1 -> wave1FirstSeenPrefKey
        BlackFridayPhase.Active.Wave2 -> wave2FirstSeenPrefKey
    }
}
