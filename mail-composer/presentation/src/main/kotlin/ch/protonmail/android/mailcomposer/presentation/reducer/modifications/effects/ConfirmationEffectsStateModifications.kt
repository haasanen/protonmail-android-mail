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
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState
import ch.protonmail.android.mailcomposer.presentation.model.FocusedFieldType

internal sealed interface ConfirmationsEffectsStateModification : EffectsStateModification {
    data object SendNoSubjectConfirmationRequested : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(confirmSendingWithoutSubject = Effect.of(Unit))
    }

    data object SendExpirationSupportUnknownConfirmationRequested : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            confirmSendExpiringMessage = Effect.of(
                event = TextUiModel.TextRes(R.string.composer_send_expiring_message_to_external_may_fail)
            )
        )
    }

    data class SendExpirationUnsupportedForSomeConfirmationRequested(
        val recipients: List<String>
    ) : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects {
            val recipientsToShow = recipients
                .take(NUM_RECIPIENTS_TO_SHOW)
                .joinToString { it }
            val recipientsHiddenCount = (recipients.size - NUM_RECIPIENTS_TO_SHOW)
                .takeIf { it > 0 }

            val formattedHiddenRecipientsCount = recipientsHiddenCount?.let { " +$it" } ?: ""
            val formattedRecipients = recipientsToShow.plus(formattedHiddenRecipientsCount)

            return state.copy(
                confirmSendExpiringMessage = Effect.of(
                    event = TextUiModel.TextResWithArgs(
                        value = R.string.composer_send_expiring_message_to_external_will_fail,
                        formatArgs = listOf(formattedRecipients)
                    )
                )
            )
        }
    }

    data object SendExpirationUnsupportedConfirmationRequested : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            confirmSendExpiringMessage = Effect.of(
                event = TextUiModel.TextRes(R.string.composer_send_expiring_message_to_external_will_fail_for_all)
            )
        )
    }

    data object CancelSendNoSubject : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects = state.copy(
            changeFocusToField = Effect.of(FocusedFieldType.SUBJECT),
            confirmSendingWithoutSubject = Effect.empty()
        )
    }

    data object DiscardDraftConfirmationRequested : EffectsStateModification {

        override fun apply(state: ComposerState.Effects): ComposerState.Effects =
            state.copy(confirmDiscardDraft = Effect.of(Unit))
    }
}

private const val NUM_RECIPIENTS_TO_SHOW = 4
