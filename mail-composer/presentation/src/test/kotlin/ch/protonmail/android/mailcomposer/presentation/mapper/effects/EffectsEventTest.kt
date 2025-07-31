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

package ch.protonmail.android.mailcomposer.presentation.mapper.effects

import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailcomposer.presentation.model.operations.EffectsEvent
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.ComposerStateModifications
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.BottomSheetEffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.CompletionEffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.ConfirmationsEffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.ContentEffectsStateModifications
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.RecoverableError
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.UnrecoverableError
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class EffectsEventTest(
    @Suppress("unused") private val testName: String,
    private val effect: EffectsEvent,
    private val expectedModification: ComposerStateModifications
) {

    @Test
    fun `should map to the correct modification`() {
        val actualModification = effect.toStateModifications()
        assertEquals(expectedModification, actualModification)
    }

    companion object {

        private val attachmentError = AttachmentAddError.AttachmentTooLarge
        private val externalRecipients: List<Recipient> = mockk()

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "OnDraftLoadingFailed to modification",
                EffectsEvent.DraftEvent.OnDraftLoadingFailed,
                ComposerStateModifications(effectsModification = UnrecoverableError.DraftContentUnavailable)
            ),
            arrayOf(
                "OnParentLoadingFailed to modification",
                EffectsEvent.LoadingEvent.OnParentLoadingFailed,
                ComposerStateModifications(effectsModification = UnrecoverableError.ParentMessageMetadata)
            ),
            arrayOf(
                "OnSenderAddressLoadingFailed to modification",
                EffectsEvent.LoadingEvent.OnSenderAddressLoadingFailed,
                ComposerStateModifications(effectsModification = UnrecoverableError.InvalidSenderAddress)
            ),
            arrayOf(
                "AttachmentEventError to modification",
                EffectsEvent.AttachmentEvent.AddAttachmentError(attachmentError),
                ComposerStateModifications(effectsModification = RecoverableError.AttachmentsStore(attachmentError))
            ),
            arrayOf(
                "AttachmentEventOnAddFileRequest to modification",
                EffectsEvent.AttachmentEvent.OnAddFileRequest,
                ComposerStateModifications(
                    effectsModification = ContentEffectsStateModifications.OnAddAttachmentFileRequested
                )
            ),
            arrayOf(
                "AttachmentEventOnAddMediaRequest to modification",
                EffectsEvent.AttachmentEvent.OnAddMediaRequest,
                ComposerStateModifications(
                    effectsModification = ContentEffectsStateModifications.OnAddAttachmentPhotosRequested
                )
            ),
            arrayOf(
                "AttachmentEventOnAddCameraRequest to modification",
                EffectsEvent.AttachmentEvent.OnAddFromCameraRequest,
                ComposerStateModifications(
                    effectsModification = ContentEffectsStateModifications.OnAddAttachmentCameraRequested
                )
            ),
            arrayOf(
                "ComposerControlEvent OnCloseRequest (true) to modification",
                EffectsEvent.ComposerControlEvent.OnCloseRequestWithDraft(MessageId("123")),
                ComposerStateModifications(
                    effectsModification = CompletionEffectsStateModification.CloseComposer.CloseComposerDraftSaved(
                        MessageId("123")
                    )
                )
            ),
            arrayOf(
                "ComposerControlEvent OnCloseRequest (false) to modification",
                EffectsEvent.ComposerControlEvent.OnCloseRequest,
                ComposerStateModifications(
                    effectsModification = CompletionEffectsStateModification.CloseComposer.CloseComposerNoDraft
                )
            ),
            arrayOf(
                "ComposerControlEvent OnCloseRequestWithDraftDiscarded to modification",
                EffectsEvent.ComposerControlEvent.OnCloseRequestWithDraftDiscarded,
                ComposerStateModifications(
                    effectsModification = CompletionEffectsStateModification.CloseComposer.CloseComposerDraftDiscarded
                )
            ),
            arrayOf(
                "ComposerControlEvent OnComposerRestored to modification",
                EffectsEvent.ComposerControlEvent.OnComposerRestored,
                ComposerStateModifications(
                    effectsModification = CompletionEffectsStateModification.CloseComposer.CloseComposerNoDraft
                )
            ),
            arrayOf(
                "ErrorEvent OnSenderChangeFreeUserError to modification",
                EffectsEvent.ErrorEvent.OnSenderChangeFreeUserError,
                ComposerStateModifications(effectsModification = RecoverableError.SenderChange.FreeUser)
            ),
            arrayOf(
                "ErrorEvent OnSenderChangePermissionsError to modification",
                EffectsEvent.ErrorEvent.OnGetAddressesError,
                ComposerStateModifications(effectsModification = RecoverableError.SenderChange.GetAddressesError)
            ),
            arrayOf(
                "ErrorEvent OnSetExpirationError to modification",
                EffectsEvent.ErrorEvent.OnSetExpirationError,
                ComposerStateModifications(effectsModification = RecoverableError.Expiration)
            ),
            arrayOf(
                "SendEvent OnCancelSendNoSubject to modification",
                EffectsEvent.SendEvent.OnCancelSendNoSubject,
                ComposerStateModifications(
                    effectsModification = ConfirmationsEffectsStateModification.CancelSendNoSubject
                )
            ),
            arrayOf(
                "SendEvent OnCancelSendNoSubject to modification",
                EffectsEvent.SendEvent.OnSendExpiringToExternalRecipients(externalRecipients),
                ComposerStateModifications(
                    effectsModification = ConfirmationsEffectsStateModification.ShowExternalExpiringRecipients(
                        externalRecipients
                    )
                )
            ),
            arrayOf(
                "SendEvent OnSendMessage to modification",
                EffectsEvent.SendEvent.OnSendMessage,
                ComposerStateModifications(
                    effectsModification = CompletionEffectsStateModification.SendMessage.SendAndExit
                )
            ),
            arrayOf(
                "SendEvent OnOfflineSendMessage to modification",
                EffectsEvent.SendEvent.OnOfflineSendMessage,
                ComposerStateModifications(
                    effectsModification = CompletionEffectsStateModification.SendMessage.SendAndExitOffline
                )
            ),
            arrayOf(
                "SendEvent OnSendingError to modification",
                EffectsEvent.SendEvent.OnSendingError("message"),
                ComposerStateModifications(effectsModification = RecoverableError.SendingFailed("message"))
            ),
            arrayOf(
                "SetExpirationReady to modification",
                EffectsEvent.SetExpirationReady,
                ComposerStateModifications(
                    effectsModification = BottomSheetEffectsStateModification.ShowBottomSheet
                )
            ),
            arrayOf(
                "SendEvent OnScheduleSendMessage to modification",
                EffectsEvent.SendEvent.OnScheduleSendMessage,
                ComposerStateModifications(
                    effectsModification = CompletionEffectsStateModification.ScheduleMessage.ScheduleAndExit
                )
            ),
            arrayOf(
                "SendEvent OnOfflineScheduleSendMessage to modification",
                EffectsEvent.SendEvent.OnOfflineScheduleSendMessage,
                ComposerStateModifications(
                    effectsModification = CompletionEffectsStateModification.ScheduleMessage.ScheduleAndExitOffline
                )
            )
        )
    }
}
