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

package ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.DraftSenderValidationError
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.model.DraftUiModel

internal sealed interface ContentEffectsStateModifications : EffectsStateModification {

    data object OnAddAttachmentCameraRequested : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            openCamera = Effect.of(Unit),
            changeBottomSheetVisibility = Effect.of(false)
        )
    }

    data object OnAddAttachmentPhotosRequested : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            openPhotosPicker = Effect.of(Unit),
            changeBottomSheetVisibility = Effect.of(false)
        )
    }

    data object OnAddAttachmentFileRequested : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            openFilesPicker = Effect.of(Unit),
            changeBottomSheetVisibility = Effect.of(false)
        )
    }

    data class OnInlineAttachmentsAdded(val contentIds: List<String>) : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(injectInlineAttachments = Effect.of(contentIds))
    }

    data class OnInlineAttachmentRemoved(val contentId: String) : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            stripInlineAttachment = Effect.of(contentId),
            changeBottomSheetVisibility = Effect.of(false)
        )
    }

    data class DraftSenderChanged(val refreshedBody: DraftDisplayBodyUiModel) : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            refreshBody = Effect.of(refreshedBody),
            changeBottomSheetVisibility = Effect.of(false)
        )
    }

    data class DraftBodyChanged(val refreshedBody: DraftDisplayBodyUiModel) : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            refreshBody = Effect.of(refreshedBody)
        )
    }

    data class DraftContentReady(
        val fields: DraftUiModel,
        val isDataRefresh: Boolean,
        val forceBodyFocus: Boolean
    ) : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            warning = createWarningIfNotRefreshed(isDataRefresh),
            focusTextBody = if (forceBodyFocus) Effect.of(Unit) else Effect.empty()
        )

        private fun createWarningIfNotRefreshed(isDataRefresh: Boolean): Effect<TextUiModel> =
            if (!isDataRefresh) Effect.of(TextUiModel(R.string.composer_warning_local_data_shown))
            else Effect.empty()
    }

    data class OnDraftSenderValidationError(val validationError: DraftSenderValidationError) :
        EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            senderChangedNotice = when (validationError) {
                is DraftSenderValidationError.AddressCanNotSend ->
                    Effect.of(TextUiModel(R.string.composer_sender_changed_original_address_cannot_send))

                is DraftSenderValidationError.AddressDisabled ->
                    Effect.of(TextUiModel(R.string.composer_sender_changed_original_address_disabled))

                is DraftSenderValidationError.SubscriptionRequired ->
                    Effect.of(TextUiModel(R.string.composer_sender_changed_pm_address_is_a_paid_feature))
            }
        )
    }
}
