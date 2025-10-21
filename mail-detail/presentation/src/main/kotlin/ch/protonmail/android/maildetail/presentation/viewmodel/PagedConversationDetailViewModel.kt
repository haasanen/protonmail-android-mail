/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.maildetail.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.repository.ConversationCursor
import ch.protonmail.android.mailconversation.domain.entity.ConversationDetailEntryPoint
import ch.protonmail.android.maildetail.presentation.mapper.toPage
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailAction
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailEvent
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailEvent.AutoAdvanceRequested
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailEvent.ClearFocusPage
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailEvent.UpdatePage
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailState
import ch.protonmail.android.maildetail.presentation.model.PagedConversationEffects
import ch.protonmail.android.maildetail.presentation.reducer.PagedConversationDetailReducer
import ch.protonmail.android.maildetail.presentation.ui.ConversationDetailScreen
import ch.protonmail.android.maildetail.presentation.ui.PagedConversationDetailScreen
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.repository.AutoAdvanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
@SuppressWarnings("UnusedPrivateMember")
class PagedConversationDetailViewModel @Inject constructor(
    private var autoAdvanceRepository: AutoAdvanceRepository,
    // private var getConversationCursor: GetConversationCursor,
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val savedStateHandle: SavedStateHandle,
    private val reducer: PagedConversationDetailReducer
) : ViewModel() {

    private val _effects = MutableStateFlow(PagedConversationEffects())
    val effects = _effects.asStateFlow()
    private val mutableState = MutableStateFlow<PagedConversationDetailState>(PagedConversationDetailState.Loading)
    val state: StateFlow<PagedConversationDetailState> = mutableState.asStateFlow()
    private var conversationCursor: ConversationCursor? = null


    init {
        Timber.d("conversation-cursor PAged ConversationDetailViewModel")
        /*      viewModelScope.launch {
                  observePrimaryUserId().first()?.let { userId ->
                      Timber.d("conversation-cursor PAged ConversationDetailViewModel $userId")
                      val autoAdvance = getAutoAdvance(userId)
                      getConversationCursor(
                          singleMessageMode = requireSingleMessageMode(),
                          conversationId = requireConversationId(),
                          userId = userId,
                          messageId = getInitialScrollToMessageId()?.id,
                          viewModeIsConversationMode = requireViewModeModeIsConversation()
                      ).collect { state ->
                          onCursor(autoAdvance, state)
                      }
                  }
              }*/
    }

    /* private fun onCursor(autoAdvance: Boolean, cursorState: EphemeralMailboxCursor?) {
         when (cursorState) {
             null,
             is EphemeralMailboxCursor.CursorDead,
             is EphemeralMailboxCursor.NotInitalised -> {
                 emitNewStateFor(
                     PagedConversationDetailEvent.Error(ConversationCursorError.InvalidState)
                 )
                 _effects.value =
                     PagedConversationEffects(Effect.of(UiError.OTHER))
             }

             is EphemeralMailboxCursor.Data -> {
                 val cursor = cursorState.cursor
                 conversationCursor = cursor
                 emitNewStateFor(
                     Ready(
                         autoAdvance = autoAdvance,
                         cursor.current.toPage(),
                         cursor.next.toPage(),
                         cursor.previous.toPage(),
                         navigationArgs = NavigationArgs(
                             openedFromLocation = requireLabelId(),
                             singleMessageMode = requireSingleMessageMode(),
                             conversationEntryPoint = getEntryPoint()
                         )
                     )
                 )
             }
         }
     }*/

    private fun emitNewStateFor(event: PagedConversationDetailEvent) {
        val currentState = mutableState.value
        mutableState.update { reducer.newStateFrom(currentState, event) }
    }

    fun submit(action: PagedConversationDetailAction) {
        viewModelScope.launch {
            when (action) {
                is PagedConversationDetailAction.SetSettledPage -> onSettledPage(action.value)
                is PagedConversationDetailAction.ClearFocusPage -> emitNewStateFor(ClearFocusPage)
                is PagedConversationDetailAction.AutoAdvance ->
                    handleAutoAdvance()

            }
        }
    }

    private fun handleAutoAdvance() {
        // block scrolling whilst we fetch pages, usually not async
        emitNewStateFor(AutoAdvanceRequested)
    }


    private suspend fun onSettledPage(index: Int) {
        guardCursor { cursor ->
            (state.value as? PagedConversationDetailState.Ready)?.let { state ->
                state.dynamicViewPagerState.currentPageIndex?.let { currentIndex ->
                    if (index < currentIndex) {
                        cursor.moveBackward()
                        emitNewStateFor(
                            UpdatePage(
                                cursor.current.toPage(),
                                cursor.next.toPage(),
                                cursor.previous.toPage()
                            )
                        )
                    }
                    if (index > currentIndex) {
                        cursor.moveForward()
                        emitNewStateFor(
                            UpdatePage(
                                cursor.current.toPage(),
                                cursor.next.toPage(),
                                cursor.previous.toPage()
                            )
                        )
                    }

                    state.dynamicViewPagerState.pendingRemoval?.let {
                        cursor.invalidatePrevious()
                    }
                }
            }
        }
    }

    private suspend fun getAutoAdvance(userId: UserId) = autoAdvanceRepository.getAutoAdvance(userId)
        .fold(
            ifLeft = {
                Timber.e("Error getting Auto Advance Settings for user: $userId")
                false
            },
            ifRight = { autoAdvanceEnabled ->
                autoAdvanceEnabled
            }
        )

    override fun onCleared() {
        conversationCursor?.close()
        super.onCleared()
    }

    private fun requireConversationId(): ConversationId {
        val conversationId = savedStateHandle.get<String>(ConversationDetailScreen.ConversationIdKey)
            ?: throw IllegalStateException("No Conversation id given")
        return ConversationId(conversationId)
    }

    private fun requireLabelId(): LabelId {
        val labelId = savedStateHandle.get<String>(ConversationDetailScreen.OpenedFromLocationKey)
            ?: throw IllegalStateException("No Conversation id given")
        return LabelId(labelId)
    }

    private fun requireSingleMessageMode(): Boolean {
        val singleMessageMode = savedStateHandle.get<String>(ConversationDetailScreen.IsSingleMessageMode)
            ?: throw IllegalStateException("No IsSingleMessageMode given")
        return singleMessageMode.toBoolean()
    }

    private fun requireViewModeModeIsConversation(): Boolean {
        val isConversation = savedStateHandle.get<String>(PagedConversationDetailScreen.ViewModeIsConversation)
            ?: throw IllegalStateException("No viewMode given")
        return isConversation.toBoolean()
    }

    private fun getInitialScrollToMessageId(): MessageIdUiModel? {
        val messageIdStr = savedStateHandle.get<String>(ConversationDetailScreen.ScrollToMessageIdKey)
        return messageIdStr?.let { if (it == "null") null else MessageIdUiModel(it) }
    }

    private fun getEntryPoint(): ConversationDetailEntryPoint {
        val value = savedStateHandle.get<String>(ConversationDetailScreen.ConversationDetailEntryPointNameKey)
            ?: throw IllegalStateException("No Entry point given")
        return ConversationDetailEntryPoint.valueOf(value)
    }

    private suspend fun guardCursor(block: suspend (cursor: ConversationCursor) -> Unit) {
        if (conversationCursor != null) {
            block(conversationCursor!!)
        }
    }
}
