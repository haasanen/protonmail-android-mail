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

package ch.protonmail.android.navigation

import app.cash.turbine.test
import ch.protonmail.android.legacymigration.domain.usecase.ObserveLegacyMigrationStatus
import ch.protonmail.android.legacymigration.domain.usecase.SetLegacyMigrationStatus
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailnotifications.permissions.NotificationsPermissionOrchestrator
import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.usecase.SetPrimaryAccount
import ch.protonmail.android.navigation.model.LauncherState
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.every
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.android.core.auth.presentation.AuthOrchestrator
import me.proton.android.core.payment.presentation.PaymentOrchestrator
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import ch.protonmail.android.legacymigration.domain.model.LegacyMigrationStatus
import ch.protonmail.android.legacymigration.domain.usecase.MigrateLegacyApplication
import ch.protonmail.android.legacymigration.domain.usecase.ShouldMigrateLegacyAccount
import kotlinx.coroutines.flow.MutableSharedFlow

@ExperimentalCoroutinesApi
class LauncherViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authOrchestrator = mockk<AuthOrchestrator>()
    private val paymentOrchestrator = mockk<PaymentOrchestrator>()
    private val setPrimaryAccount = mockk<SetPrimaryAccount>()
    private val userSessionRepository = mockk<UserSessionRepository>()
    private val notificationsPermissionOrchestrator = mockk<NotificationsPermissionOrchestrator>(relaxUnitFun = true)
    private val observeLegacyMigrationStatus = mockk<ObserveLegacyMigrationStatus>()
    private val setLegacyMigrationStatus = mockk<SetLegacyMigrationStatus>(relaxUnitFun = true)
    private val migrateLegacyApplication = mockk<MigrateLegacyApplication>(relaxUnitFun = true)
    private val shouldMigrateLegacyAccount = mockk<ShouldMigrateLegacyAccount>()

    private lateinit var viewModel: LauncherViewModel

    private val readyAccount = Account(
        userId = UserIdSample.Primary,
        name = "User",
        state = AccountState.Ready,
        primaryAddress = "address"
    )

    @Test
    fun `state should be AccountNeeded when userSession is not available`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { observeLegacyMigrationStatus() } returns flowOf(LegacyMigrationStatus.Done)
            every { userSessionRepository.observeAccounts() } returns flowOf(emptyList())

            viewModel = LauncherViewModel(
                authOrchestrator,
                paymentOrchestrator,
                setPrimaryAccount,
                userSessionRepository,
                notificationsPermissionOrchestrator,
                observeLegacyMigrationStatus,
                setLegacyMigrationStatus,
                migrateLegacyApplication,
                shouldMigrateLegacyAccount
            )

            viewModel.state.test {
                assertEquals(LauncherState.AccountNeeded, awaitItem())
            }
        }

    @Test
    fun `state should be PrimaryExist when userSession is available`() = runTest(mainDispatcherRule.testDispatcher) {
        every { observeLegacyMigrationStatus() } returns flowOf(LegacyMigrationStatus.Done)
        every { userSessionRepository.observeAccounts() } returns flowOf(listOf(readyAccount))

        viewModel = LauncherViewModel(
            authOrchestrator,
            paymentOrchestrator,
            setPrimaryAccount,
            userSessionRepository,
            notificationsPermissionOrchestrator,
            observeLegacyMigrationStatus,
            setLegacyMigrationStatus,
            migrateLegacyApplication,
            shouldMigrateLegacyAccount
        )

        viewModel.state.test {
            assertEquals(LauncherState.PrimaryExist, awaitItem())
        }
    }

    @Test
    fun `migration is triggered when status is NotDone`() = runTest(mainDispatcherRule.testDispatcher) {
        val migrationStatusFlow = MutableSharedFlow<LegacyMigrationStatus>()
        every { observeLegacyMigrationStatus() } returns migrationStatusFlow
        every { userSessionRepository.observeAccounts() } returns flowOf(listOf(readyAccount))
        coEvery { migrateLegacyApplication() } returns Unit

        viewModel = LauncherViewModel(
            authOrchestrator,
            paymentOrchestrator,
            setPrimaryAccount,
            userSessionRepository,
            notificationsPermissionOrchestrator,
            observeLegacyMigrationStatus,
            setLegacyMigrationStatus,
            migrateLegacyApplication,
            shouldMigrateLegacyAccount
        )

        viewModel.state.test {
            skipItems(1)
            migrationStatusFlow.emit(LegacyMigrationStatus.NotDone)

            assertEquals(LauncherState.MigrationInProgress, awaitItem())
            assertEquals(LauncherState.PrimaryExist, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { migrateLegacyApplication() }
        coVerify(exactly = 1) { setLegacyMigrationStatus(LegacyMigrationStatus.Done) }
    }
}
