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

package ch.protonmail.android.mailbugreport.presentation.ui.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.component.ProtonSettingsToggleItem
import ch.protonmail.android.design.compose.component.VerticalSpacer
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeWeak
import ch.protonmail.android.mailbugreport.presentation.R
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportFormErrorsUiModel
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportFormFieldsUiModel
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.compose.FocusableForm

@Composable
internal fun BugReportScreenContent(
    fields: BugReportFormFieldsUiModel,
    includeLogs: Boolean,
    onLogsToggled: (Boolean) -> Unit,
    validationErrors: BugReportFormErrorsUiModel,
    forceFocusRequest: Effect<BugReportFocusableField>,
    modifier: Modifier = Modifier
) {
    FocusableForm(
        fieldList = listOf(
            BugReportFocusableField.Summary,
            BugReportFocusableField.ExpectedResult,
            BugReportFocusableField.StepsToReproduce,
            BugReportFocusableField.ActualResult
        ),
        initialFocus = null,
        onFocusedField = {}
    ) { fieldFocusRequesters ->

        ConsumableLaunchedEffect(forceFocusRequest) {
            fieldFocusRequesters[it]?.requestFocus()
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(ProtonDimens.Spacing.Large)
                .verticalScroll(rememberScrollState())
        ) {
            HeaderText()
            VerticalSpacer(height = ProtonDimens.Spacing.Jumbo)

            ErrorLogsToggle(includeLogs, onToggle = onLogsToggled)
            VerticalSpacer(height = ProtonDimens.Spacing.Jumbo)

            BugReportTextField(
                title = stringResource(R.string.report_a_problem_summary_title),
                description = stringResource(R.string.report_a_problem_summary_description),
                inputState = fields.summaryText,
                shouldShowError = validationErrors.showSummaryError,
                modifier = Modifier.retainFieldFocusOnConfigurationChange(BugReportFocusableField.Summary)
            )

            VerticalSpacer(height = ProtonDimens.Spacing.Jumbo)

            BugReportTextField(
                title = stringResource(R.string.report_a_problem_expected_results_title),
                description = stringResource(R.string.report_a_problem_expected_results_description),
                inputState = fields.expectedResult,
                shouldShowError = validationErrors.showExpectedResultError,
                modifier = Modifier.retainFieldFocusOnConfigurationChange(BugReportFocusableField.ExpectedResult)
            )

            VerticalSpacer(height = ProtonDimens.Spacing.Jumbo)

            BugReportTextField(
                title = stringResource(R.string.report_a_problem_repro_steps_title),
                description = stringResource(R.string.report_a_problem_repro_steps_description),
                inputState = fields.reproStepsText,
                shouldShowError = validationErrors.showReproStepsError,
                modifier = Modifier.retainFieldFocusOnConfigurationChange(BugReportFocusableField.StepsToReproduce)
            )

            VerticalSpacer(height = ProtonDimens.Spacing.Jumbo)

            BugReportTextField(
                title = stringResource(R.string.report_a_problem_actual_results_title),
                description = stringResource(R.string.report_a_problem_actual_results_description),
                inputState = fields.actualResultSteps,
                shouldShowError = validationErrors.showActualResultError,
                modifier = Modifier.retainFieldFocusOnConfigurationChange(BugReportFocusableField.ActualResult)
            )
        }
    }
}

@Composable
private fun HeaderText() {
    Text(
        text = stringResource(R.string.report_a_problem_disclaimer_e2ee),
        style = ProtonTheme.typography.bodyLargeWeak
    )
}

@Composable
private fun ErrorLogsToggle(value: Boolean, onToggle: (value: Boolean) -> Unit) {
    ProtonSettingsToggleItem(
        name = stringResource(R.string.report_a_problem_include_errors_title),
        hint = stringResource(R.string.report_a_problem_include_errors_description),
        onToggle = onToggle,
        value = value
    )
}
