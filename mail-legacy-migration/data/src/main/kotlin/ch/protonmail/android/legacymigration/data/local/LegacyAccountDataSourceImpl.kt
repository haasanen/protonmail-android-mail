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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.proton.core.account.data.db.AccountDatabase
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountMetadataDetails
import me.proton.core.account.domain.entity.SessionDetails
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

class LegacyAccountDataSourceImpl @Inject constructor(
    private val product: Product,
    private val db: AccountDatabase,
    private val keyStoreCrypto: KeyStoreCrypto
) : LegacyAccountDataSource {

    private val accountDao = db.accountDao()
    private val sessionDao = db.sessionDao()
    private val accountMetadataDao = db.accountMetadataDao()
    private val sessionDetailsDao = db.sessionDetailsDao()

    private suspend fun getSessionDetails(sessionId: SessionId): SessionDetails? =
        sessionDetailsDao.getBySessionId(sessionId)?.toSessionDetails()


    private suspend fun getAccountMetadataDetails(userId: UserId): AccountMetadataDetails? =
        accountMetadataDao.getByUserId(product, userId)?.toAccountMetadataDetails()

    private suspend fun getAccountInfo(entity: AccountEntity): AccountDetails {
        val sessionId = entity.sessionId
        return AccountDetails(
            account = getAccountMetadataDetails(entity.userId),
            session = sessionId?.let { getSessionDetails(it) }
        )
    }

    override fun getAccount(userId: UserId): Flow<Account?> = accountDao.findByUserId(userId)
        .map { account -> account?.toAccount(getAccountInfo(account)) }
        .distinctUntilChanged()

    override fun getSession(sessionId: SessionId): Flow<Session?> = sessionDao.findBySessionId(sessionId)
        .map { it?.toSession(keyStoreCrypto) }
        .distinctUntilChanged()

    override fun getPrimaryUserId(): Flow<UserId?> = accountMetadataDao.observeLatestPrimary(product)
        .map { account -> account?.userId.takeIf { account?.migrations == null } }
        .distinctUntilChanged()

    override fun getSessions(): Flow<List<Session>> = sessionDao.findAll(product).map { list ->
        list.map {
            it.toSession(keyStoreCrypto)
        }
    }.distinctUntilChanged()
}
