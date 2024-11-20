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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalUserId
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.MailSession
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredSession
import uniffi.proton_mail_uniffi.WatchedAccounts

class MailSessionWrapper(private val mailSession: MailSession) {

    fun rustObject() = mailSession

    suspend fun watchAccounts(liveQueryCallback: LiveQueryCallback): WatchedAccounts =
        mailSession.watchAccounts(liveQueryCallback)

    suspend fun getAccounts(): List<StoredAccount> = mailSession.getAccounts()

    suspend fun getAccount(userId: LocalUserId) = mailSession.getAccount(userId)

    suspend fun getPrimaryAccount() = mailSession.getPrimaryAccount()

    suspend fun getAccountSessions(account: StoredAccount) = mailSession.getAccountSessions(account)

    suspend fun userContextFromSession(session: StoredSession) = MailUserSessionWrapper(
        mailSession.userContextFromSession(session)
    )

    suspend fun deleteAccount(userId: LocalUserId) = mailSession.deleteAccount(userId)

    suspend fun logoutAccount(userId: LocalUserId) = mailSession.logoutAccount(userId)

    suspend fun setPrimaryAccount(userId: LocalUserId) = mailSession.setPrimaryAccount(userId)

}
