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

package ch.protonmail.android.mailcomposer.presentation.model

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentGroupUiModel
import kotlin.time.Duration
import kotlin.time.Instant

data class ComposerDraftState(
    val fields: ComposerFields,
    val attachments: AttachmentGroupUiModel,
    val premiumFeatureMessage: Effect<TextUiModel>,
    val error: Effect<TextUiModel>,
    val isSubmittable: Boolean,
    val senderAddresses: List<SenderUiModel>,
    val scheduleSendOptions: ScheduleSendOptionsUiModel,
    val changeBottomSheetVisibility: Effect<Boolean>,
    val closeComposer: Effect<Unit>,
    val closeComposerWithDraftSaved: Effect<MessageId>,
    val closeComposerWithMessageSending: Effect<Unit>,
    val closeComposerWithMessageSendingOffline: Effect<Unit>,
    val confirmSendingWithoutSubject: Effect<Unit>,
    val changeFocusToField: Effect<FocusedFieldType>,
    val isLoading: Boolean,
    val showSendingLoading: Boolean,
    val attachmentsFileSizeExceeded: Effect<Unit>,
    val attachmentsEncryptionFailed: Effect<Unit>,
    val replaceDraftBody: Effect<TextUiModel>,
    val warning: Effect<TextUiModel>,
    val isMessagePasswordSet: Boolean,
    val focusTextBody: Effect<Unit> = Effect.empty(),
    val sendingErrorEffect: Effect<TextUiModel> = Effect.empty(),
    val senderChangedNotice: Effect<TextUiModel> = Effect.empty(),
    val messageExpiresIn: Duration,
    val confirmSendExpiringMessage: Effect<List<Participant>>,
    val openFilesPicker: Effect<Unit>,
    val openCamera: Effect<Unit>,
    val openPhotosPicker: Effect<Unit>,
    val confirmDiscardDraft: Effect<Unit>,
    val injectInlineAttachment: Effect<String>,
    val stripInlineAttachment: Effect<String>
) {

    companion object {

        fun initial(isSubmittable: Boolean = false): ComposerDraftState = ComposerDraftState(
            fields = ComposerFields(
                sender = SenderUiModel(""),
                displayBody = DraftDisplayBodyUiModel(""),
                body = ""
            ),
            attachments = AttachmentGroupUiModel(
                attachments = emptyList()
            ),
            premiumFeatureMessage = Effect.empty(),
            error = Effect.empty(),
            isSubmittable = isSubmittable,
            senderAddresses = emptyList(),
            scheduleSendOptions = ScheduleSendOptionsUiModel(
                tomorrow = InstantWithFormattedTime(Instant.DISTANT_PAST, ""),
                monday = InstantWithFormattedTime(Instant.DISTANT_PAST, ""),
                isCustomTimeOptionAvailable = false
            ),
            changeBottomSheetVisibility = Effect.empty(),
            closeComposer = Effect.empty(),
            closeComposerWithDraftSaved = Effect.empty(),
            closeComposerWithMessageSending = Effect.empty(),
            closeComposerWithMessageSendingOffline = Effect.empty(),
            confirmSendingWithoutSubject = Effect.empty(),
            changeFocusToField = Effect.empty(),
            isLoading = false,
            showSendingLoading = false,
            attachmentsFileSizeExceeded = Effect.empty(),
            attachmentsEncryptionFailed = Effect.empty(),
            replaceDraftBody = Effect.empty(),
            warning = Effect.empty(),
            isMessagePasswordSet = false,
            sendingErrorEffect = Effect.empty(),
            senderChangedNotice = Effect.empty(),
            messageExpiresIn = Duration.ZERO,
            confirmSendExpiringMessage = Effect.empty(),
            openFilesPicker = Effect.empty(),
            openCamera = Effect.empty(),
            openPhotosPicker = Effect.empty(),
            confirmDiscardDraft = Effect.empty(),
            injectInlineAttachment = Effect.empty(),
            stripInlineAttachment = Effect.empty()
        )
    }
}
