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

package ch.protonmail.android.mailnotifications.data.repository

import ch.protonmail.android.mailnotifications.data.local.NotificationsPermissionLocalDataSource
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

internal class NotificationsPermissionRepositoryTest {

    private val dataSource = mockk<NotificationsPermissionLocalDataSource>(relaxUnitFun = true)
    private val repository = NotificationsPermissionRepositoryImpl(dataSource)

    @Test
    fun `should proxy the attempts observation to the datasource`() {
        // Given
        every { dataSource.observePermissionRequestAttempts() } returns flowOf(mockk())

        // When
        repository.observePermissionsRequestsAttempts()

        // Then
        verify(exactly = 1) { dataSource.observePermissionRequestAttempts() }
        confirmVerified(dataSource)
    }

    @Test
    fun `should proxy the attempts increase to the datasource`() = runTest {
        // When
        repository.increasePermissionsRequestAttempts()

        // Then
        coVerify(exactly = 1) { dataSource.increasePermissionRequestAttempts() }
        confirmVerified(dataSource)
    }
}
