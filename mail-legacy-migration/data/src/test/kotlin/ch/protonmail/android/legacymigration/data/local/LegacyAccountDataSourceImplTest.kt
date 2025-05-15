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

package ch.protonmail.android.legacymigration.data.local

import io.mockk.every
import io.mockk.mockk
import me.proton.core.account.data.db.AccountDao
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.db.AccountMetadataDao
import me.proton.core.account.data.db.SessionDao
import me.proton.core.account.data.db.SessionDetailsDao
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.Product
import org.junit.Rule
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import kotlinx.coroutines.flow.flowOf
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.data.entity.AccountMetadataEntity
import me.proton.core.account.data.entity.SessionDetailsEntity
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountMetadataDetails
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId

class LegacyAccountDataSourceImplTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val product: Product = Product.Mail
    private val keyStoreCrypto: KeyStoreCrypto = mockk()

    private val accountDao: AccountDao = mockk()
    private val sessionDao: SessionDao = mockk()
    private val accountMetadataDao: AccountMetadataDao = mockk()
    private val sessionDetailsDao: SessionDetailsDao = mockk()

    private val accountDatabase: AccountDatabase = mockk {
        every { accountDao() } returns accountDao
        every { sessionDao() } returns sessionDao
        every { accountMetadataDao() } returns accountMetadataDao
        every { sessionDetailsDao() } returns sessionDetailsDao
    }

    private val dataSource = LegacyAccountDataSourceImpl(
        product = product,
        db = accountDatabase,
        keyStoreCrypto = keyStoreCrypto
    )

    @Test
    fun `getPrimaryUserId returns primary userId`() = runTest {
        // Given
        val userId = UserId("user123")
        val metadata = mockk<AccountMetadataEntity> {
            every { this@mockk.userId } returns userId
            every { this@mockk.migrations } returns null
        }
        every { accountMetadataDao.observeLatestPrimary(product) } returns flowOf(metadata)

        // When
        dataSource.getPrimaryUserId().test {

            // Then
            assertEquals(userId, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getSession returns session`() = runTest {
        // Given
        val sessionId = SessionId("session123")
        val sessionEntity = mockk<SessionEntity>()
        val session = mockk<Session>()

        every { sessionDao.findBySessionId(sessionId) } returns flowOf(sessionEntity)
        every { sessionEntity.toSession(keyStoreCrypto) } returns session

        // When
        dataSource.getSession(sessionId).test {
            // Then
            assertEquals(session, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getAccount returns account with details`() = runTest {
        // Given
        val userId = UserId("user123")
        val sessionId = SessionId("session123")

        val accountEntity = mockk<AccountEntity> {
            every { this@mockk.userId } returns userId
            every { this@mockk.sessionId } returns sessionId
        }
        val sessionDetailsEntity = mockk<SessionDetailsEntity>()
        val sessionDetails = mockk<SessionDetails>()
        val accountMetadataEntity = mockk<AccountMetadataEntity>()
        val accountMetadata = mockk<AccountMetadataDetails>()
        val account = mockk<Account>()

        every { accountDao.findByUserId(userId) } returns flowOf(accountEntity)
        coEvery { sessionDetailsDao.getBySessionId(sessionId) } returns sessionDetailsEntity
        every { sessionDetailsEntity.toSessionDetails() } returns sessionDetails
        coEvery { accountMetadataDao.getByUserId(product, userId) } returns accountMetadataEntity
        every { accountMetadataEntity.toAccountMetadataDetails() } returns accountMetadata
        every { accountEntity.toAccount(AccountDetails(accountMetadata, sessionDetails)) } returns account

        // When
        dataSource.getAccount(userId).test {
            // Then
            assertEquals(account, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
