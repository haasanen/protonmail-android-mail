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

import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.BugReportStateModifications
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.MainEvent
import ch.protonmail.android.mailbugreport.presentation.model.bugreport.operations.MainStateModification
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class BugReportMainEventTest(
    @Suppress("unused") private val testName: String,
    private val effect: MainEvent,
    private val expectedModification: BugReportStateModifications
) {

    @Test
    fun `should map to the correct modification`() {
        val actualModification = effect.toStateModifications()
        assertEquals(expectedModification, actualModification)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "loading triggered to modification (true)",
                MainEvent.LoadingToggled(true),
                BugReportStateModifications(
                    mainModification = MainStateModification.OnLoadingToggled(true)
                )
            ),
            arrayOf(
                "loading triggered to modification (false)",
                MainEvent.LoadingToggled(false),
                BugReportStateModifications(
                    mainModification = MainStateModification.OnLoadingToggled(false)
                )
            ),
            arrayOf(
                "summary error present to modification (false)",
                MainEvent.SummaryErrorToggled(false),
                BugReportStateModifications(
                    mainModification = MainStateModification.OnSummaryValidationErrorToggled(false)
                )
            ),
            arrayOf(
                "summary error present to modification (true)",
                MainEvent.SummaryErrorToggled(true),
                BugReportStateModifications(
                    mainModification = MainStateModification.OnSummaryValidationErrorToggled(true)
                )
            )
        )
    }
}
