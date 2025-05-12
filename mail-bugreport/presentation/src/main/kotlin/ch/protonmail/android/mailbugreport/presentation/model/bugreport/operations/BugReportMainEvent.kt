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

import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.MainStateModification.OnLoadingToggled
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportState

internal sealed interface MainEvent : BugReportEvent {

    override fun toStateModifications(): BugReportStateModifications = BugReportStateModifications(
        mainModification = when (this) {
            is LoadingToggled -> OnLoadingToggled(newValue)
            is SummaryErrorToggled -> MainStateModification.OnSummaryValidationErrorToggled(newValue)
        }
    )

    data class LoadingToggled(val newValue: Boolean) : MainEvent
    data class SummaryErrorToggled(val newValue: Boolean) : MainEvent
}

internal sealed interface MainStateModification : BugReportStateModification<BugReportState.Main> {
    data class OnLoadingToggled(val value: Boolean) : MainStateModification {

        override fun apply(state: BugReportState.Main) = state.copy(isLoading = value)
    }

    data class OnSummaryValidationErrorToggled(val value: Boolean) : MainStateModification {

        override fun apply(state: BugReportState.Main) = state.copy(
            validationErrors = state.validationErrors.copy(showSummaryError = value),
            isLoading = false
        )
    }
}
