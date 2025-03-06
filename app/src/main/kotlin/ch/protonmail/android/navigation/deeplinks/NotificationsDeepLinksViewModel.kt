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

package ch.protonmail.android.navigation.deeplinks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.usecase.GetPrimaryAddress
import ch.protonmail.android.mailconversation.domain.repository.ConversationRepository
import ch.protonmail.android.maillabel.domain.model.ExclusiveLocation
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.FindLocalSystemLabelId
import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.navigation.deeplinks.NotificationsDeepLinksViewModel.State.NavigateToInbox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class NotificationsDeepLinksViewModel @Inject constructor(
    private val networkManager: NetworkManager,
    private val accountManager: AccountManager,
    private val getPrimaryAddress: GetPrimaryAddress,
    private val messageRepository: MessageRepository,
    private val conversationRepository: ConversationRepository,
    private val findLocalSystemLabelId: FindLocalSystemLabelId
) : ViewModel() {

    private val _state = MutableStateFlow<State>(State.Launched)
    val state: StateFlow<State> = _state

    private var navigateJob: Job? = null

    fun navigateToMessage(messageId: String, userId: String) {
        if (isOffline()) {
            navigateToInbox(userId)
        } else {
            navigateToMessageOrConversation(messageId, UserId(userId))
        }
    }

    fun navigateToInbox(userId: String) {
        viewModelScope.launch {
            val activeUserId = accountManager.getPrimaryUserId().firstOrNull()
            if (activeUserId != null && activeUserId.id != userId) {
                switchUserAndNavigateToInbox(userId)
            } else {
                _state.value = NavigateToInbox.ActiveUser
            }
        }
    }

    private suspend fun switchUserAndNavigateToInbox(userId: String) {
        val switchAccountResult = switchActiveUserIfRequiredTo(userId)
        _state.value = when (switchAccountResult) {
            AccountSwitchResult.AccountSwitchError -> NavigateToInbox.ActiveUser
            is AccountSwitchResult.AccountSwitched -> NavigateToInbox.ActiveUserSwitched(switchAccountResult.newEmail)
            AccountSwitchResult.NotRequired -> NavigateToInbox.ActiveUser
        }
    }

    private fun navigateToMessageOrConversation(messageId: String, userId: UserId) {
        Timber.d("navigateToMessage: $messageId, $userId")
        navigateJob?.cancel()
        navigateJob = viewModelScope.launch {
            when (val switchAccountResult = switchActiveUserIfRequiredTo(userId.id)) {
                AccountSwitchResult.AccountSwitchError -> navigateToInbox(userId.id)
                is AccountSwitchResult.AccountSwitched -> navigateToMessageOrConversation(
                    this.coroutineContext,
                    messageId,
                    switchAccountResult.newUserId,
                    switchAccountResult.newEmail
                )

                AccountSwitchResult.NotRequired -> navigateToMessageOrConversation(
                    this.coroutineContext,
                    messageId,
                    userId
                )
            }
        }
    }

    private suspend fun navigateToMessageOrConversation(
        coroutineContext: CoroutineContext,
        messageId: String,
        userId: UserId,
        switchedAccountEmail: String? = null
    ) {
        messageRepository.observeMessage(userId, MessageId(messageId))
            .distinctUntilChanged()
            .collectLatest { messageResult ->
                messageResult
                    .onLeft {
                        if (it != DataError.Local.NoDataCached) navigateToInbox(userId.id)
                    }
                    .onRight { message ->
                        navigateToConversation(message, userId, switchedAccountEmail)
                        coroutineContext.cancel()
                    }
            }
    }

    private suspend fun switchActiveUserIfRequiredTo(userId: String): AccountSwitchResult {
        return if (accountManager.getPrimaryUserId().firstOrNull()?.id == userId) {
            AccountSwitchResult.NotRequired
        } else {
            val targetAccount = accountManager.getAccounts(AccountState.Ready)
                .firstOrNull()
                ?.find { it.userId.id == userId }
                ?: return AccountSwitchResult.AccountSwitchError

            accountManager.setAsPrimary(UserId(userId))
            val emailAddress = getPrimaryAddress(UserId(userId)).getOrNull()?.email
            AccountSwitchResult.AccountSwitched(targetAccount.userId, emailAddress ?: "")
        }
    }

    private suspend fun navigateToConversation(
        message: Message,
        userId: UserId,
        switchedAccountEmail: String?
    ) {
        val labelId = message.exclusiveLocation.getLabelId(userId) ?: return

        conversationRepository.observeConversation(
            userId,
            message.conversationId,
            labelId
        ).collectLatest { conversationResult ->
            conversationResult
                .onLeft {
                    Timber.d("Conversation not found: $it")
                    if (it != DataError.Local.NoDataCached) navigateToInbox(userId.id)
                }
                .onRight { conversation ->
                    _state.value =
                        State.NavigateToConversation(
                            conversationId = conversation.conversationId,
                            userSwitchedEmail = switchedAccountEmail,
                            contextLabelId = labelId
                        )
                }
        }
    }

    private suspend fun ExclusiveLocation.getLabelId(userId: UserId) = when (this) {
        is ExclusiveLocation.Folder -> this.labelId
        is ExclusiveLocation.NoLocation -> findLocalSystemLabelId(userId, SystemLabelId.AllMail)?.labelId
        is ExclusiveLocation.System -> this.labelId
    }

    private fun isOffline() = networkManager.networkStatus == NetworkStatus.Disconnected

    sealed interface State {
        data object Launched : State

        sealed interface NavigateToInbox : State {
            data object ActiveUser : NavigateToInbox
            data class ActiveUserSwitched(val email: String) : NavigateToInbox
        }

        data class NavigateToConversation(
            val conversationId: ConversationId,
            val scrollToMessageId: MessageId? = null,
            val userSwitchedEmail: String? = null,
            val contextLabelId: LabelId
        ) : State
    }

    private sealed interface AccountSwitchResult {
        data object NotRequired : AccountSwitchResult
        data class AccountSwitched(val newUserId: UserId, val newEmail: String) : AccountSwitchResult

        data object AccountSwitchError : AccountSwitchResult
    }
}
