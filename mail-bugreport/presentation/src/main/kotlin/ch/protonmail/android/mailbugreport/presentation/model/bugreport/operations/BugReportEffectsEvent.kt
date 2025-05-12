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

package ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations

import androidx.annotation.StringRes
import ch.protonmail.android.mailbugreport.presentation.R
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportState
import ch.protonmail.android.mailbugreport.presentation.ui.report.BugReportFocusableField
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.NetworkError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel

internal sealed interface EffectEvent : BugReportEvent {

    override fun toStateModifications() = BugReportStateModifications(
        effectsModification = when (this) {
            UserIdNotFound -> EffectsStateModification.OnCloseWithErrorMessage(R.string.report_a_problem_generic_error)
            is ErrorOnSubmission -> EffectsStateModification.OnSubmissionError(dataError)
            CompletedWithSuccess -> EffectsStateModification.OnCloseWithSuccess
            CloseWithData -> EffectsStateModification.OnCloseWithPendingData
            Close -> EffectsStateModification.OnClose
            is OnFieldValidationFailed -> EffectsStateModification.FieldValidationError(field)
        }
    )

    data object UserIdNotFound : EffectEvent
    data class ErrorOnSubmission(val dataError: DataError) : EffectEvent
    data object CompletedWithSuccess : EffectEvent
    data object CloseWithData : EffectEvent
    data object Close : EffectEvent

    data class OnFieldValidationFailed(val field: BugReportFocusableField) : EffectEvent
}

internal sealed interface EffectsStateModification : BugReportStateModification<BugReportState.Effects> {
    data object OnCloseWithPendingData : EffectsStateModification {

        override fun apply(state: BugReportState.Effects) = state.copy(
            closeWithPendingData = Effect.of(Unit)
        )
    }

    data object OnClose : EffectsStateModification {

        override fun apply(state: BugReportState.Effects): BugReportState.Effects = state.copy(
            close = Effect.of(Unit)
        )
    }

    data class OnCloseWithErrorMessage(@StringRes val messageRes: Int) : EffectsStateModification {

        override fun apply(state: BugReportState.Effects) = state.copy(
            closeWithMessage = Effect.of(TextUiModel.TextRes(messageRes))
        )
    }

    data object OnCloseWithSuccess : EffectsStateModification {

        override fun apply(state: BugReportState.Effects) = state.copy(
            closeWithMessage = Effect.of(TextUiModel.TextRes(R.string.report_a_problem_success))
        )
    }

    data class OnSubmissionError(val dataError: DataError) : EffectsStateModification {

        override fun apply(state: BugReportState.Effects): BugReportState.Effects {
            val textRes = when (dataError) {
                is DataError.Remote.Http -> {
                    if (dataError.networkError is NetworkError.NoNetwork) {
                        R.string.report_a_problem_offline_error
                    } else {
                        R.string.report_a_problem_generic_error
                    }
                }

                else -> R.string.report_a_problem_generic_error
            }

            return state.copy(
                submissionError = Effect.of(TextUiModel.TextRes(textRes))
            )
        }
    }

    data class FieldValidationError(val field: BugReportFocusableField) : EffectsStateModification {

        override fun apply(state: BugReportState.Effects) = state.copy(
            forceFocusField = Effect.of(field)
        )
    }
}
