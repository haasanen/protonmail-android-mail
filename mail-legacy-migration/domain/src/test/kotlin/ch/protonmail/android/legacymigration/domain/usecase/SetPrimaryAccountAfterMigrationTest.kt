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

package ch.protonmail.android.legacymigration.domain.usecase

import ch.protonmail.android.legacymigration.domain.model.AccountMigrationInfo
import ch.protonmail.android.legacymigration.domain.model.AccountPasswordMode
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import kotlin.test.Test
import io.mockk.Runs
import io.mockk.just

class SetPrimaryAccountAfterMigrationTest {

    private val userSessionRepository: UserSessionRepository = mockk(relaxed = true)
    private val setPrimaryAccountAfterMigration = SetPrimaryAccountAfterMigration(userSessionRepository)

    private val primarySessionId = SessionId("session-primary")
    private val primaryUserId = UserId("user-primary")

    private val primaryAccount = AccountMigrationInfo(
        userId = primaryUserId,
        username = "user1",
        primaryAddr = "user1@example.com",
        displayName = "User One",
        sessionId = primarySessionId,
        refreshToken = "refresh1",
        keySecret = "secret1",
        passwordMode = AccountPasswordMode.ONE,
        isPrimaryUser = true
    )

    private val secondaryAccount = AccountMigrationInfo(
        userId = primaryUserId,
        username = "user2",
        primaryAddr = "user2@example.com",
        displayName = "User Two",
        sessionId = SessionId("session-secondary"),
        refreshToken = "refresh2",
        keySecret = "secret2",
        passwordMode = AccountPasswordMode.ONE,
        isPrimaryUser = false
    )

    @Test
    fun `sets primary account successfully`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserId(primarySessionId) } returns primaryUserId
        coEvery { userSessionRepository.setPrimaryAccount(primaryUserId) } just Runs

        val migratedList = listOf(secondaryAccount, primaryAccount)

        // When
        setPrimaryAccountAfterMigration(migratedList)

        // Then
        coVerify(exactly = 1) { userSessionRepository.getUserId(primarySessionId) }
        coVerify(exactly = 1) { userSessionRepository.setPrimaryAccount(primaryUserId) }
    }

    @Test
    fun `does nothing logs error when no primary account is in list`() = runTest {
        // Given
        val migratedList = listOf(secondaryAccount)

        // When
        setPrimaryAccountAfterMigration(migratedList)

        // Then
        coVerify(exactly = 0) { userSessionRepository.getUserId(any()) }
        coVerify(exactly = 0) { userSessionRepository.setPrimaryAccount(any()) }
    }

    @Test
    fun `does nothing logs error when primary account found but user id look up fails`() = runTest {
        // Given
        coEvery { userSessionRepository.getUserId(primarySessionId) } returns null
        val migratedList = listOf(primaryAccount)

        // When
        setPrimaryAccountAfterMigration(migratedList)

        // Then
        coVerify(exactly = 1) { userSessionRepository.getUserId(primarySessionId) }
        coVerify(exactly = 0) { userSessionRepository.setPrimaryAccount(any()) }
    }
}
