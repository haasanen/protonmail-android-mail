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

package ch.protonmail.android.mailnotifications.presentation

import app.cash.turbine.test
import ch.protonmail.android.mailnotifications.data.model.NotificationsPermissionRequestAttempts
import ch.protonmail.android.mailnotifications.data.repository.NotificationsPermissionRepository
import ch.protonmail.android.mailnotifications.domain.proxy.NotificationManagerCompatProxy
import ch.protonmail.android.mailnotifications.presentation.model.NotificationsPermissionState
import ch.protonmail.android.mailnotifications.presentation.model.PermissionRequestedHolder
import ch.protonmail.android.mailnotifications.presentation.model.NotificationsPermissionStateType
import ch.protonmail.android.mailnotifications.presentation.viewmodel.NotificationsPermissionViewModel
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NotificationsPermissionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val notificationsPermissionRepository = mockk<NotificationsPermissionRepository>()
    private val notificationManagerCompatProxy = mockk<NotificationManagerCompatProxy>()
    private val permissionRequestedHolder = mockk<PermissionRequestedHolder>()

    private lateinit var viewModel: NotificationsPermissionViewModel

    @BeforeTest
    fun setup() {
        viewModel = NotificationsPermissionViewModel(
            notificationsPermissionRepository,
            notificationManagerCompatProxy,
            permissionRequestedHolder
        )
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should emit granted state when notifications are enabled`() = runTest {
        // Given
        every { notificationManagerCompatProxy.areNotificationsEnabled() } returns true

        // When + Then
        viewModel.state.test {
            assertEquals(NotificationsPermissionState.Granted, awaitItem())
        }
    }

    @Test
    fun `should emit no action state on permission required but already asked in the session`() = runTest {
        // Given
        every { notificationManagerCompatProxy.areNotificationsEnabled() } returns false
        every { permissionRequestedHolder.value } returns MutableStateFlow(true)
        every {
            notificationsPermissionRepository.observePermissionsRequestsAttempts()
        } returns flowOf(NotificationsPermissionRequestAttempts(0))

        // When + Then
        viewModel.state.test {
            assertEquals(NotificationsPermissionState.NoAction, awaitItem())
        }
    }

    @Test
    fun `should emit first time request state on permission required and not asked in the session`() = runTest {
        // Given
        every { notificationManagerCompatProxy.areNotificationsEnabled() } returns false
        every { permissionRequestedHolder.value } returns MutableStateFlow(false)
        every {
            notificationsPermissionRepository.observePermissionsRequestsAttempts()
        } returns flowOf(NotificationsPermissionRequestAttempts(0))

        val expectedState = NotificationsPermissionState.RequiresInteraction(NotificationsPermissionStateType.FirstTime)

        // When + Then
        viewModel.state.test {
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `should emit second time request state on permission required and not asked in the session`() = runTest {
        // Given
        every { notificationManagerCompatProxy.areNotificationsEnabled() } returns false
        every { permissionRequestedHolder.value } returns MutableStateFlow(false)
        every {
            notificationsPermissionRepository.observePermissionsRequestsAttempts()
        } returns flowOf(NotificationsPermissionRequestAttempts(1))

        val expectedState =
            NotificationsPermissionState.RequiresInteraction(NotificationsPermissionStateType.SecondTime)

        // When + Then
        viewModel.state.test {
            assertEquals(expectedState, awaitItem())
        }
    }

    @Test
    fun `should emit denied request state on permission required over threshold`() = runTest {
        // Given
        every { notificationManagerCompatProxy.areNotificationsEnabled() } returns false
        every { permissionRequestedHolder.value } returns MutableStateFlow(false)
        every {
            notificationsPermissionRepository.observePermissionsRequestsAttempts()
        } returns flowOf(NotificationsPermissionRequestAttempts(2))

        // When + Then
        viewModel.state.test {
            assertEquals(NotificationsPermissionState.Denied, awaitItem())
        }
    }

    @Test
    fun `should track the permission request display`() = runTest {
        // Given
        every { notificationManagerCompatProxy.areNotificationsEnabled() } returns true
        every { permissionRequestedHolder.trackRequest() } just runs
        coEvery { notificationsPermissionRepository.increasePermissionsRequestAttempts() } just runs

        // When + Then
        viewModel.state.test {
            assertEquals(NotificationsPermissionState.Granted, awaitItem())
            viewModel.trackPermissionRequested()
        }

        coVerify(ordering = Ordering.SEQUENCE) {
            permissionRequestedHolder.trackRequest()
            notificationsPermissionRepository.increasePermissionsRequestAttempts()
        }
        confirmVerified(notificationsPermissionRepository, permissionRequestedHolder)
    }
}
