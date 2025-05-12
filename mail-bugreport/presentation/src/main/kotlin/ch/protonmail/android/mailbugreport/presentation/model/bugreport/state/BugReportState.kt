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

package ch.protonmail.android.mailbugreport.presentation.model.bugreport.state

import ch.protonmail.android.mailbugreport.presentation.ui.report.BugReportFocusableField
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel

internal data class BugReportStates(
    val main: BugReportState.Main,
    val effects: BugReportState.Effects
)

internal sealed interface BugReportState {
    data class Main(
        val fields: BugReportFormFieldsUiModel,
        val validationErrors: BugReportFormErrorsUiModel,
        val isLoading: Boolean
    ) : BugReportState {

        companion object {

            fun initialState() = Main(
                fields = BugReportFormFieldsUiModel.initialState(),
                validationErrors = BugReportFormErrorsUiModel.initialState(),
                isLoading = false
            )
        }
    }

    data class Effects(
        val submissionError: Effect<TextUiModel>,
        val forceFocusField: Effect<BugReportFocusableField>,
        val closeWithPendingData: Effect<Unit>,
        val close: Effect<Unit>,
        val closeWithMessage: Effect<TextUiModel>
    ) : BugReportState {

        companion object {

            fun initialState() = Effects(
                submissionError = Effect.empty(),
                forceFocusField = Effect.empty(),
                closeWithPendingData = Effect.empty(),
                close = Effect.empty(),
                closeWithMessage = Effect.empty()
            )
        }
    }
}
