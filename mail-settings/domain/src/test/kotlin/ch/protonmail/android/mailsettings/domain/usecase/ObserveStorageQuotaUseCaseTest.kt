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

package ch.protonmail.android.mailsettings.domain.usecase

import app.cash.turbine.test
import ch.protonmail.android.mailsession.domain.model.User
import ch.protonmail.android.mailsettings.domain.repository.FakeUserSessionRepository
import ch.protonmail.android.mailsettings.domain.usecase.scenario.UserStorageQuotaScenario
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import kotlin.enums.EnumEntries
import kotlin.test.assertEquals

internal class ObserveStorageQuotaUseCaseTest {

    @Test
    fun `emits correct StorageQuotaResult based on the scenario`() = runTest {
        val testCases: EnumEntries<UserStorageQuotaScenario> = UserStorageQuotaScenario.entries

        testCases.forEach { scenario ->

            val observeStorageQuotaUseCase = buildObserveStorageQuotaUseCase(
                primaryUserId = scenario.primaryUserId,
                userList = scenario.userList
            )

            observeStorageQuotaUseCase().test {
                assertEquals(
                    expected = scenario.expectedStorageQuota,
                    actual = awaitItem(),
                    message = "Test case [$scenario] failed!"
                )
                awaitComplete()
            }
        }
    }

    private fun buildObserveStorageQuotaUseCase(
        primaryUserId: UserId?,
        userList: List<User> = emptyList()
    ): ObserveStorageQuotaUseCase = ObserveStorageQuotaUseCase(
        userSessionRepository = FakeUserSessionRepository(
            primaryUserId = primaryUserId,
            userList = userList
        )
    )
}
