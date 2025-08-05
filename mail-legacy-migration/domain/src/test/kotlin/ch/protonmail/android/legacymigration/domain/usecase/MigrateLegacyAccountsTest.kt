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

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.domain.model.AccountMigrationInfo
import ch.protonmail.android.legacymigration.domain.model.AccountPasswordMode
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.legacymigration.domain.model.LegacySessionInfo
import ch.protonmail.android.legacymigration.domain.repository.LegacyAccountRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import kotlin.test.Test

class MigrateLegacyAccountsTest {

    private val legacyAccountRepository: LegacyAccountRepository = mockk()
    private val setPrimaryAccountAfterMigration: SetPrimaryAccountAfterMigration = mockk()

    private val userId1 = UserId("user-id-1")
    private val userId2 = UserId("user-id-2")
    private val sessionId1 = SessionId("session-id-1")
    private val sessionId2 = SessionId("session-id-2")

    private val session1 = LegacySessionInfo(
        userId = userId1,
        sessionId = sessionId1,
        refreshToken = "refresh-token-1",
        twoPassModeEnabled = true
    )

    private val session2 = LegacySessionInfo(
        userId = userId2,
        sessionId = sessionId2,
        refreshToken = "refresh-token-2",
        twoPassModeEnabled = false
    )

    private val migrationInfo1 = AccountMigrationInfo(
        userId = userId1,
        username = "user1",
        primaryAddr = "user1@example.com",
        displayName = "User One",
        sessionId = sessionId1,
        refreshToken = "token1",
        keySecret = "keySecret1",
        passwordMode = AccountPasswordMode.TWO,
        isPrimaryUser = true,
        addressSignatureEnabled = true,
        mobileSignatureEnabled = true,
        mobileSignature = "Sent from Proton"
    )

    private val migrationInfo2 = AccountMigrationInfo(
        userId = userId2,
        username = "user2",
        primaryAddr = "user2@example.com",
        displayName = "User Two",
        sessionId = sessionId2,
        refreshToken = "token2",
        keySecret = "keySecret2",
        passwordMode = AccountPasswordMode.ONE,
        isPrimaryUser = false,
        addressSignatureEnabled = false,
        mobileSignatureEnabled = false,
        mobileSignature = null
    )

    private val legacyDbError: MigrationError = MigrationError.LegacyDbFailure.MissingUser

    private val migrateLegacyAccounts = MigrateLegacyAccounts(
        legacyAccountRepository, setPrimaryAccountAfterMigration
    )

    @Test
    fun `Should migrate all accounts successfully`() = runTest {
        // Given
        coEvery { legacyAccountRepository.getAuthenticatedLegacySessions() } returns listOf(session1, session2)
        coEvery { legacyAccountRepository.getLegacyAccountMigrationInfoFor(session1) } returns migrationInfo1.right()
        coEvery { legacyAccountRepository.getLegacyAccountMigrationInfoFor(session2) } returns migrationInfo2.right()
        coEvery { legacyAccountRepository.migrateLegacyAccount(migrationInfo1) } returns Unit.right()
        coEvery { legacyAccountRepository.migrateLegacyAccount(migrationInfo2) } returns Unit.right()
        coEvery { setPrimaryAccountAfterMigration(any()) } just Runs

        // When
        val result = migrateLegacyAccounts()

        // Then
        assertEquals(Unit.right(), result)
        coVerify(exactly = 1) { setPrimaryAccountAfterMigration(any()) }
    }

    @Test
    fun `migration fails when fetching all migration info fail`() = runTest {
        // Given
        coEvery { legacyAccountRepository.getAuthenticatedLegacySessions() } returns listOf(session1, session2)
        coEvery { legacyAccountRepository.getLegacyAccountMigrationInfoFor(any()) } returns legacyDbError.left()
        coEvery { setPrimaryAccountAfterMigration(any()) } just Runs

        // When
        val result = migrateLegacyAccounts()

        // Then
        assertEquals(listOf(legacyDbError, legacyDbError).left(), result)
        coVerify(exactly = 0) { legacyAccountRepository.migrateLegacyAccount(any()) }
    }

    @Test
    fun `Overall migration will be successful if at least one migration is successful`() = runTest {
        // Given
        coEvery { legacyAccountRepository.getAuthenticatedLegacySessions() } returns listOf(session1, session2)
        coEvery { legacyAccountRepository.getLegacyAccountMigrationInfoFor(session1) } returns migrationInfo1.right()
        coEvery { legacyAccountRepository.getLegacyAccountMigrationInfoFor(session2) } returns legacyDbError.left()
        coEvery { legacyAccountRepository.migrateLegacyAccount(migrationInfo1) } returns Unit.right()
        coEvery { setPrimaryAccountAfterMigration(any()) } just Runs

        // When
        val result = migrateLegacyAccounts()

        // Then
        assertEquals(Unit.right(), result)
    }
}
