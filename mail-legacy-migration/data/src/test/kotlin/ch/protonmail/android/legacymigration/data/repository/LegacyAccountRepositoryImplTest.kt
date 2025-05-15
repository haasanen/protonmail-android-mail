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

import ch.protonmail.android.legacymigration.data.local.LegacyAccountDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyUserAddressDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyUserDataSource
import ch.protonmail.android.legacymigration.data.mapper.MigrationInfoMapper
import ch.protonmail.android.legacymigration.domain.model.AccountMigrationInfo
import ch.protonmail.android.legacymigration.domain.model.LegacyUserAddressInfo
import ch.protonmail.android.legacymigration.domain.model.LegacyUserInfo
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.domain.entity.UserId
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.domain.model.AccountPasswordMode
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsession.data.wrapper.LoginFlowWrapper
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import org.junit.Rule
import org.junit.Test
import ch.protonmail.android.mailsession.domain.model.LoginError
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountMetadataDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.AddressId

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
    private val session = Session.Authenticated(
        userId = userId,
        sessionId = sessionId,
        accessToken = "access",
        refreshToken = "refresh-token",
        scopes = listOf("mail")
    )

    private val account = Account(
        userId = userId,
        username = username,
        email = email,
        state = AccountState.Ready,
        sessionId = sessionId,
        sessionState = SessionState.Authenticated,
        details = AccountDetails(
            account = AccountMetadataDetails(1_708_000_000, emptyList()),
            session = SessionDetails(
                initialEventId = "event-001",
                requiredAccountType = AccountType.Internal,
                secondFactorEnabled = true,
                twoPassModeEnabled = false,
                passphrase = EncryptedByteArray(ByteArray(32)),
                password = null,
                fido2AuthenticationOptionsJson = "{}"
            )
        )
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
        order = 0,
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
        passwordMode = AccountPasswordMode.ONE
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
        every { accountDataSource.getSessions() } returns flowOf(listOf(session))

        // When
        val result = repository.hasLegacyLoggedInAccounts()

        // Then
        assertTrue(result)
    }

    @Test
    fun `hasLegacyLoggedInAccounts returns false`() = runTest {
        // Given
        every { accountDataSource.getSessions() } returns flowOf(emptyList())

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
        every { accountDataSource.getAccount(userId) } returns flowOf(account)
        every { accountDataSource.getPrimaryUserId() } returns flowOf(userId)
        every {
            migrationInfoMapper.mapToAccountMigrationInfo(
                session, account, user, userAddress, isPrimaryUser = true
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
    fun `getLegacyAccountMigrationInfoFor fails with MissingAccount`() = runTest {
        // Given
        coEvery { userDataSource.getUser(userId) } returns user
        coEvery { userAddressDataSource.getPrimaryUserAddress(userId) } returns userAddress
        every { accountDataSource.getAccount(userId) } returns flowOf()
        every { accountDataSource.getPrimaryUserId() } returns flowOf(userId)

        // When
        val result = repository.getLegacyAccountMigrationInfoFor(session)

        // Then
        assertEquals(MigrationError.LegacyDbFailure.MissingAccount.left(), result)
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
        } returns LoginError.InvalidCredentials.left()

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
        coEvery { wrapper.migrate(any()) } returns LoginError.UnsupportedTwoFactorAuthentication.left()

        // When
        val result = repository.migrateLegacyAccount(accountInfo)

        // Then
        assertEquals(MigrationError.MigrateFailed.UnsupportedTwoFactorAuth.left(), result)
    }
}
