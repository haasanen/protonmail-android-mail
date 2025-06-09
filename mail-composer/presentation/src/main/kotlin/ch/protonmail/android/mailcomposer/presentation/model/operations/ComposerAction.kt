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

package ch.protonmail.android.mailcomposer.presentation.model.operations

import android.net.Uri
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.presentation.model.SenderUiModel
import kotlin.time.Duration

internal sealed interface ComposerAction : ComposerStateOperation {
    data object ChangeSender : ComposerAction
    data class SetSenderAddress(val sender: SenderUiModel) : ComposerAction

    data object OpenExpirationSettings : ComposerAction
    data class SetMessageExpiration(val duration: Duration) : ComposerAction

    data object CloseComposer : ComposerAction
    data object SendMessage : ComposerAction

    data object ConfirmSendWithNoSubject : ComposerAction
    data object CancelSendWithNoSubject : ComposerAction

    data object ConfirmSendExpirationSetToExternal : ComposerAction
    data object CancelSendExpirationSetToExternal : ComposerAction

    data object ClearSendingError : ComposerAction

    data object InlineImageActionsRequested : ComposerAction
    data object AddAttachmentsRequested : ComposerAction
    data object OpenPhotosPicker : ComposerAction
    data object OpenCameraPicker : ComposerAction
    data object OpenFilePicker : ComposerAction
    data class AddAttachments(val uriList: List<Uri>) : ComposerAction
    data class AddFileAttachments(val uriList: List<Uri>) : ComposerAction
    data class RemoveAttachment(val attachmentId: AttachmentId) : ComposerAction
    data class RemoveInlineAttachment(val contentId: String) : ComposerAction

    data class DraftBodyChanged(val draftBody: DraftBody) : ComposerAction

    data object DiscardDraftRequested : ComposerAction
    data object DiscardDraftConfirmed : ComposerAction
}
