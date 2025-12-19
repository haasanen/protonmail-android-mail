/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.maildetail.presentation.usecase

import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maildetail.domain.usecase.IsShowSingleMessageMode
import ch.protonmail.android.maillabel.domain.model.ViewMode
import ch.protonmail.android.mailsettings.domain.usecase.GetUserPreferredViewMode
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IsShowSingleMessageModeTest {

    private val getUserPreferredViewMode = mockk<GetUserPreferredViewMode>()

    private val isShowSingleMessageMode = IsShowSingleMessageMode(getUserPreferredViewMode)

    @Test
    fun `returns false when preferred mode is conversation mode`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        coEvery { getUserPreferredViewMode(userId) } returns ViewMode.ConversationGrouping

        // When
        val actual = isShowSingleMessageMode(userId)

        // Then
        assertFalse(actual)
    }

    @Test
    fun `returns true when preferred mode is message mode`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        coEvery { getUserPreferredViewMode(userId) } returns ViewMode.NoConversationGrouping

        // When
        val actual = isShowSingleMessageMode(userId)

        // Then
        assertTrue(actual)
    }
}
