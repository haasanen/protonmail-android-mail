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

package ch.protonmail.android.mailbugreport.presentation

import java.io.File
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailbugreport.domain.LogsFileHandler
import ch.protonmail.android.mailbugreport.domain.model.IssueReport
import ch.protonmail.android.mailbugreport.domain.usecase.CreateIssueReport
import ch.protonmail.android.mailbugreport.domain.usecase.SubmitIssueReport
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportFormErrorsUiModel
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportFormFieldsUiModel
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportState
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportStates
import ch.protonmail.android.mailbugreport.presentation.reducer.BugReportFormReducer
import ch.protonmail.android.mailbugreport.presentation.ui.report.BugReportFocusableField
import ch.protonmail.android.mailbugreport.presentation.viewmodel.report.BugReportViewModel
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BugReportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(TestDispatcherProvider().Main)

    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()
    private val createIssueReport = mockk<CreateIssueReport>()
    private val submitIssueReport = mockk<SubmitIssueReport>()
    private val logsFileHandler = mockk<LogsFileHandler>()
    private val bugReportFormReducer = spyk<BugReportFormReducer>()

    fun viewModel() = BugReportViewModel(
        observePrimaryUserId,
        createIssueReport,
        submitIssueReport,
        logsFileHandler,
        bugReportFormReducer
    )

    @Test
    fun `should emit initial state`() = runTest {
        // Given
        val mainInitialState = BugReportState.Main.initialState()
        val effectsInitialState = BugReportState.Effects.initialState()

        // When + Then
        viewModel().states.test {
            advanceUntilIdle()
            verifyStates(
                isLoading = mainInitialState.isLoading,
                validationErrors = mainInitialState.validationErrors,
                effects = effectsInitialState,
                actualStates = awaitItem()
            )
            verifyEmptyFields(mainInitialState.fields)
        }
    }

    @Test
    fun `should trigger an error when summary is invalid`() = runTest {
        // Given
        val mainInitialState = BugReportState.Main.initialState()
        val effectsInitialState = BugReportState.Effects.initialState()

        // When + Then
        viewModel().states.test {
            advanceUntilIdle()
            verifyStates(
                isLoading = mainInitialState.isLoading,
                validationErrors = mainInitialState.validationErrors,
                effects = effectsInitialState,
                actualStates = awaitItem()
            )
            verifyEmptyFields(mainInitialState.fields)
        }
    }

    @Test
    fun `should emit event upon closing (no confirmation)`() = runTest {
        // Given
        val mainInitialState = BugReportState.Main.initialState()
        val effectsInitialState = BugReportState.Effects.initialState()
        val effectsFinalState = effectsInitialState.copy(close = Effect.of(Unit))

        // When + Then
        val viewModel = viewModel()
        viewModel.states.test {
            skipItems(1)
            viewModel.close()

            verifyStates(
                isLoading = mainInitialState.isLoading,
                validationErrors = mainInitialState.validationErrors,
                effects = effectsFinalState,
                actualStates = awaitItem()
            )
        }
    }

    @Test
    fun `should emit event upon closing (for confirmation)`() = runTest {
        // Given
        val mainInitialState = BugReportState.Main.initialState()
        val effectsInitialState = BugReportState.Effects.initialState()
        val effectsFinalState = effectsInitialState.copy(closeWithPendingData = Effect.of(Unit))

        // When + Then
        val viewModel = viewModel()
        viewModel.states.test {
            viewModel.states.value.main.fields.summaryText.edit { append("123") }
            viewModel.close()

            verifyStates(
                isLoading = mainInitialState.isLoading,
                validationErrors = mainInitialState.validationErrors,
                effects = effectsFinalState,
                actualStates = expectMostRecentItem()
            )
        }
    }

    @Test
    fun `should emit event upon closing (no confirmation, blank data)`() = runTest {
        // Given
        val mainInitialState = BugReportState.Main.initialState()
        val effectsInitialState = BugReportState.Effects.initialState()
        val effectsFinalState = effectsInitialState.copy(close = Effect.of(Unit))

        // When + Then
        val viewModel = viewModel()
        viewModel.states.test {
            viewModel.states.value.main.fields.summaryText.edit { append(" ".repeat(10)) }
            viewModel.close()

            verifyStates(
                isLoading = mainInitialState.isLoading,
                validationErrors = mainInitialState.validationErrors,
                effects = effectsFinalState,
                actualStates = expectMostRecentItem()
            )
        }
    }

    @Test
    fun `should emit error event when userId can't be fetched`() = runTest {
        // Given
        val finalInitialState = BugReportState.Main.initialState()
        val effectsInitialState = BugReportState.Effects.initialState()
        val finalEffectsState = effectsInitialState.copy(
            closeWithMessage = Effect.of(TextUiModel.TextRes(R.string.report_a_problem_generic_error))
        )

        every { observePrimaryUserId() } returns flowOf(null)

        // When + Then
        val viewModel = viewModel()
        viewModel.states.test {
            viewModel.states.value.main.fields.summaryText.edit { append("1100223344") }
            viewModel.submit(false)
            advanceUntilIdle()

            verifyStates(
                isLoading = finalInitialState.isLoading,
                validationErrors = finalInitialState.validationErrors,
                effects = finalEffectsState,
                actualStates = expectMostRecentItem()
            )
        }
    }

    @Test
    fun `should emit error event when submitting with short summary`() = runTest {
        // Given
        val mainInitialState = BugReportState.Main.initialState()
        val finalInitialState = mainInitialState.copy(
            validationErrors = mainInitialState.validationErrors.copy(showSummaryError = true)
        )
        val effectsInitialState = BugReportState.Effects.initialState()
        val finalEffectsState = effectsInitialState.copy(
            forceFocusField = Effect.of(BugReportFocusableField.Summary)
        )

        every { observePrimaryUserId() } returns flowOf(UserId("userId"))

        // When + Then
        val viewModel = viewModel()
        viewModel.states.test {
            viewModel.states.value.main.fields.summaryText.edit { append("123") }
            viewModel.submit(false)
            advanceUntilIdle()

            verifyStates(
                isLoading = finalInitialState.isLoading,
                validationErrors = finalInitialState.validationErrors,
                effects = finalEffectsState,
                actualStates = expectMostRecentItem()
            )
        }
    }

    @Test
    fun `should emit error event when submitting fails`() = runTest {
        // Given
        val userId = UserId("userId")
        val expectedMainState = BugReportState.Main.initialState()
        val expectedEffectState = BugReportState.Effects.initialState().copy(
            submissionError = Effect.of(TextUiModel.TextRes(R.string.report_a_problem_generic_error))
        )

        val expectedIssueReport = mockk<IssueReport>()

        every { observePrimaryUserId() } returns flowOf(userId)
        every { logsFileHandler.getParentPath() } returns File("")
        coEvery {
            createIssueReport(any(), any(), any(), any(), any(), any())
        } returns expectedIssueReport
        coEvery { submitIssueReport(userId, expectedIssueReport) } returns DataError.Local.NoDataCached.left()

        // When + Then
        val viewModel = viewModel()
        viewModel.states.test {
            viewModel.states.value.main.fields.summaryText.edit { append("1100223344") }
            viewModel.submit(false)
            advanceUntilIdle()

            verifyStates(
                isLoading = expectedMainState.isLoading,
                validationErrors = expectedMainState.validationErrors,
                effects = expectedEffectState,
                actualStates = expectMostRecentItem()
            )
            coVerify(exactly = 1) { submitIssueReport(userId, expectedIssueReport) }
            confirmVerified(submitIssueReport)
        }
    }

    @Test
    fun `should emit completed with success event when submitting succeeds`() = runTest {
        // Given
        val userId = UserId("userId")
        val expectedMainState = BugReportState.Main.initialState()
        val expectedEffectState = BugReportState.Effects.initialState().copy(
            closeWithMessage = Effect.of(TextUiModel.TextRes(R.string.report_a_problem_success))
        )

        val expectedIssueReport = mockk<IssueReport>()

        every { observePrimaryUserId() } returns flowOf(userId)
        every { logsFileHandler.getParentPath() } returns File("")
        coEvery {
            createIssueReport(any(), any(), any(), any(), any(), any())
        } returns expectedIssueReport
        coEvery { submitIssueReport(userId, expectedIssueReport) } returns Unit.right()

        // When + Then
        val viewModel = viewModel()
        viewModel.states.test {
            viewModel.states.value.main.fields.summaryText.edit { append("1100223344") }
            viewModel.submit(false)
            advanceUntilIdle()

            verifyStates(
                isLoading = expectedMainState.isLoading,
                validationErrors = expectedMainState.validationErrors,
                effects = expectedEffectState,
                actualStates = expectMostRecentItem()
            )
            coVerify(exactly = 1) { submitIssueReport(userId, expectedIssueReport) }
            confirmVerified(submitIssueReport)
        }
    }

    private fun verifyStates(
        isLoading: Boolean,
        validationErrors: BugReportFormErrorsUiModel,
        effects: BugReportState.Effects = BugReportState.Effects.initialState(),
        actualStates: BugReportStates
    ) {
        assertEquals(isLoading, actualStates.main.isLoading)
        assertEquals(validationErrors, actualStates.main.validationErrors)
        assertEquals(effects, actualStates.effects)
    }

    // Fields equality needs to be checked separately as TextFieldState is not a data class
    // and we should not define a custom equals()/hashCode() just for this.
    private fun verifyEmptyFields(fieldsUiModel: BugReportFormFieldsUiModel) {
        assertTrue(fieldsUiModel.summaryText.text.isEmpty())
        assertTrue(fieldsUiModel.actualResultSteps.text.isEmpty())
        assertTrue(fieldsUiModel.reproStepsText.text.isEmpty())
        assertTrue(fieldsUiModel.expectedResult.text.isEmpty())
    }
}
