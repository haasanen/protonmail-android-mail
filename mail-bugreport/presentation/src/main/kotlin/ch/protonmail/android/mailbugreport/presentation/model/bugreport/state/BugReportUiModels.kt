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

import androidx.compose.foundation.text.input.TextFieldState

internal data class BugReportFormFieldsUiModel(
    val summaryText: TextFieldState,
    val expectedResult: TextFieldState,
    val reproStepsText: TextFieldState,
    val actualResultSteps: TextFieldState
) {

    companion object {

        fun initialState() = BugReportFormFieldsUiModel(
            summaryText = TextFieldState(),
            expectedResult = TextFieldState(),
            reproStepsText = TextFieldState(),
            actualResultSteps = TextFieldState()
        )
    }
}

internal data class BugReportFormErrorsUiModel(
    val showSummaryError: Boolean,
    val showExpectedResultError: Boolean,
    val showReproStepsError: Boolean,
    val showActualResultError: Boolean
) {

    companion object {

        fun initialState() = BugReportFormErrorsUiModel(
            showSummaryError = false,
            showExpectedResultError = false,
            showReproStepsError = false,
            showActualResultError = false
        )
    }
}
