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

package ch.protonmail.android.mailsession.data.repository

import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.MailUserSession
import javax.inject.Inject

@Deprecated(
    """
    In place to allow compilation and basic features with old core libs.
    Newer methods might be stubbed or not at all implemented. 
    New usages shouldn't be added anywhere.
    """,
    ReplaceWith("UserSessionRepositoryImpl")
)
class CoreUserSessionRepositoryImpl @Inject constructor(
    private val accountManager: AccountManager
) : UserSessionRepository {

    override fun observeCurrentUserSession(): Flow<MailUserSession?> {
        Timber.w("mail-session: calling stubbed observeCurrentSession method! This won't work.")
        return flowOf(null)
    }

    override suspend fun getUserSession(userId: UserId): MailUserSession? {
        Timber.w("mail-session: calling stubbed observeCurrentSession method! This won't work.")
        return null
    }

    override fun observeCurrentUserId(): Flow<UserId?> = accountManager.getPrimaryUserId()

}
