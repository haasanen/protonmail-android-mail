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

import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.BugReportEvent
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.state.BugReportStates
import javax.inject.Inject

internal class BugReportFormReducer @Inject constructor() {

    internal fun reduceNewState(currentStates: BugReportStates, event: BugReportEvent): BugReportStates {
        val modifications = event.toStateModifications()

        return currentStates.copy(
            main = modifications.mainModification?.apply(currentStates.main)
                ?: currentStates.main,
            effects = modifications.effectsModification?.apply(currentStates.effects)
                ?: currentStates.effects
        )
    }
}
