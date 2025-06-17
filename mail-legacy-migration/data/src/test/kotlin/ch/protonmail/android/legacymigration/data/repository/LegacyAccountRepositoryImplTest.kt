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

package ch.protonmail.android.legacymigration.data.repository

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.data.local.LegacyAccountDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyUserAddressDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyUserDataSource
import ch.protonmail.android.legacymigration.data.mapper.MigrationInfoMapper
import ch.protonmail.android.legacymigration.domain.model.AccountMigrationInfo
import ch.protonmail.android.legacymigration.domain.model.AccountPasswordMode
import ch.protonmail.android.legacymigration.domain.model.LegacySessionInfo
import ch.protonmail.android.legacymigration.domain.model.LegacyUserAddressInfo
import ch.protonmail.android.legacymigration.domain.model.LegacyUserInfo
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.model.NetworkError
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsession.data.wrapper.LoginFlowWrapper
import ch.protonmail.android.mailsession.domain.model.LoginError
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.AddressId
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LegacyAccountRepositoryImplTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val accountDataSource: LegacyAccountDataSource = mockk()
    private val userDataSource: LegacyUserDataSource = mockk()
    private val userAddressDataSource: LegacyUserAddressDataSource = mockk()
    private val migrationInfoMapper: MigrationInfoMapper = mockk()
    private val mailSessionRepository: MailSessionRepository = mockk()

    private val userId = UserId("user123")
    private val sessionId = SessionId("session123")
    private val username = "john.doe"
    private val email = "john.doe@protonmail.com"

    private val session = LegacySessionInfo(
        userId = userId,
        sessionId = sessionId,
        refreshToken = "refresh-token",
        twoPassModeEnabled = false
    )

    private val user = LegacyUserInfo(
        userId = userId,
        passPhrase = "secret",
        email = email,
        name = "John",
        displayName = "Johnny"
    )

    private val userAddress = LegacyUserAddressInfo(
        addressId = AddressId("address123"),
        email = email,
        userId = userId,
        displayName = "Johnny"
    )

    private val accountInfo = AccountMigrationInfo(
        userId = userId,
        username = username,
        primaryAddr = email,
        displayName = "Johnny",
        sessionId = sessionId,
        refreshToken = "refresh-token",
        keySecret = "secret",
        passwordMode = AccountPasswordMode.ONE,
        isPrimaryUser = true
    )

    private val repository = LegacyAccountRepositoryImpl(
        accountDataSource,
        userDataSource,
        userAddressDataSource,
        migrationInfoMapper,
        mailSessionRepository
    )

    @Test
    fun `hasLegacyLoggedInAccounts returns true`() = runTest {
        // Given
        coEvery { accountDataSource.getSessions() } returns listOf(session)

        // When
        val result = repository.hasLegacyLoggedInAccounts()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasLegacyLoggedInAccounts returns false`() = runTest {
        // Given
        coEvery { accountDataSource.getSessions() } returns emptyList()

        // When
        val result = repository.hasLegacyLoggedInAccounts()

        // Then
        assertFalse(result)
    }

    @Test
    fun `getLegacyAccountMigrationInfoFor returns Right when all data present`() = runTest {
        // Given
        coEvery { userDataSource.getUser(userId) } returns user
        coEvery { userAddressDataSource.getPrimaryUserAddress(userId) } returns userAddress
        coEvery { accountDataSource.getPrimaryUserId() } returns userId
        every {
            migrationInfoMapper.mapToAccountMigrationInfo(
                sessionInfo = session,
                user = user,
                userAddress = userAddress,
                isPrimaryUser = true
            )
        } returns accountInfo

        // When
        val result = repository.getLegacyAccountMigrationInfoFor(session)

        // Then
        assertEquals(accountInfo.right(), result)
    }

    @Test
    fun `getLegacyAccountMigrationInfoFor fails with MissingUser`() = runTest {
        // Given
        coEvery { userDataSource.getUser(userId) } returns null

        // When
        val result = repository.getLegacyAccountMigrationInfoFor(session)

        // Then
        assertEquals(MigrationError.LegacyDbFailure.MissingUser.left(), result)
    }

    @Test
    fun `getLegacyAccountMigrationInfoFor fails with MissingUserAddress`() = runTest {
        // Given
        coEvery { userDataSource.getUser(userId) } returns user
        coEvery { userAddressDataSource.getPrimaryUserAddress(userId) } returns null

        // When
        val result = repository.getLegacyAccountMigrationInfoFor(session)

        // Then
        assertEquals(MigrationError.LegacyDbFailure.MissingUserAddress.left(), result)
    }

    @Test
    fun `migrateLegacyAccount returns Right`() = runTest {
        // Given
        val wrapper = mockk<LoginFlowWrapper>()
        coEvery { mailSessionRepository.getMailSession().newLoginFlow() } returns wrapper.right()
        coEvery { wrapper.migrate(any()) } returns Unit.right()

        // When
        val result = repository.migrateLegacyAccount(accountInfo)

        // Then
        assertTrue(result.isRight())
    }

    @Test
    fun `migration fails due to login flow error`() = runTest {
        // Given
        coEvery {
            mailSessionRepository.getMailSession().newLoginFlow()
        } returns DataError.Remote.Http(NetworkError.UnprocessableEntity).left()

        // When
        val result = repository.migrateLegacyAccount(accountInfo)

        // Then
        assertEquals(MigrationError.LoginFlowFailed, result.leftOrNull())
    }

    @Test
    fun `migration fails with mapped login error`() = runTest {
        // Given
        val wrapper = mockk<LoginFlowWrapper>()
        coEvery { mailSessionRepository.getMailSession().newLoginFlow() } returns wrapper.right()
        coEvery { wrapper.migrate(any()) } returns LoginError.AuthenticationFailure.left()

        // When
        val result = repository.migrateLegacyAccount(accountInfo)

        // Then
        assertEquals(MigrationError.MigrateFailed.AuthenticationFailure.left(), result)
    }
}
