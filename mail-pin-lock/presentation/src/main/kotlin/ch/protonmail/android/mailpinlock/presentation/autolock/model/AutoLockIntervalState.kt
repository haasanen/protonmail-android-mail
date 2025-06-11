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

package ch.protonmail.android.mailpinlock.presentation.autolock.model

import androidx.compose.runtime.Immutable
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.model.AutoLockInterval

@Immutable
sealed class AutoLockIntervalState {

    data class Data(
        private val currentInterval: AutoLockInterval,
        val intervalsToChoices: Map<AutoLockInterval, TextUiModel>
    ) : AutoLockIntervalState() {

        val intervalChoices = intervalsToChoices.values.toList()
        val selectedInterval = intervalsToChoices.getValue(currentInterval)
    }

    data object Loading : AutoLockIntervalState()
}

data class AutolockIntervalEffects(
    val close: Effect<Unit> = Effect.empty()
)

internal fun AutolockIntervalEffects.onCloseEffect() = this.copy(close = Effect.of(Unit))

internal fun AutoLockIntervalState.Data.intervalFor(choice: TextUiModel) =
    intervalsToChoices.entries.first { it.value == choice }.key

