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

package ch.protonmail.android.mailcomposer.presentation.reducer

import java.util.UUID
import ch.protonmail.android.mailcommon.domain.sample.UserAddressSample
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.MessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.model.MessagePassword
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ComposerAction
import ch.protonmail.android.mailcomposer.presentation.model.ComposerAction.SenderChanged
import ch.protonmail.android.mailcomposer.presentation.model.ComposerDraftState
import ch.protonmail.android.mailcomposer.presentation.model.ComposerEvent
import ch.protonmail.android.mailcomposer.presentation.model.ComposerFields
import ch.protonmail.android.mailcomposer.presentation.model.ComposerOperation
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.model.DraftUiModel
import ch.protonmail.android.mailcomposer.presentation.model.FocusedFieldType
import ch.protonmail.android.mailcomposer.presentation.model.SenderUiModel
import ch.protonmail.android.mailmessage.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailmessage.domain.model.AttachmentState
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import ch.protonmail.android.mailmessage.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailmessage.domain.sample.RecipientSample
import ch.protonmail.android.mailmessage.presentation.mapper.AttachmentMetadataUiModelMapper
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentGroupUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.NO_ATTACHMENT_LIMIT
import ch.protonmail.android.mailmessage.presentation.sample.AttachmentMetadataUiModelSamples
import ch.protonmail.android.testdata.user.UserIdTestData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@RunWith(Parameterized::class)
class ComposerReducerTest(
    private val testName: String,
    private val testTransition: TestTransition
) {

    private val attachmentUiModelMapper = AttachmentMetadataUiModelMapper()
    private val composerReducer = ComposerReducer(attachmentUiModelMapper)

    @Test
    fun `Test composer transition states`() = runTest {
        with(testTransition) {
            val actualState = composerReducer.newStateFrom(currentState, operation)

            assertEquals(expectedState, actualState, testName)
        }
    }

    companion object {

        private val messageId = MessageId(UUID.randomUUID().toString())
        private val addresses = listOf(UserAddressSample.PrimaryAddress, UserAddressSample.AliasAddress)

        private val draftFields = DraftFields(
            messageId,
            SenderEmail("author@proton.me"),
            Subject("Here is the matter"),
            DraftBody("Decrypted body of this draft"),
            RecipientsTo(listOf(Recipient("you@proton.ch", "Name"))),
            RecipientsCc(emptyList()),
            RecipientsBcc(emptyList())
        )

        private val draftFieldsWithoutRecipients = DraftFields(
            messageId,
            SenderEmail("author@proton.me"),
            Subject("Here is the matter"),
            DraftBody("Decrypted body of this draft"),
            RecipientsTo(emptyList()),
            RecipientsCc(emptyList()),
            RecipientsBcc(emptyList())
        )

        private val draftDisplayBody = DraftDisplayBodyUiModel("<html>draft display body</html>")
        private val draftUiModel = DraftUiModel(draftFields, draftDisplayBody)

        private val draftUiModelWithoutRecipients = DraftUiModel(draftFieldsWithoutRecipients, draftDisplayBody)

        private val EmptyToSubmittableToField = TestTransition(
            name = "Should generate submittable state when recipients updated with a valid recipient",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.RecipientsUpdated(true),
            expectedState = aSubmittableState(messageId)
        )

        private val EmptyToNotSubmittableToField = TestTransition(
            name = "Should generate not submittable error state when recipients update with no valid recipient",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.RecipientsUpdated(false),
            expectedState = aNotSubmittableState(messageId)
        )
        private val SubmittableToNotSubmittableEmptyToField = TestTransition(
            name = "Should generate not-submittable state when removing all valid email addresses",
            currentState = ComposerDraftState.initial(
                messageId,
                isSubmittable = true
            ),
            operation = ComposerEvent.RecipientsUpdated(false),
            expectedState = aNotSubmittableState(messageId, error = Effect.empty())
        )

        private val EmptyToUpgradePlan = TestTransition(
            name = "Should generate a state showing 'upgrade plan' message when free user tries to change sender",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.ErrorFreeUserCannotChangeSender,
            expectedState = aNotSubmittableState(
                draftId = messageId,
                premiumFeatureMessage = Effect.of(TextUiModel(R.string.composer_change_sender_paid_feature)),
                error = Effect.empty()
            )
        )

        private val EmptyToSenderAddressesList = TestTransition(
            name = "Should generate a state showing change sender bottom sheet when paid tries to change sender",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.SenderAddressesReceived(addresses.map { SenderUiModel(it.email) }),
            expectedState = aNotSubmittableState(
                draftId = messageId,
                error = Effect.empty(),
                senderAddresses = addresses.map { SenderUiModel(it.email) },
                changeSenderBottomSheetVisibility = Effect.of(true)
            )
        )

        private val EmptyToErrorWhenUserPlanUnknown = TestTransition(
            name = "Should generate an error state when failing to determine if user can change sender",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.ErrorVerifyingPermissionsToChangeSender,
            expectedState = aNotSubmittableState(
                draftId = messageId,
                error = Effect.of(TextUiModel(R.string.composer_error_change_sender_failed_getting_subscription))
            )
        )

        private val EmptyToUpdatedSender = with(SenderUiModel("updated-sender@proton.ch")) {
            TestTransition(
                name = "Should update the state with the new sender and close bottom sheet when address changes",
                currentState = ComposerDraftState.initial(messageId),
                operation = SenderChanged(this),
                expectedState = aNotSubmittableState(
                    draftId = messageId,
                    sender = this,
                    error = Effect.empty(),
                    changeSenderBottomSheetVisibility = Effect.of(false)
                )
            )
        }

        private val EmptyToChangeSubjectError = TestTransition(
            name = "Should update the state showing an error when error storing draft subject",
            currentState = aNotSubmittableState(draftId = messageId),
            operation = ComposerEvent.ErrorStoringDraftSubject,
            expectedState = aNotSubmittableState(
                draftId = messageId,
                error = Effect.of(TextUiModel(R.string.composer_error_store_draft_subject))
            )
        )

        private val EmptyToUpdatedDraftBody = with(DraftBody("Updated draft body")) {
            TestTransition(
                name = "Should update the state with the new draft body when it changes",
                currentState = ComposerDraftState.initial(messageId),
                operation = ComposerAction.DraftBodyChanged(this),
                expectedState = aNotSubmittableState(
                    draftId = messageId,
                    error = Effect.empty(),
                    draftBody = this.value
                )
            )
        }

        private val EmptyToCloseComposer = TestTransition(
            name = "Should close the composer",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerAction.OnCloseComposer,
            expectedState = aNotSubmittableState(
                draftId = messageId,
                error = Effect.empty(),
                closeComposer = Effect.of(Unit),
                closeComposerWithDraftSaved = Effect.empty()
            )
        )

        private val EmptyToCloseComposerWithDraftSaved = TestTransition(
            name = "Should close the composer notifying draft saved",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.OnCloseWithDraftSaved,
            expectedState = aNotSubmittableState(
                draftId = messageId,
                error = Effect.empty(),
                closeComposer = Effect.empty(),
                closeComposerWithDraftSaved = Effect.of(Unit)
            )
        )

        private val SubmittableToSendMessage =
            TestTransition(
                name = "Should update submittable state with message sending after OnSendMessage action",
                currentState = aSubmittableState(messageId),
                operation = ComposerAction.OnSendMessage,
                expectedState = aSubmittableState(
                    messageId,
                    closeComposerWithMessageSending = Effect.of(Unit)
                )
            )

        private val SubmittableToOnSendMessageOffline =
            TestTransition(
                name = "Should update submittable state with message sending after OnSendMessageOffline action",
                currentState = aSubmittableState(messageId),
                operation = ComposerEvent.OnSendMessageOffline,
                expectedState = aSubmittableState(
                    messageId,
                    closeComposerWithMessageSendingOffline = Effect.of(Unit)
                )
            )

        private val EmptyToLoadingWithOpenExistingDraft = TestTransition(
            name = "Should set state to loading when open of existing draft was requested",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.OpenExistingDraft,
            expectedState = aNotSubmittableState(
                draftId = messageId,
                error = Effect.empty(),
                isLoading = true
            )
        )

        @Suppress("VariableMaxLength")
        private val LoadingToFieldsWhenReceivedDraftDataFromRemote = TestTransition(
            name = "Should stop loading and set the received draft data as composer fields when draft data received",
            currentState = ComposerDraftState.initial(messageId).copy(isLoading = true),
            operation = ComposerEvent.PrefillDraftDataReceived(
                draftUiModel,
                isDataRefreshed = true,
                isBlockedSendingFromPmAddress = false,
                isBlockedSendingFromDisabledAddress = false
            ),
            expectedState = aNotSubmittableState(
                draftId = messageId,
                sender = SenderUiModel(draftFieldsWithoutRecipients.sender.value),
                draftBody = draftFieldsWithoutRecipients.body.value,
                draftDisplayBodyUiModel = draftDisplayBody,
                error = Effect.empty()
            )
        )

        @Suppress("VariableMaxLength")
        private val LoadingToFieldsWhenReceivedDraftDataFromLocal = TestTransition(
            name = "Should stop loading and set the received draft data as composer fields when draft data received",
            currentState = ComposerDraftState.initial(messageId).copy(isLoading = true),
            operation = ComposerEvent.PrefillDraftDataReceived(
                draftUiModel,
                isDataRefreshed = false,
                isBlockedSendingFromPmAddress = false,
                isBlockedSendingFromDisabledAddress = false
            ),
            expectedState = aNotSubmittableState(
                draftId = messageId,
                sender = SenderUiModel(draftFieldsWithoutRecipients.sender.value),
                draftBody = draftFieldsWithoutRecipients.body.value,
                draftDisplayBodyUiModel = draftDisplayBody,
                warning = Effect.of(TextUiModel(R.string.composer_warning_local_data_shown))
            )
        )

        @Suppress("VariableMaxLength")
        private val LoadingToFieldsWhenReceivedDraftDataFromViaShare = TestTransition(
            name = "Should stop loading and set the received draft data as composer fields when draft data received, " +
                " via share",
            currentState = ComposerDraftState.initial(messageId).copy(isLoading = true),
            operation = ComposerEvent.PrefillDataReceivedViaShare(draftUiModel),
            expectedState = aNotSubmittableState(
                draftId = messageId,
                sender = SenderUiModel(draftFieldsWithoutRecipients.sender.value),
                draftBody = draftFieldsWithoutRecipients.body.value,
                draftDisplayBodyUiModel = draftDisplayBody,
                warning = Effect.empty()
            )
        )

        @Suppress("VariableMaxLength")
        private val LoadingToSendingNoticeWhenReceivedDraftDataWithInvalidSender = TestTransition(
            name = "Should stop loading and show the sending notice when prefilled address in invalid",
            currentState = ComposerDraftState.initial(messageId).copy(isLoading = true),
            operation = ComposerEvent.PrefillDraftDataReceived(
                draftUiModelWithoutRecipients,
                isDataRefreshed = true,
                isBlockedSendingFromPmAddress = true,
                isBlockedSendingFromDisabledAddress = false
            ),
            expectedState = aNotSubmittableState(
                draftId = messageId,
                sender = SenderUiModel(draftFieldsWithoutRecipients.sender.value),
                draftBody = draftFieldsWithoutRecipients.body.value,
                draftDisplayBodyUiModel = draftDisplayBody,
                error = Effect.empty(),
                isLoading = false,
                senderChangedNotice = Effect.of(
                    TextUiModel(R.string.composer_sender_changed_pm_address_is_a_paid_feature)
                )
            )
        )

        @Suppress("VariableMaxLength")
        private val EmptyToStateWhenReplaceDraftBody = TestTransition(
            name = "Should update the state with new DraftBody Effect when ReplaceDraftBody was emitted",
            currentState = aNotSubmittableState(draftId = messageId),
            operation = ComposerEvent.ReplaceDraftBody(draftFieldsWithoutRecipients.body),
            expectedState = aNotSubmittableState(
                draftId = messageId,
                replaceDraftBody = Effect.of(TextUiModel(draftFieldsWithoutRecipients.body.value))
            )
        )

        private val LoadingToErrorWhenErrorLoadingDraftData = TestTransition(
            name = "Should stop loading and display error when failing to receive draft data",
            currentState = ComposerDraftState.initial(messageId).copy(isLoading = true),
            operation = ComposerEvent.ErrorLoadingDraftData,
            expectedState = aNotSubmittableState(
                draftId = messageId,
                error = Effect.of(TextUiModel(R.string.composer_error_loading_draft)),
                isLoading = false
            )
        )

        private val EmptyToBottomSheetOpened = TestTransition(
            name = "Should open the file picker when add attachments action is chosen",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerAction.OnAddAttachments,
            expectedState = ComposerDraftState.initial(messageId).copy(
                openImagePicker = Effect.of(Unit)
            )
        )

        private val EmptyToAttachmentsUpdated = TestTransition(
            name = "Should emit attachments when they are updated",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.OnAttachmentsUpdated(
                listOf(
                    AttachmentMetadataWithState(AttachmentMetadataSamples.Invoice, AttachmentState.Uploaded)
                )
            ),
            expectedState = ComposerDraftState.initial(messageId).copy(
                attachments = AttachmentGroupUiModel(
                    limit = NO_ATTACHMENT_LIMIT,
                    attachments = listOf(
                        AttachmentMetadataUiModelSamples.DeletableInvoice.copy(
                            status = AttachmentState.Uploaded
                        )
                    )
                )
            )
        )

        private val EmptyToAttachmentFileExceeded = TestTransition(
            name = "Should emit attachment exceeded file limit",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.ErrorAttachmentsExceedSizeLimit,
            expectedState = ComposerDraftState.initial(messageId).copy(
                attachmentsFileSizeExceeded = Effect.of(Unit)
            )
        )

        private val EmptyToAttachmentReEncryptionFailed = TestTransition(
            name = "Should emit attachment exceeded file limit",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.ErrorAttachmentsReEncryption,
            expectedState = ComposerDraftState.initial(messageId).copy(
                attachmentsReEncryptionFailed = Effect.of(Unit)
            )
        )

        private val EmptyToOnSendingError = TestTransition(
            name = "Should emit sending error",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.OnSendingError(TextUiModel.Text("SendingError")),
            expectedState = ComposerDraftState.initial(messageId).copy(
                sendingErrorEffect = Effect.of(TextUiModel.Text("SendingError"))
            )
        )

        private val EmptyToOnMessagePasswordUpdated = TestTransition(
            name = "Should update state with info whether a message password is set",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.OnMessagePasswordUpdated(
                MessagePassword(
                    UserIdTestData.userId,
                    messageId,
                    "password",
                    null
                )
            ),
            expectedState = ComposerDraftState.initial(messageId).copy(
                isMessagePasswordSet = true
            )
        )
        private val SubmittableToRequestConfirmEmptySubject = TestTransition(
            name = "Should update state to request confirmation for sending without subject",
            currentState = aSubmittableState(
                messageId,
                confirmSendingWithoutSubject = Effect.empty()
            ),
            operation = ComposerEvent.ConfirmEmptySubject,
            expectedState = aSubmittableState(
                messageId,
                confirmSendingWithoutSubject = Effect.of(Unit)
            )
        )

        private val SubmittableToConfirmEmptySubject = TestTransition(
            name = "Should update state to confirm sending without subject",
            currentState = aSubmittableState(
                messageId,
                confirmSendingWithoutSubject = Effect.of(Unit)
            ),
            operation = ComposerAction.ConfirmSendingWithoutSubject,
            expectedState = aSubmittableState(
                messageId,
                confirmSendingWithoutSubject = Effect.empty(),
                closeComposerWithMessageSending = Effect.of(Unit)
            )
        )

        private val SubmittableToRejectEmptySubject = TestTransition(
            name = "Should update state to reject sending without subject",
            currentState = aSubmittableState(
                messageId,
                confirmSendingWithoutSubject = Effect.of(Unit),
                changeFocusToField = Effect.empty()
            ),
            operation = ComposerAction.RejectSendingWithoutSubject,
            expectedState = aSubmittableState(
                messageId,
                confirmSendingWithoutSubject = Effect.empty(),
                changeFocusToField = Effect.of(FocusedFieldType.SUBJECT)
            )
        )

        private val EmptyToSetExpirationTimeRequested = TestTransition(
            name = "Should update state to open expiration time bottom sheet",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerAction.OnSetExpirationTimeRequested,
            expectedState = ComposerDraftState.initial(messageId).copy(changeBottomSheetVisibility = Effect.of(true))
        )

        private val EmptyToExpirationTimeSet = TestTransition(
            name = "Should update state to open expiration time bottom sheet",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerAction.ExpirationTimeSet(duration = 1.days),
            expectedState = ComposerDraftState.initial(messageId).copy(changeBottomSheetVisibility = Effect.of(false))
        )

        private val EmptyToErrorSettingExpirationTime = TestTransition(
            name = "Should update state to an error when setting expiration time failed",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.ErrorSettingExpirationTime,
            expectedState = ComposerDraftState.initial(messageId).copy(
                error = Effect.of(TextUiModel(R.string.composer_error_setting_expiration_time))
            )
        )

        private val EmptyToMessageExpirationTimeUpdated = TestTransition(
            name = "Should update state with message expiration time",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.OnMessageExpirationTimeUpdated(
                MessageExpirationTime(UserIdTestData.userId, messageId, 1.days)
            ),
            expectedState = ComposerDraftState.initial(messageId).copy(messageExpiresIn = 1.days)
        )

        private val EmptyToConfirmSendExpiringMessage = TestTransition(
            name = "Should update state with an effect when sending an expiring message to external recipients",
            currentState = ComposerDraftState.initial(messageId),
            operation = ComposerEvent.ConfirmSendExpiringMessageToExternalRecipients(
                listOf(RecipientSample.ExternalEncrypted)
            ),
            expectedState = ComposerDraftState.initial(messageId).copy(
                confirmSendExpiringMessage = Effect.of(listOf(RecipientSample.ExternalEncrypted))
            )
        )

        private val SubmittableToDiscardDraft = TestTransition(
            name = "Should update state with an effect to close composer when discarding a draft",
            currentState = aSubmittableState(draftId = messageId),
            operation = ComposerAction.DiscardDraft,
            expectedState = aSubmittableState(draftId = messageId).copy(closeComposer = Effect.of(Unit))
        )

        private val transitions = listOf(
            EmptyToSubmittableToField,
            EmptyToNotSubmittableToField,
            SubmittableToNotSubmittableEmptyToField,
            EmptyToUpgradePlan,
            EmptyToSenderAddressesList,
            EmptyToErrorWhenUserPlanUnknown,
            EmptyToUpdatedSender,
            EmptyToChangeSubjectError,
            EmptyToUpdatedDraftBody,
            EmptyToCloseComposer,
            EmptyToCloseComposerWithDraftSaved,
            EmptyToStateWhenReplaceDraftBody,
            SubmittableToSendMessage,
            SubmittableToOnSendMessageOffline,
            EmptyToLoadingWithOpenExistingDraft,
            LoadingToFieldsWhenReceivedDraftDataFromRemote,
            LoadingToFieldsWhenReceivedDraftDataFromLocal,
            LoadingToFieldsWhenReceivedDraftDataFromViaShare,
            LoadingToErrorWhenErrorLoadingDraftData,
            LoadingToSendingNoticeWhenReceivedDraftDataWithInvalidSender,
            EmptyToBottomSheetOpened,
            EmptyToAttachmentsUpdated,
            EmptyToAttachmentFileExceeded,
            EmptyToAttachmentReEncryptionFailed,
            EmptyToOnSendingError,
            EmptyToOnMessagePasswordUpdated,
            SubmittableToRequestConfirmEmptySubject,
            SubmittableToConfirmEmptySubject,
            SubmittableToRejectEmptySubject,
            EmptyToSetExpirationTimeRequested,
            EmptyToExpirationTimeSet,
            EmptyToErrorSettingExpirationTime,
            EmptyToMessageExpirationTimeUpdated,
            EmptyToConfirmSendExpiringMessage,
            SubmittableToDiscardDraft
        )

        private fun aSubmittableState(
            draftId: MessageId,
            sender: SenderUiModel = SenderUiModel(""),
            draftBody: String = "",
            draftDisplayBodyUiModel: DraftDisplayBodyUiModel = DraftDisplayBodyUiModel(""),
            error: Effect<TextUiModel> = Effect.empty(),
            closeComposerWithMessageSending: Effect<Unit> = Effect.empty(),
            closeComposerWithMessageSendingOffline: Effect<Unit> = Effect.empty(),
            confirmSendingWithoutSubject: Effect<Unit> = Effect.empty(),
            changeFocusToField: Effect<FocusedFieldType> = Effect.empty(),
            attachmentsFileSizeExceeded: Effect<Unit> = Effect.empty(),
            attachmentReEncryptionFailed: Effect<Unit> = Effect.empty(),
            warning: Effect<TextUiModel> = Effect.empty(),
            replaceDraftBody: Effect<TextUiModel> = Effect.empty()
        ) = ComposerDraftState(
            fields = ComposerFields(
                draftId = draftId,
                sender = sender,
                displayBody = draftDisplayBodyUiModel,
                body = draftBody
            ),
            attachments = AttachmentGroupUiModel(attachments = emptyList()),
            premiumFeatureMessage = Effect.empty(),
            error = error,
            isSubmittable = true,
            senderAddresses = emptyList(),
            changeBottomSheetVisibility = Effect.empty(),
            closeComposer = Effect.empty(),
            closeComposerWithDraftSaved = Effect.empty(),
            closeComposerWithMessageSending = closeComposerWithMessageSending,
            closeComposerWithMessageSendingOffline = closeComposerWithMessageSendingOffline,
            confirmSendingWithoutSubject = confirmSendingWithoutSubject,
            changeFocusToField = changeFocusToField,
            isLoading = false,
            attachmentsFileSizeExceeded = attachmentsFileSizeExceeded,
            attachmentsReEncryptionFailed = attachmentReEncryptionFailed,
            replaceDraftBody = replaceDraftBody,
            warning = warning,
            isMessagePasswordSet = false,
            messageExpiresIn = Duration.ZERO,
            confirmSendExpiringMessage = Effect.empty(),
            openImagePicker = Effect.empty()
        )

        private fun aNotSubmittableState(
            draftId: MessageId,
            sender: SenderUiModel = SenderUiModel(""),
            error: Effect<TextUiModel> = Effect.empty(),
            premiumFeatureMessage: Effect<TextUiModel> = Effect.empty(),
            senderAddresses: List<SenderUiModel> = emptyList(),
            changeSenderBottomSheetVisibility: Effect<Boolean> = Effect.empty(),
            draftBody: String = "",
            draftDisplayBodyUiModel: DraftDisplayBodyUiModel = DraftDisplayBodyUiModel(""),
            closeComposer: Effect<Unit> = Effect.empty(),
            closeComposerWithDraftSaved: Effect<Unit> = Effect.empty(),
            isLoading: Boolean = false,
            attachmentsFileSizeExceeded: Effect<Unit> = Effect.empty(),
            attachmentReEncryptionFailed: Effect<Unit> = Effect.empty(),
            warning: Effect<TextUiModel> = Effect.empty(),
            replaceDraftBody: Effect<TextUiModel> = Effect.empty(),
            senderChangedNotice: Effect<TextUiModel> = Effect.empty()
        ) = ComposerDraftState(
            fields = ComposerFields(
                draftId = draftId,
                sender = sender,
                displayBody = draftDisplayBodyUiModel,
                body = draftBody
            ),
            attachments = AttachmentGroupUiModel(attachments = emptyList()),
            premiumFeatureMessage = premiumFeatureMessage,
            error = error,
            isSubmittable = false,
            senderAddresses = senderAddresses,
            changeBottomSheetVisibility = changeSenderBottomSheetVisibility,
            closeComposer = closeComposer,
            closeComposerWithDraftSaved = closeComposerWithDraftSaved,
            closeComposerWithMessageSending = Effect.empty(),
            closeComposerWithMessageSendingOffline = Effect.empty(),
            confirmSendingWithoutSubject = Effect.empty(),
            changeFocusToField = Effect.empty(),
            isLoading = isLoading,
            attachmentsFileSizeExceeded = attachmentsFileSizeExceeded,
            attachmentsReEncryptionFailed = attachmentReEncryptionFailed,
            replaceDraftBody = replaceDraftBody,
            warning = warning,
            isMessagePasswordSet = false,
            senderChangedNotice = senderChangedNotice,
            messageExpiresIn = Duration.ZERO,
            confirmSendExpiringMessage = Effect.empty(),
            openImagePicker = Effect.empty()
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = transitions.map { test -> arrayOf(test.name, test) }

        data class TestTransition(
            val name: String,
            val currentState: ComposerDraftState,
            val operation: ComposerOperation,
            val expectedState: ComposerDraftState
        )
    }
}
