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

package ch.protonmail.android.mailsnooze.presentation.model

import androidx.compose.runtime.Immutable
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsnooze.domain.model.SnoozeTime
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility

@Immutable
sealed class SnoozeOptionsState {

    data class Loaded(
        val snoozeOptions: List<SnoozeOptionUiModel>,
        val snoozeBottomSheet: SelectionType = PredefinedChoice
    ) : SnoozeOptionsState()

    data object Loading : SnoozeOptionsState()
}

sealed interface SelectionType
object Custom : SelectionType
object PredefinedChoice : SelectionType

data class SnoozeOptionsEffects(
    val success: Effect<TextUiModel> = Effect.empty(),
    val error: Effect<TextUiModel> = Effect.empty(),
    val navigateToUpsell: Effect<UpsellingVisibility> = Effect.empty()
)

internal fun SnoozeOptionsEffects.onSuccessEffect(snoozeTime: TextUiModel) = this.copy(success = Effect.of(snoozeTime))
internal fun SnoozeOptionsEffects.onErrorEffect(uiModel: TextUiModel) = this.copy(error = Effect.of(uiModel))
internal fun SnoozeOptionsEffects.onNavigateToUpsell(type: UpsellingVisibility) =
    this.copy(navigateToUpsell = Effect.of(type))

sealed interface SnoozeOperation

sealed interface SnoozeOperationViewAction : SnoozeOperation {

    data class SnoozeUntil(val snoozeTime: SnoozeTime) : SnoozeOperationViewAction
    data object PickSnooze : SnoozeOperationViewAction
    data object UnSnooze : SnoozeOperationViewAction
    data class Upgrade(val type: UpsellingVisibility) : SnoozeOperationViewAction
    object CancelPicker : SnoozeOperationViewAction
}
