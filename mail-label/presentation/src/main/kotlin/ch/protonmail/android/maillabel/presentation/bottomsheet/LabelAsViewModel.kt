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

package ch.protonmail.android.maillabel.presentation.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.domain.usecase.LabelConversations
import ch.protonmail.android.maillabel.domain.model.LabelAsActions
import ch.protonmail.android.maillabel.presentation.model.LabelSelectedState
import ch.protonmail.android.mailmessage.domain.model.LabelSelectionList
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.LabelMessages
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.ViewMode
import timber.log.Timber

@HiltViewModel(assistedFactory = LabelAsViewModel.Factory::class)
internal class LabelAsViewModel @AssistedInject constructor(
    @Assisted val initialData: LabelAsBottomSheet.InitialData,
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val getLabelAsBottomSheetContent: GetLabelAsBottomSheetContent,
    private val labelMessages: LabelMessages,
    private val labelConversations: LabelConversations,
    private val reducer: LabelAsReducer
) : ViewModel() {

    private val mutableState = MutableStateFlow<LabelAsState>(LabelAsState.Loading)
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            val entryPoint = initialData.entryPoint
            val content = getInitialStateForEntryPoint(entryPoint).getOrElse {
                Timber.d("Unable to fetch LabelAs bottom sheet content.")
                return@launch emitNewStateFrom(LabelAsOperation.LabelAsEvent.LoadingError)
            }

            emitNewStateFrom(operation = LabelAsOperation.LabelAsEvent.InitialData(content, entryPoint))
        }
    }

    fun submit(action: LabelAsOperation.LabelAsAction) {
        viewModelScope.launch {
            when (action) {
                is LabelAsOperation.LabelAsAction.LabelToggled -> emitNewStateFrom(action)
                is LabelAsOperation.LabelAsAction.OperationConfirmed -> processLabelAsAction(action.alsoArchive)
            }
        }
    }

    private suspend fun getInitialStateForEntryPoint(
        entryPoint: LabelAsBottomSheetEntryPoint
    ): Either<GetInitialStateError, LabelAsActions> {
        val userId = initialData.userId
        val labelId = initialData.currentLocationLabelId
        val items = initialData.items.takeIf { it.isNotEmpty() } ?: return GetInitialStateError.NoItemsProvided.left()

        val result = when (entryPoint) {
            is LabelAsBottomSheetEntryPoint.Conversation -> getLabelAsBottomSheetContent.forConversation(
                userId = userId,
                labelId = labelId,
                conversationId = ConversationId(items.first().value)
            )

            is LabelAsBottomSheetEntryPoint.Message -> getLabelAsBottomSheetContent.forMessage(
                userId = userId,
                labelId = labelId,
                messageId = MessageId(items.first().value)
            )

            is LabelAsBottomSheetEntryPoint.ViewModeAware -> {
                getLabelAsBottomSheetContent.forMailbox(userId, labelId, items, entryPoint.viewMode)
            }
        }

        return result.mapLeft { error ->
            GetInitialStateError.LoadingContentError(error)
        }
    }

    private suspend fun processLabelAsAction(archiveSelected: Boolean) {
        val userId = observePrimaryUserId().filterNotNull().first()
        val labelAsData = state.value as? LabelAsState.Data
            ?: throw IllegalStateException("state is not LabelAsState.Data")

        val updatedSelection = labelAsData.getLabelSelectionState()

        val handleLabelData = HandleLabelData(
            userId = userId,
            selectedItems = initialData.items.toSet(),
            updatedSelectionList = updatedSelection,
            archiveSelected = archiveSelected,
            entryPoint = initialData.entryPoint
        )

        emitNewStateFrom(handleLabelOperation(handleLabelData))
    }

    private fun LabelAsState.Data.getLabelSelectionState(): LabelSelectionList {
        val selectedLabels = this.labelUiModels
            .filter { it.selectedState == LabelSelectedState.Selected }
            .map { it.labelUiModel.id.labelId }

        val partiallySelectedLabels = this.labelUiModels
            .filter { it.selectedState == LabelSelectedState.PartiallySelected }
            .map { it.labelUiModel.id.labelId }
        return LabelSelectionList(
            selectedLabels = selectedLabels,
            partiallySelectionLabels = partiallySelectedLabels
        )
    }

    private suspend fun handleLabelOperation(data: HandleLabelData): LabelAsOperation {
        val selectedItems = data.selectedItems
        val updatedSelectionList = data.updatedSelectionList
        val alsoArchive = data.archiveSelected
        val entryPoint = initialData.entryPoint

        return when (entryPoint) {
            LabelAsBottomSheetEntryPoint.Conversation -> labelConversations(
                userId = data.userId,
                conversationIds = selectedItems.map { ConversationId(it.value) },
                updatedSelections = updatedSelectionList,
                shouldArchive = alsoArchive
            )

            is LabelAsBottomSheetEntryPoint.Message -> labelMessages(
                userId = data.userId,
                messageIds = selectedItems.map { MessageId(it.value) },
                updatedSelections = updatedSelectionList,
                shouldArchive = alsoArchive
            )

            is LabelAsBottomSheetEntryPoint.ViewModeAware -> when (entryPoint.viewMode) {
                ViewMode.ConversationGrouping -> labelConversations(
                    userId = data.userId,
                    conversationIds = selectedItems.map { ConversationId(it.value) },
                    updatedSelections = updatedSelectionList,
                    shouldArchive = alsoArchive
                )

                ViewMode.NoConversationGrouping -> labelMessages(
                    userId = data.userId,
                    messageIds = selectedItems.map { MessageId(it.value) },
                    updatedSelections = updatedSelectionList,
                    shouldArchive = alsoArchive
                )
            }
        }.fold(
            ifLeft = { LabelAsOperation.LabelAsEvent.ErrorLabeling },
            ifRight = { LabelAsOperation.LabelAsEvent.LabelingComplete }
        )
    }

    private fun emitNewStateFrom(operation: LabelAsOperation) {
        mutableState.update { reducer.newStateFrom(state.value, operation) }
    }

    private data class HandleLabelData(
        val userId: UserId,
        val selectedItems: Set<LabelAsItemId>,
        val updatedSelectionList: LabelSelectionList,
        val archiveSelected: Boolean,
        val entryPoint: LabelAsBottomSheetEntryPoint
    )

    sealed interface GetInitialStateError {
        data object NoItemsProvided : GetInitialStateError

        @JvmInline
        value class LoadingContentError(val error: DataError) : GetInitialStateError
    }

    @AssistedFactory
    interface Factory {

        fun create(payload: LabelAsBottomSheet.InitialData): LabelAsViewModel
    }
}
