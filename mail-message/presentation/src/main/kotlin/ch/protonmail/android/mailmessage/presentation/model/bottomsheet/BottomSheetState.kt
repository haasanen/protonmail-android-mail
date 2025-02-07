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

package ch.protonmail.android.mailmessage.presentation.model.bottomsheet

import ch.protonmail.android.mailcommon.domain.model.AvailableActions
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.presentation.MailLabelUiModel
import ch.protonmail.android.maillabel.presentation.model.LabelUiModelWithSelectedState
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Participant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.maillabel.domain.model.LabelId

data class BottomSheetState(
    val contentState: BottomSheetContentState?,
    val bottomSheetVisibilityEffect: Effect<BottomSheetVisibilityEffect> = Effect.empty()
) {

    fun isShowEffectWithoutContent() =
        bottomSheetVisibilityEffect == Effect.of(BottomSheetVisibilityEffect.Show) && contentState == null
}

sealed interface BottomSheetVisibilityEffect {
    object Show : BottomSheetVisibilityEffect
    object Hide : BottomSheetVisibilityEffect
}

sealed interface BottomSheetContentState
sealed interface BottomSheetOperation {
    object Requested : BottomSheetOperation
    object Dismiss : BottomSheetOperation
}


sealed interface MoveToBottomSheetState : BottomSheetContentState {

    data class Data(
        val moveToDestinations: ImmutableList<MailLabelUiModel>,
        val selected: MailLabelUiModel?,
        val messageIdInConversation: MessageId?
    ) : MoveToBottomSheetState

    object Loading : MoveToBottomSheetState

    sealed interface MoveToBottomSheetOperation : BottomSheetOperation

    sealed interface MoveToBottomSheetEvent : MoveToBottomSheetOperation {
        data class ActionData(
            val moveToDestinations: ImmutableList<MailLabelUiModel>,
            val messageIdInConversation: MessageId? = null
        ) : MoveToBottomSheetEvent
    }

    sealed interface MoveToBottomSheetAction : MoveToBottomSheetOperation {
        data class MoveToDestinationSelected(val mailLabelId: MailLabelId) : MoveToBottomSheetAction
    }
}

sealed interface LabelAsBottomSheetState : BottomSheetContentState {

    data class Data(
        val labelUiModelsWithSelectedState: ImmutableList<LabelUiModelWithSelectedState>,
        val messageIdInConversation: MessageId?
    ) : LabelAsBottomSheetState

    object Loading : LabelAsBottomSheetState

    sealed interface LabelAsBottomSheetOperation : BottomSheetOperation

    sealed interface LabelAsBottomSheetEvent : LabelAsBottomSheetOperation {
        data class ActionData(
            val customLabelList: ImmutableList<MailLabelUiModel.Custom>,
            val selectedLabels: ImmutableList<LabelId>,
            val partiallySelectedLabels: ImmutableList<LabelId> = emptyList<LabelId>().toImmutableList(),
            val messageIdInConversation: MessageId? = null
        ) : LabelAsBottomSheetEvent
    }

    sealed interface LabelAsBottomSheetAction : LabelAsBottomSheetOperation {
        data class LabelToggled(val labelId: LabelId) : LabelAsBottomSheetAction
    }
}

sealed interface MailboxMoreActionsBottomSheetState : BottomSheetContentState {

    data class Data(
        val hiddenActionUiModels: ImmutableList<ActionUiModel>,
        val visibleActionUiModels: ImmutableList<ActionUiModel>,
        val customizeToolbarActionUiModel: ActionUiModel,
        val selectedCount: Int
    ) : MailboxMoreActionsBottomSheetState

    data object Loading : MailboxMoreActionsBottomSheetState

    sealed interface MailboxMoreActionsBottomSheetOperation : BottomSheetOperation
    sealed interface MailboxMoreActionsBottomSheetEvent : MailboxMoreActionsBottomSheetOperation {
        data class ActionData(
            val hiddenActionUiModels: ImmutableList<ActionUiModel>,
            val visibleActionUiModels: ImmutableList<ActionUiModel>,
            val customizeToolbarActionUiModel: ActionUiModel,
            val selectedCount: Int
        ) : MailboxMoreActionsBottomSheetEvent
    }
}

sealed interface DetailMoreActionsBottomSheetState : BottomSheetContentState {
    data class Data(
        val detailDataUiModel: DetailDataUiModel,
        val replyActions: ImmutableList<ActionUiModel>,
        val messageActions: ImmutableList<ActionUiModel>,
        val moveActions: ImmutableList<ActionUiModel>,
        val genericActions: ImmutableList<ActionUiModel>
    ) : DetailMoreActionsBottomSheetState

    object Loading : DetailMoreActionsBottomSheetState

    sealed interface DetailMoreActionsBottomSheetOperation : BottomSheetOperation
    sealed interface DetailMoreActionsBottomSheetEvent : DetailMoreActionsBottomSheetOperation {
        data class DataLoaded(
            val messageSender: String,
            val messageSubject: String,
            val messageIdInConversation: String?,
            val availableActions: AvailableActions
        ) : DetailMoreActionsBottomSheetEvent
    }

    data class DetailDataUiModel(
        val headerSubjectText: TextUiModel,
        val messageIdInConversation: String?
    )
}

sealed interface UpsellingBottomSheetState : BottomSheetContentState {
    data object Requested : UpsellingBottomSheetState
    sealed interface UpsellingBottomSheetOperation : BottomSheetOperation
    sealed interface UpsellingBottomSheetEvent : UpsellingBottomSheetOperation {
        data object Ready : UpsellingBottomSheetEvent
    }
}

sealed interface ContactActionsBottomSheetState : BottomSheetContentState {

    data class Data(
        val participant: Participant,
        val avatarUiModel: AvatarUiModel,
        val contactId: ContactId?
    ) : ContactActionsBottomSheetState

    object Loading : ContactActionsBottomSheetState

    sealed interface ContactActionsBottomSheetOperation : BottomSheetOperation

    sealed interface ContactActionsBottomSheetEvent : ContactActionsBottomSheetOperation {
        data class ActionData(
            val participant: Participant,
            val avatarUiModel: AvatarUiModel,
            val contactId: ContactId?
        ) : ContactActionsBottomSheetEvent
    }
}
