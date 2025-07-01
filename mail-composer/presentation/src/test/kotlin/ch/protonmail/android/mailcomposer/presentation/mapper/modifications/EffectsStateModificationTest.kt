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

package ch.protonmail.android.mailcomposer.presentation.mapper.modifications

import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailattachments.domain.model.AttachmentState
import ch.protonmail.android.mailattachments.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddError
import ch.protonmail.android.mailcomposer.domain.model.AttachmentAddErrorWithList
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.model.DraftUiModel
import ch.protonmail.android.mailcomposer.presentation.model.FocusedFieldType
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.BottomSheetEffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.CompletionEffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.ConfirmationsEffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.ContentEffectsStateModifications
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.EffectsStateModification
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.RecoverableError
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.effects.UnrecoverableError
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import io.mockk.mockk
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class EffectsStateModificationTest(
    @Suppress("unused") private val testName: String,
    private val initialState: ComposerState.Effects,
    private val modification: EffectsStateModification,
    private val expectedState: ComposerState.Effects
) {

    @Test
    fun `should apply the modification`() {
        val updatedState = modification.apply(initialState)
        assertEquals(expectedState, updatedState)
    }

    companion object {

        private val initialState = ComposerState.Effects.initial()
        private val externalRecipients = listOf(mockk<Recipient>())
        private val draftDisplayBody = DraftDisplayBodyUiModel("<html>draft display body</html>")
        private val draftFields = DraftFields(
            SenderEmail("author@proton.me"),
            Subject("Here is the matter"),
            DraftBody("Decrypted body of this draft"),
            RecipientsTo(listOf(Recipient("you@proton.ch", "Name"))),
            RecipientsCc(emptyList()),
            RecipientsBcc(emptyList())
        )
        private val draftUiModel = DraftUiModel(draftFields, draftDisplayBody)

        private val invoiceAttachment = AttachmentMetadataSamples.Invoice
        private val invoiceId = invoiceAttachment.attachmentId

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "shows invalid sender error",
                initialState,
                UnrecoverableError.InvalidSenderAddress,
                initialState.copy(exitError = Effect.of(TextUiModel(R.string.composer_error_invalid_sender)))
            ),
            arrayOf(
                "shows draft content loading error",
                initialState,
                UnrecoverableError.DraftContentUnavailable,
                initialState.copy(exitError = Effect.of(TextUiModel(R.string.composer_error_loading_draft)))
            ),
            arrayOf(
                "shows parent message loading error",
                initialState,
                UnrecoverableError.ParentMessageMetadata,
                initialState.copy(exitError = Effect.of(TextUiModel(R.string.composer_error_loading_parent_message)))
            ),
            arrayOf(
                "shows free user sender change error (paid feature)",
                initialState,
                RecoverableError.SenderChange.FreeUser,
                initialState.copy(
                    premiumFeatureMessage = Effect.of(TextUiModel(R.string.composer_change_sender_paid_feature))
                )
            ),
            arrayOf(
                "shows failed getting addresses on sender change error",
                initialState,
                RecoverableError.SenderChange.GetAddressesError,
                initialState.copy(
                    error = Effect.of(TextUiModel(R.string.composer_error_change_sender_failed_getting_addresses)),
                    changeBottomSheetVisibility = Effect.of(false)
                )
            ),
            arrayOf(
                "shows attachments unexpected",
                initialState,
                RecoverableError.AttachmentsStore(AttachmentAddError.InvalidDraftMessage),
                initialState.copy(
                    error = Effect.of(TextUiModel.TextRes(R.string.composer_unexpected_attachments_error))
                )
            ),
            arrayOf(
                "shows attachments number exceeded error",
                initialState,
                RecoverableError.AttachmentsStore(AttachmentAddError.TooManyAttachments),
                initialState.copy(error = Effect.of(TextUiModel.TextRes(R.string.composer_too_many_attachments_error)))
            ),
            arrayOf(
                "show file size exceeded error",
                initialState,
                RecoverableError.AttachmentsStore(AttachmentAddError.AttachmentTooLarge),
                initialState.copy(attachmentsFileSizeExceeded = Effect.of(emptyList()))
            ),
            arrayOf(
                "shows expiration error",
                initialState,
                RecoverableError.Expiration,
                initialState.copy(
                    error = Effect.of(TextUiModel(R.string.composer_error_setting_expiration_time)),
                    changeBottomSheetVisibility = Effect.of(false)
                )
            ),
            arrayOf(
                "shows sending failed error",
                initialState,
                RecoverableError.SendingFailed("Test error"),
                initialState.copy(sendingErrorEffect = Effect.of(TextUiModel.Text("Test error")))
            ),
            arrayOf(
                "shows file picker and hides bottom sheet",
                initialState,
                ContentEffectsStateModifications.OnAddAttachmentFileRequested,
                initialState.copy(
                    openFilesPicker = Effect.of(Unit),
                    changeBottomSheetVisibility = Effect.of(false)
                )
            ),
            arrayOf(
                "shows media picker and hides bottom sheet",
                initialState,
                ContentEffectsStateModifications.OnAddAttachmentPhotosRequested,
                initialState.copy(
                    openPhotosPicker = Effect.of(Unit),
                    changeBottomSheetVisibility = Effect.of(false)
                )
            ),
            arrayOf(
                "shows camera to grab pic and hides bottom sheet",
                initialState,
                ContentEffectsStateModifications.OnAddAttachmentCameraRequested,
                initialState.copy(openCamera = Effect.of(Unit), changeBottomSheetVisibility = Effect.of(false))
            ),
            arrayOf(
                "handles draft content ready with valid sender",
                initialState,
                ContentEffectsStateModifications.DraftContentReady(
                    draftUiModel,
                    isDataRefresh = true,
                    forceBodyFocus = true
                ),
                initialState.copy(focusTextBody = Effect.of(Unit))
            ),
            arrayOf(
                "closes composer with saved draft",
                initialState,
                CompletionEffectsStateModification.CloseComposer.CloseComposerDraftSaved(MessageId("123")),
                initialState.copy(closeComposerWithDraftSaved = Effect.of(MessageId("123")))
            ),
            arrayOf(
                "closes composer without saved draft",
                initialState,
                CompletionEffectsStateModification.CloseComposer.CloseComposerNoDraft,
                initialState.copy(closeComposer = Effect.of(Unit))
            ),
            arrayOf(
                "sends message and exit",
                initialState,
                CompletionEffectsStateModification.SendMessage.SendAndExit,
                initialState.copy(closeComposerWithMessageSending = Effect.of(Unit))
            ),
            arrayOf(
                "sends message and exit offline",
                initialState,
                CompletionEffectsStateModification.SendMessage.SendAndExitOffline,
                initialState.copy(closeComposerWithMessageSendingOffline = Effect.of(Unit))
            ),
            arrayOf(
                "shows bottom sheet",
                initialState,
                BottomSheetEffectsStateModification.ShowBottomSheet,
                initialState.copy(changeBottomSheetVisibility = Effect.of(true))
            ),
            arrayOf(
                "hides bottom sheet",
                initialState,
                BottomSheetEffectsStateModification.HideBottomSheet,
                initialState.copy(changeBottomSheetVisibility = Effect.of(false))
            ),
            arrayOf(
                "requests no subject confirmation",
                initialState,
                ConfirmationsEffectsStateModification.SendNoSubjectConfirmationRequested,
                initialState.copy(confirmSendingWithoutSubject = Effect.of(Unit))
            ),
            arrayOf(
                "cancels no subject confirmation",
                initialState,
                ConfirmationsEffectsStateModification.CancelSendNoSubject,
                initialState.copy(
                    changeFocusToField = Effect.of(FocusedFieldType.SUBJECT),
                    confirmSendingWithoutSubject = Effect.empty()
                )
            ),
            arrayOf(
                "shows external expiring recipients confirmation",
                initialState,
                ConfirmationsEffectsStateModification.ShowExternalExpiringRecipients(externalRecipients),
                initialState.copy(confirmSendExpiringMessage = Effect.of(externalRecipients))
            ),
            arrayOf(
                "removes inline attachments and hide bottomsheet",
                initialState,
                ContentEffectsStateModifications.OnInlineAttachmentRemoved("cid-123"),
                initialState.copy(
                    stripInlineAttachment = Effect.of("cid-123"),
                    changeBottomSheetVisibility = Effect.of(false)
                )
            ),
            arrayOf(
                "schedule sends message and exit",
                initialState,
                CompletionEffectsStateModification.ScheduleMessage.ScheduleAndExit,
                initialState.copy(closeComposerWithScheduleSending = Effect.of(Unit))
            ),
            arrayOf(
                "schedule sends message and exit offline",
                initialState,
                CompletionEffectsStateModification.ScheduleMessage.ScheduleAndExitOffline,
                initialState.copy(closeComposerWithScheduleSendingOffline = Effect.of(Unit))
            ),
            arrayOf(
                "attachment list changed with TooManyAttachments error",
                initialState,
                RecoverableError.AttachmentsListChangedWithError(
                    AttachmentAddErrorWithList(
                        error = AttachmentAddError.TooManyAttachments,
                        failedAttachments = listOf(
                            errorAttachment(DataError.Local.AttachmentError.TooManyAttachments)
                        )
                    )
                ),
                initialState.copy(
                    attachmentsFileSizeExceeded = Effect.of(listOf(invoiceId))
                )
            ),
            arrayOf(
                "attachment list changed with AttachmentTooLarge error",
                initialState,
                RecoverableError.AttachmentsListChangedWithError(
                    AttachmentAddErrorWithList(
                        error = AttachmentAddError.AttachmentTooLarge,
                        failedAttachments = listOf(
                            errorAttachment(DataError.Local.AttachmentError.AttachmentTooLarge)
                        )
                    )
                ),
                initialState.copy(
                    attachmentsFileSizeExceeded = Effect.of(listOf(invoiceId))
                )
            ),
            arrayOf(
                "attachment list changed with EncryptionError",
                initialState,
                RecoverableError.AttachmentsListChangedWithError(
                    AttachmentAddErrorWithList(
                        error = AttachmentAddError.EncryptionError,
                        failedAttachments = listOf(
                            errorAttachment(DataError.Local.AttachmentError.EncryptionError)
                        )
                    )
                ),
                initialState.copy(
                    error = Effect.of(TextUiModel.TextRes(R.string.composer_unexpected_attachments_error))
                )
            ),
            arrayOf(
                "attachment list changed with InvalidDraftMessage error",
                initialState,
                RecoverableError.AttachmentsListChangedWithError(
                    AttachmentAddErrorWithList(
                        error = AttachmentAddError.InvalidDraftMessage,
                        failedAttachments = listOf(
                            errorAttachment(DataError.Local.AttachmentError.InvalidDraftMessage)
                        )
                    )
                ),
                initialState.copy(
                    error = Effect.of(TextUiModel.TextRes(R.string.composer_unexpected_attachments_error))
                )
            ),
            arrayOf(
                "attachment list changed with Unknown error",
                initialState,
                RecoverableError.AttachmentsListChangedWithError(
                    AttachmentAddErrorWithList(
                        error = AttachmentAddError.Unknown,
                        failedAttachments = listOf(
                            errorAttachment(
                                DataError.Local.AttachmentError.AttachmentTooLarge
                            )
                        )
                    )
                ),
                initialState.copy(
                    error = Effect.of(TextUiModel.TextRes(R.string.composer_unexpected_attachments_error))
                )
            ),
            arrayOf(
                "draft sender changed refreshed body and hides bottomsheet",
                initialState,
                ContentEffectsStateModifications.DraftSenderChanged(draftDisplayBody),
                initialState.copy(
                    refreshBody = Effect.of(draftDisplayBody),
                    changeBottomSheetVisibility = Effect.of(false)
                )
            )
        )

        private fun errorAttachment(error: DataError.Local): AttachmentMetadataWithState = AttachmentMetadataWithState(
            attachmentMetadata = invoiceAttachment,
            attachmentState = AttachmentState.Error(reason = error)
        )
    }
}
