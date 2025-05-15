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

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.data.local.LegacyAccountDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyUserAddressDataSource
import ch.protonmail.android.legacymigration.data.local.LegacyUserDataSource
import ch.protonmail.android.legacymigration.data.mapper.MigrationInfoMapper
import ch.protonmail.android.legacymigration.data.mapper.toMigrationData
import ch.protonmail.android.legacymigration.domain.model.AccountMigrationInfo
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.legacymigration.domain.repository.LegacyAccountRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import ch.protonmail.android.mailsession.domain.model.LoginError
import me.proton.core.network.domain.session.Session

class LegacyAccountRepositoryImpl @Inject constructor(
    private val accountDataSource: LegacyAccountDataSource,
    private val userDataSource: LegacyUserDataSource,
    private val userAddressDataSource: LegacyUserAddressDataSource,
    private val migrationInfoMapper: MigrationInfoMapper,
    private val mailSessionRepository: MailSessionRepository
) : LegacyAccountRepository {

    override suspend fun getAuthenticatedLegacySessions(): List<Session.Authenticated> = accountDataSource.getSessions()
        .firstOrNull()
        ?.filterIsInstance<Session.Authenticated>()
        .orEmpty()

    override suspend fun getLegacyAccountMigrationInfoFor(
        session: Session.Authenticated
    ): Either<MigrationError, AccountMigrationInfo> {
        val user = userDataSource.getUser(session.userId)
            ?: return MigrationError.LegacyDbFailure.MissingUser.left()

        val userAddress = userAddressDataSource.getPrimaryUserAddress(session.userId)
            ?: return MigrationError.LegacyDbFailure.MissingUserAddress.left()

        val account = accountDataSource.getAccount(session.userId).firstOrNull()
            ?: return MigrationError.LegacyDbFailure.MissingAccount.left()

        val primaryUserId = accountDataSource.getPrimaryUserId().firstOrNull()
        val isPrimaryUser = primaryUserId == session.userId

        return migrationInfoMapper.mapToAccountMigrationInfo(
            session = session,
            account = account,
            user = user,
            userAddress = userAddress,
            isPrimaryUser = isPrimaryUser
        ).right()
    }

    override suspend fun hasLegacyLoggedInAccounts(): Boolean = getAuthenticatedLegacySessions().isNotEmpty()

    override suspend fun migrateLegacyAccount(
        accountMigrationInfo: AccountMigrationInfo
    ): Either<MigrationError, Unit> {
        val loginFlowWrapper = mailSessionRepository.getMailSession()
            .newLoginFlow()
            .getOrElse { return MigrationError.LoginFlowFailed.left() }

        return loginFlowWrapper.migrate(accountMigrationInfo.toMigrationData())
            .mapLeft { it.toMigrationError() }
    }
}

fun LoginError.toMigrationError(): MigrationError.MigrateFailed = when (this) {
    is LoginError.InvalidCredentials -> MigrationError.MigrateFailed.InvalidCredentials
    is LoginError.UnsupportedTwoFactorAuthentication -> MigrationError.MigrateFailed.UnsupportedTwoFactorAuth
    is LoginError.CannotUnlockUserKey -> MigrationError.MigrateFailed.CantUnlockUserKey
    is LoginError.Other -> MigrationError.MigrateFailed.Other(error)
}
