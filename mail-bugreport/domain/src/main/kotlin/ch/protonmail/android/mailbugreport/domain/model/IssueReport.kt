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

package ch.protonmail.android.mailbugreport.domain.model

import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.ActualResult
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.Client
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.ClientVersion
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.ExpectedResult
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.OperatingSystem
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.OperatingSystemVersion
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.ShouldIncludeLogs
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.StepsToReproduce
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.Summary
import ch.protonmail.android.mailbugreport.domain.model.IssueReportField.Title

data class IssueReport(
    val operatingSystem: OperatingSystem = OperatingSystem("Android"),
    val operatingSystemVersion: OperatingSystemVersion,
    val client: Client = Client("Android_Native"),
    val clientVersion: ClientVersion,
    val title: Title = Title("Proton Mail Android App - Bug report"),
    val summary: Summary,
    val stepsToReproduce: StepsToReproduce,
    val expectedResult: ExpectedResult,
    val actualResult: ActualResult,
    val shouldIncludeLogs: ShouldIncludeLogs
)

object IssueReportField {
    @JvmInline
    value class OperatingSystem(val value: String)

    @JvmInline
    value class OperatingSystemVersion(val value: String)

    @JvmInline
    value class Client(val value: String)

    @JvmInline
    value class ClientVersion(val value: String)

    @JvmInline
    value class Title(val value: String)

    @JvmInline
    value class Summary(val value: String)

    @JvmInline
    value class StepsToReproduce(val value: String)

    @JvmInline
    value class ExpectedResult(val value: String)

    @JvmInline
    value class ActualResult(val value: String)

    @JvmInline
    value class ShouldIncludeLogs(val value: Boolean)
}
