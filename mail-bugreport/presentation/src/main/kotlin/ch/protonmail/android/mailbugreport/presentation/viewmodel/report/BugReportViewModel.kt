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

package ch.protonmail.android.mailbugreport.presentation.viewmodel.report

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailbugreport.domain.model.IssueReport
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField
import ch.protonmail.android.mailbugreport.domain.usecase.CreateIssueReport
import ch.protonmail.android.mailbugreport.domain.usecase.SubmitIssueReport
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.BugReportEvent
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.EffectEvent
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.MainEvent
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportState
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportStates
import ch.protonmail.android.mailbugreport.presentation.reducer.BugReportFormReducer
import ch.protonmail.android.mailbugreport.presentation.ui.report.BugReportFocusableField
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class BugReportViewModel @Inject constructor(
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val createIssueReport: CreateIssueReport,
    private val submitIssueReport: SubmitIssueReport,
    private val bugReportFormReducer: BugReportFormReducer
) : ViewModel() {

    private val validationTriggered = MutableStateFlow(false)

    private val mutableBugReportStates = MutableStateFlow(
        BugReportStates(
            main = BugReportState.Main.initialState(),
            effects = BugReportState.Effects.initialState()
        )
    )

    val states = mutableBugReportStates.asStateFlow()

    private val summaryValidation = snapshotFlow { isSummaryValid() }.distinctUntilChanged()

    init {
        observeSummaryValidation()
    }

    private fun observeSummaryValidation() {
        combine(
            summaryValidation,
            validationTriggered
        ) { isValid, validationTriggered ->
            !isValid && validationTriggered
        }.onEach {
            emitNewStateFromOperation(MainEvent.SummaryErrorToggled(it))
        }.launchIn(viewModelScope)
    }

    fun submit(includeLogs: Boolean) {
        validationTriggered.update { true }

        if (!isSummaryValid()) {
            emitNewStateFromOperation(EffectEvent.OnFieldValidationFailed(BugReportFocusableField.Summary))
            return
        }

        emitNewStateFromOperation(MainEvent.LoadingToggled(true))

        viewModelScope.launch {
            val userId = observePrimaryUserId().firstOrNull()
                ?: return@launch emitNewStateFromOperation(EffectEvent.UserIdNotFound)

            val report = extractIssueReport(includeLogs)

            submitIssueReport(userId, report).onLeft {
                emitNewStateFromOperation(EffectEvent.ErrorOnSubmission(it))
            }.onRight {
                emitNewStateFromOperation(EffectEvent.CompletedWithSuccess)
            }

            emitNewStateFromOperation(MainEvent.LoadingToggled(false))
        }
    }

    fun close() {
        val event = if (mutableBugReportStates.value.anyChangedField()) {
            EffectEvent.CloseWithData
        } else {
            EffectEvent.Close
        }

        emitNewStateFromOperation(event)
    }

    private fun extractIssueReport(includeLogs: Boolean): IssueReport {
        val fields = mutableBugReportStates.value.main.fields

        return createIssueReport(
            summary = IssueReportField.Summary(fields.summaryText.text.toString()),
            stepsToReproduce = IssueReportField.StepsToReproduce(fields.reproStepsText.text.toString()),
            expectedResult = IssueReportField.ExpectedResult(fields.expectedResult.text.toString()),
            actualResult = IssueReportField.ActualResult(fields.actualResultSteps.text.toString()),
            shouldIncludeLogs = IssueReportField.ShouldIncludeLogs(includeLogs)
        )
    }

    private fun isSummaryValid() = mutableBugReportStates.value.summaryText.trim().length >= SummaryLengthMinChars

    private fun emitNewStateFromOperation(event: BugReportEvent) {
        mutableBugReportStates.update { bugReportFormReducer.reduceNewState(it, event) }
    }

    private fun BugReportStates.anyChangedField() = summaryText.isNotBlank() ||
        expectedResultText.isNotBlank() ||
        actualResultText.isNotBlank() ||
        stepsToReproText.isNotBlank()

    private companion object {

        const val SummaryLengthMinChars = 10
    }
}

private val BugReportStates.summaryText get() = main.fields.summaryText.text
private val BugReportStates.expectedResultText get() = main.fields.expectedResult.text
private val BugReportStates.actualResultText get() = main.fields.actualResultSteps.text
private val BugReportStates.stepsToReproText get() = main.fields.reproStepsText.text
