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

package ch.protonmail.android.mailbugreport.presentation.reducer

import ch.protonmail.android.mailbugreport.presentation.R
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.BugReportEvent
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.EffectEvent
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.MainEvent
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportState
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportStates
import ch.protonmail.android.mailbugreport.presentation.ui.report.BugReportFocusableField
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class BugReportFormReducerTest(
    @Suppress("unused") private val testName: String,
    private val initialState: BugReportStates,
    private val operation: BugReportEvent,
    private val expectedState: BugReportStates
) {

    private val reducer = BugReportFormReducer()

    @Test
    fun `should map to the correct modification`() {
        val actualState = reducer.reduceNewState(initialState, operation)
        assertEquals(expectedState.main.isLoading, actualState.main.isLoading)
        assertEquals(expectedState.main.validationErrors, actualState.main.validationErrors)
        assertEquals(expectedState.effects, actualState.effects)
    }

    companion object {

        private val mainEvents: Collection<Array<Any>> = listOf(
            arrayOf(
                "should toggle loading (on)",
                initialState(),
                MainEvent.LoadingToggled(true),
                initialState().copy(main = initialMainState().copy(isLoading = true))
            ),
            arrayOf(
                "should toggle loading (off)",
                initialState().copy(main = initialMainState().copy(isLoading = true)),
                MainEvent.LoadingToggled(false),
                initialState().copy(main = initialMainState().copy(isLoading = false))
            ),
            arrayOf(
                "should toggle summary validation error (on)",
                initialState().copy(main = initialMainState().copy(validationErrors = createValidationError(false))),
                MainEvent.SummaryErrorToggled(true),
                initialState().copy(main = initialMainState().copy(validationErrors = createValidationError(true)))
            ),
            arrayOf(
                "should toggle summary validation error (off)",
                initialState().copy(main = initialMainState().copy(validationErrors = createValidationError(true))),
                MainEvent.SummaryErrorToggled(false),
                initialState().copy(main = initialMainState().copy(validationErrors = createValidationError(false)))
            )
        )

        private val effectEvents: Collection<Array<Any>> = listOf(
            arrayOf(
                "should trigger close",
                initialState(),
                EffectEvent.Close,
                initialState().copy(effects = initialEffectsState().copy(close = Effect.of(Unit)))
            ),
            arrayOf(
                "should trigger close with pending data",
                initialState(),
                EffectEvent.CloseWithData,
                initialState().copy(effects = initialEffectsState().copy(closeWithPendingData = Effect.of(Unit)))
            ),
            arrayOf(
                "should trigger close with success",
                initialState(),
                EffectEvent.CompletedWithSuccess,
                initialState().copy(
                    effects = initialEffectsState().copy(
                        closeWithMessage = Effect.of(TextUiModel.TextRes(R.string.report_a_problem_success))
                    )
                )
            ),
            arrayOf(
                "should trigger close with error on userId not found",
                initialState(),
                EffectEvent.UserIdNotFound,
                initialState().copy(
                    effects = initialEffectsState().copy(
                        closeWithMessage = Effect.of(TextUiModel.TextRes(R.string.report_a_problem_generic_error))
                    )
                )
            ),
            arrayOf(
                "should trigger offline error when connectivity not present",
                initialState(),
                EffectEvent.ErrorOnSubmission(DataError.Remote.NoNetwork),
                initialState().copy(
                    effects = initialEffectsState().copy(
                        submissionError = Effect.of(TextUiModel.TextRes(R.string.report_a_problem_offline_error))
                    )
                )
            ),
            arrayOf(
                "should trigger generic error when submission fails",
                initialState(),
                EffectEvent.ErrorOnSubmission(DataError.Local.NoDataCached),
                initialState().copy(
                    effects = initialEffectsState().copy(
                        submissionError = Effect.of(TextUiModel.TextRes(R.string.report_a_problem_generic_error))
                    )
                )
            ),
            arrayOf(
                "should highlight field when validation error occurs",
                initialState(),
                EffectEvent.OnFieldValidationFailed(BugReportFocusableField.Summary),
                initialState().copy(
                    effects = initialEffectsState().copy(
                        forceFocusField = Effect.of(BugReportFocusableField.Summary)
                    )
                )
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = mainEvents + effectEvents

        private fun initialMainState() = BugReportState.Main.initialState()
        private fun initialEffectsState() = BugReportState.Effects.initialState()
        private fun initialState() = BugReportStates(initialMainState(), initialEffectsState())

        private fun createValidationError(summaryError: Boolean) =
            initialState().main.validationErrors.copy(showSummaryError = summaryError)
    }
}
