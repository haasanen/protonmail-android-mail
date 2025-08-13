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

package ch.protonmail.android.mailnotifications.domain.handler

import java.util.concurrent.ConcurrentHashMap
import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailnotifications.domain.usecase.DismissEmailNotificationsForUser
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

internal class AccountStateAwareNotificationHandler @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val dismissEmailNotificationsForUser: DismissEmailNotificationsForUser,
    @AppScope private val coroutineScope: CoroutineScope
) : NotificationHandler {

    private val accountStatesMap = ConcurrentHashMap<UserId, AccountState>()

    override fun handle() {
        coroutineScope.launch {
            userSessionRepository
                .observeAccounts()
                .distinctUntilChanged()
                .collect { accounts ->
                    val existingUserIds = accounts.map { it.userId }.toSet()
                    val removedAccounts = accountStatesMap.keys.filter { !existingUserIds.contains(it) }

                    // Account removal is not collected via `observeAccounts`, since the entity is not emitted anymore.
                    removedAccounts.forEach { userId ->
                        dismissEmailNotificationsForUser(userId)
                        accountStatesMap.remove(userId)
                    }

                    accounts.forEach { account ->
                        accountStatesMap[account.userId] = account.state

                        when (account.state) {
                            AccountState.Disabled -> dismissEmailNotificationsForUser(account.userId)

                            AccountState.NotReady,
                            AccountState.Ready,
                            AccountState.TwoPasswordNeeded,
                            AccountState.TwoFactorNeeded,
                            AccountState.NewPassNeeded -> Unit
                        }
                    }
                }
        }
    }
}
