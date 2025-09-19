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

package ch.protonmail.android.navigation

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import ch.protonmail.android.mailcommon.data.file.getShareInfo
import ch.protonmail.android.mailcommon.data.file.isExternal
import ch.protonmail.android.mailcommon.data.file.isStartedFromLauncher
import ch.protonmail.android.mailcommon.domain.model.encode
import ch.protonmail.android.mailcommon.domain.model.isNotEmpty
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.usecase.FormatFullDate
import ch.protonmail.android.mailcomposer.domain.model.MessageSendingStatus
import ch.protonmail.android.mailcomposer.domain.usecase.DiscardDraft
import ch.protonmail.android.mailcomposer.domain.usecase.MarkMessageSendingStatusesAsSeen
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveSendingMessagesStatus
import ch.protonmail.android.mailcomposer.domain.usecase.UndoSendMessage
import ch.protonmail.android.mailmailbox.domain.usecase.RecordMailboxScreenView
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.CancelScheduleSendMessage
import ch.protonmail.android.mailnotifications.domain.NotificationsDeepLinkHelper
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.navigation.model.Destination
import ch.protonmail.android.navigation.model.HomeState
import ch.protonmail.android.navigation.model.NavigationEffect
import ch.protonmail.android.navigation.share.NewIntentObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Instant

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeSendingMessagesStatus: ObserveSendingMessagesStatus,
    private val recordMailboxScreenView: RecordMailboxScreenView,
    private val discardDraft: DiscardDraft,
    private val undoSendMessage: UndoSendMessage,
    private val markMessageSendingStatusesAsSeen: MarkMessageSendingStatusesAsSeen,
    private val formatFullDate: FormatFullDate,
    private val cancelScheduleSendMessage: CancelScheduleSendMessage,
    observePrimaryUserId: ObservePrimaryUserId,
    newIntentObserver: NewIntentObserver
) : ViewModel() {

    private val primaryUserId = observePrimaryUserId().filterNotNull()

    private val mutableState = MutableStateFlow(HomeState.Initial)

    val state: StateFlow<HomeState> = mutableState

    init {
        primaryUserId.flatMapLatest { userId ->
            observeSendingMessagesStatus(userId)
        }.onEach {
            emitNewStateFor(it)
        }.launchIn(viewModelScope)

        newIntentObserver()
            .onEach { emitNewStateForIntent(it) }
            .launchIn(viewModelScope)
    }

    fun navigateTo(navController: NavController, navigationEffect: NavigationEffect) {
        when (navigationEffect) {
            is NavigationEffect.NavigateTo -> navController.navigate(
                route = navigationEffect.route,
                navigationEffect.navOptions
            )

            is NavigationEffect.PopBackStack -> navController.popBackStack()
            is NavigationEffect.PopBackStackTo -> navController.popBackStack(
                route = navigationEffect.route,
                inclusive = navigationEffect.inclusive
            )

            is NavigationEffect.NavigateToUri -> navController.navigate(
                navigationEffect.uri,
                navigationEffect.navOptions
            )
        }
    }

    /**
     * Navigate to Drafts only when:
     * - we are outside of Mailbox
     * - we are in Mailbox but not in Drafts
     */
    fun shouldNavigateToDraftsOnSendingFailure(currentNavDestination: NavDestination?): Boolean =
        currentNavDestination?.route != Destination.Screen.Mailbox.route

    fun navigateToDrafts(navController: NavController) {
        if (navController.currentDestination?.route != Destination.Screen.Mailbox.route) {
            navController.popBackStack(Destination.Screen.Mailbox.route, inclusive = false)
        }
        Timber.e("Not navigating to drafts, missing implementation")
        // Removed with dynamic system labels feature. Need to observe mail labels to know what's draft location ID
        // in order to navigate
//        selectedMailLabelId.set()
    }

    fun discardDraft(messageId: MessageId) {
        viewModelScope.launch {
            primaryUserId.firstOrNull()?.let { userId ->
                discardDraft(userId, messageId)
            } ?: Timber.e("Primary user is not available!")
        }
    }

    fun undoSendMessage(messageId: MessageId) {
        viewModelScope.launch {
            primaryUserId.firstOrNull()?.let {
                undoSendMessage(it, messageId)
                    .onRight { navigateToDraftInComposer(messageId) }
                    .onLeft { showUndoSendError(messageId) }
            } ?: Timber.e("Primary user is not available!")
        }
    }

    fun undoScheduleSendMessage(messageId: MessageId) {
        viewModelScope.launch {
            primaryUserId.firstOrNull()?.let { userId ->
                showCancellingScheduleSend(messageId)
                cancelScheduleSendMessage(userId, messageId)
                    .onRight { navigateToDraftInComposer(messageId) }
                    .onLeft { showUndoSendError(messageId) }
            } ?: Timber.e("Primary user is not available!")
        }
    }

    fun confirmMessageAsSeen(messageId: MessageId) {
        viewModelScope.launch {
            primaryUserId.firstOrNull()?.let {
                markMessageSendingStatusesAsSeen(it, listOf(messageId))
            } ?: Timber.e("Primary user is not available!")
        }
    }

    fun recordViewOfMailboxScreen() = recordMailboxScreenView()

    fun formatTime(time: Instant) = formatFullDate(time)

    private fun showCancellingScheduleSend(messageId: MessageId) {
        mutableState.update {
            it.copy(messageSendingStatusEffect = Effect.of(MessageSendingStatus.CancellingScheduleSend(messageId)))
        }
    }

    private fun showUndoSendError(messageId: MessageId) {
        mutableState.update {
            it.copy(messageSendingStatusEffect = Effect.of(MessageSendingStatus.UndoSendError(messageId)))
        }
    }

    private fun navigateToDraftInComposer(messageId: MessageId) {
        val popUpToMailbox = NavOptions.Builder()
            .setPopUpTo(route = Destination.Screen.Mailbox.route, inclusive = false, saveState = false)
            .build()
        val navigateToComposer = NavigationEffect.NavigateTo(
            route = Destination.Screen.EditDraftComposer(messageId),
            navOptions = popUpToMailbox
        )
        mutableState.update { it.copy(navigateToEffect = Effect.of(navigateToComposer)) }
    }

    private fun emitNewStateFor(messageSendingStatus: MessageSendingStatus) {
        if (messageSendingStatus is MessageSendingStatus.NoStatus) {
            // Emitting a None status to UI would override the previously emitted effect and cause snack not to show
            return
        }
        mutableState.update { it.copy(messageSendingStatusEffect = Effect.of(messageSendingStatus)) }
    }

    private fun emitNewStateForIntent(intent: Intent) {
        Timber.tag("intent-navigation").d("Processing intent: ${intent.action}, data: ${intent.data}")

        val currentState = state.value
        val isNotificationIntent = intent.data?.host == NotificationsDeepLinkHelper.NotificationHost

        when {
            // Notification intents should always be processed, regardless of how app was started
            isNotificationIntent -> {
                Timber.tag("intent-navigation").d("Processing notification intent")
                emitNavigationForIntent(intent)
            }

            // For share intents, check if app was started from launcher
            intent.isStartedFromLauncher() -> {
                mutableState.value = currentState.copy(startedFromLauncher = true)
                Timber.tag("intent-navigation").d("App started from launcher")
            }

            // Process share intent only if app wasn't previously started from launcher
            // Or process if it's triggered by the app itself (e.g. via mailto: links in message bodies)
            !currentState.startedFromLauncher || !intent.isExternal() -> {
                Timber.tag("intent-navigation").d("Processing share intent")
                emitNavigationForIntent(intent)
            }

            else -> {
                Timber.tag("intent-navigation")
                    .d("Share intent is not processed as this instance was started from launcher!")
            }
        }
    }

    private fun emitNavigationForIntent(intent: Intent) {
        Timber.tag("intent-navigation").d("emitNavigationForIntent called")

        val isNotificationIntent = intent.data?.host == NotificationsDeepLinkHelper.NotificationHost

        val event = when {
            isNotificationIntent -> {
                Timber.tag("intent-navigation").d("Creating notification navigation: ${intent.data}")
                NavigationEffect.NavigateToUri(intent.data!!)
            }

            else -> {
                val intentShareInfo = intent.getShareInfo()
                    .takeIf { it.isNotEmpty() }
                    ?: return Timber.tag("intent-navigation").e("Unable to determine uri from share intent.")

                val draftAction = DraftAction.PrefillForShare(intentShareInfo.encode())
                val isExternal = intentShareInfo.isExternal
                NavigationEffect.NavigateTo(Destination.Screen.ShareFileComposer(draftAction, isExternal))
            }
        }

        Timber.tag("intent-navigation").d("Updating state with navigation effect: $event")
        mutableState.update { it.copy(navigateToEffect = Effect.of(event)) }
    }
}
