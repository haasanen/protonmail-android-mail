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

import arrow.core.right
import ch.protonmail.android.mailbugreport.domain.model.IssueReport
import ch.protonmail.android.mailbugreport.domain.repository.BugReportRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import kotlin.test.Test
import kotlin.test.assertTrue

internal class SubmitIssueReportTest {

    private val bugReportRepository = mockk<BugReportRepository>()
    private val submitIssueReport = SubmitIssueReport(bugReportRepository)

    @Test
    fun `should call the repository`() = runTest {
        // Given
        val userId = mockk<UserId>()
        val report = mockk<IssueReport>()
        coEvery { bugReportRepository.submitBugReport(userId, report) } returns Unit.right()

        // When
        val result = submitIssueReport(userId, report)

        // Then
        assertTrue(result.isRight())
        coVerify { bugReportRepository.submitBugReport(userId, report) }
    }
}
