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

package ch.protonmail.android.mailbugreport.domain.usecase

import ch.protonmail.android.mailbugreport.domain.model.IssueReport
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField
import ch.protonmail.android.mailcommon.domain.AppInformation
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CreateIssueReportTest {

    private val appInformation = AppInformation()
    private val createIssueReport = CreateIssueReport(appInformation)

    @Test
    fun `should generate the issue report entity`() {
        // Given
        val summary = IssueReportField.Summary("summary")
        val reproSteps = IssueReportField.StepsToReproduce("repro")
        val expectedResult = IssueReportField.ExpectedResult("expect")
        val actualResult = IssueReportField.ActualResult("actual")
        val includeLogs = IssueReportField.ShouldIncludeLogs(true)

        val expectedIssueReport = IssueReport(
            operatingSystem = IssueReportField.OperatingSystem("Android"),
            operatingSystemVersion = IssueReportField.OperatingSystemVersion("0"),
            client = IssueReportField.Client("Android Proton Mail"),
            clientVersion = IssueReportField.ClientVersion("0.0.0 (0)"),
            title = IssueReportField.Title("Proton Mail Android App - Bug report"),
            summary = summary,
            stepsToReproduce = reproSteps,
            expectedResult = expectedResult,
            actualResult = actualResult,
            shouldIncludeLogs = includeLogs
        )

        // When
        val actual = createIssueReport(summary, reproSteps, expectedResult, actualResult, includeLogs)

        // Then
        assertEquals(expectedIssueReport, actual)

    }
}
