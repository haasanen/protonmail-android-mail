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

package ch.protonmail.android.mailbugreport.data.repository

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailbugreport.data.local.RustBugReportDataSource
import ch.protonmail.android.mailbugreport.data.mapper.toLocalIssueReport
import ch.protonmail.android.mailbugreport.domain.model.IssueReport
import ch.protonmail.android.mailcommon.data.mapper.LocalIssueReport
import ch.protonmail.android.mailcommon.domain.model.DataError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

internal class BugReportRepositoryImplTest {

    private val datasource = mockk<RustBugReportDataSource>()
    private val repository = BugReportRepositoryImpl(datasource)

    @BeforeTest
    fun setup() {
        mockkStatic("ch.protonmail.android.mailbugreport.data.mapper.IssueReportMapperKt")
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should proxy the call to the datasource (success)`() = runTest {
        // Given
        coEvery { datasource.submitBugReport(any(), any()) } returns Unit.right()

        val issueReport = mockk<IssueReport>()
        val rustIssueReport = mockk<LocalIssueReport>()

        every { issueReport.toLocalIssueReport() } returns rustIssueReport
        // When
        val result = repository.submitBugReport(UserId, issueReport)

        // Then
        assertTrue(result.isRight())
        coVerify { datasource.submitBugReport(UserId, rustIssueReport) }
    }

    @Test
    fun `should proxy the call to the datasource (error)`() = runTest {
        // Given
        coEvery { datasource.submitBugReport(any(), any()) } returns DataError.Local.NoUserSession.left()

        val issueReport = mockk<IssueReport>()
        val rustIssueReport = mockk<LocalIssueReport>()

        every { issueReport.toLocalIssueReport() } returns rustIssueReport
        // When
        val result = repository.submitBugReport(UserId, issueReport)

        // Then
        assertTrue(result.isLeft())
        coVerify { datasource.submitBugReport(UserId, rustIssueReport) }
    }

    private companion object {

        val UserId = UserId("")
    }
}
