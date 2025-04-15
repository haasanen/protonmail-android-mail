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

package ch.protonmail.android.mailsession.data.wrapper

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.datarust.mapper.LocalUserId
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.MailSessionGetAccountResult
import uniffi.proton_mail_uniffi.MailSessionGetAccountSessionsResult
import uniffi.proton_mail_uniffi.MailSessionGetAccountsResult
import uniffi.proton_mail_uniffi.MailSessionGetPrimaryAccountResult
import uniffi.proton_mail_uniffi.MailSessionGetSessionsResult
import uniffi.proton_mail_uniffi.MailSessionUserContextFromSessionResult
import uniffi.proton_mail_uniffi.MailSessionWatchAccountsResult
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredSession
import uniffi.proton_mail_uniffi.WatchedAccounts

class MailSessionWrapper(private val mailSession: MailSession) {

    fun getRustMailSession() = mailSession

    suspend fun watchAccounts(liveQueryCallback: LiveQueryCallback): Either<DataError, WatchedAccounts> =
        when (val result = mailSession.watchAccounts(liveQueryCallback)) {
            is MailSessionWatchAccountsResult.Error -> result.v1.toDataError().left()
            is MailSessionWatchAccountsResult.Ok -> result.v1.right()
        }

    suspend fun getAccounts(): Either<DataError, List<StoredAccount>> = when (val result = mailSession.getAccounts()) {
        is MailSessionGetAccountsResult.Error -> result.v1.toDataError().left()
        is MailSessionGetAccountsResult.Ok -> result.v1.right()
    }

    suspend fun getAccount(userId: LocalUserId): Either<DataError, StoredAccount> =
        when (val result = mailSession.getAccount(userId)) {
            is MailSessionGetAccountResult.Error -> result.v1.toDataError().left()
            is MailSessionGetAccountResult.Ok -> {
                when (val data = result.v1) {
                    null -> DataError.Local.NoDataCached.left()
                    else -> data.right()
                }
            }
        }

    suspend fun getPrimaryAccount(): Either<DataError, StoredAccount> =
        when (val result = mailSession.getPrimaryAccount()) {
            is MailSessionGetPrimaryAccountResult.Error -> result.v1.toDataError().left()
            is MailSessionGetPrimaryAccountResult.Ok -> {
                when (val data = result.v1) {
                    null -> DataError.Local.NoDataCached.left()
                    else -> data.right()
                }
            }
        }

    suspend fun getAccountSessions(account: StoredAccount): Either<DataError, List<StoredSession>> =
        when (val result = mailSession.getAccountSessions(account)) {
            is MailSessionGetAccountSessionsResult.Error -> result.v1.toDataError().left()
            is MailSessionGetAccountSessionsResult.Ok -> result.v1.right()
        }

    suspend fun getSessions(): Either<DataError, List<StoredSession>> = when (val result = mailSession.getSessions()) {
        is MailSessionGetSessionsResult.Error -> result.v1.toDataError().left()
        is MailSessionGetSessionsResult.Ok -> result.v1.right()
    }

    suspend fun userContextFromSession(session: StoredSession): Either<DataError, MailUserSessionWrapper> =
        when (val result = mailSession.userContextFromSession(session)) {
            is MailSessionUserContextFromSessionResult.Error -> result.v1.toDataError().left()
            is MailSessionUserContextFromSessionResult.Ok -> MailUserSessionWrapper(result.v1).right()
        }

    suspend fun deleteAccount(userId: LocalUserId) = mailSession.deleteAccount(userId)

    suspend fun logoutAccount(userId: LocalUserId) = mailSession.logoutAccount(userId)

    suspend fun setPrimaryAccount(userId: LocalUserId) = mailSession.setPrimaryAccount(userId)

    fun registerDeviceTask() = mailSession.registerDeviceTask()
}
