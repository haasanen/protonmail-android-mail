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
import ch.protonmail.android.mailcomposer.domain.usecase.AttachmentAddError
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
            SenderEmail("author@proton.me"),
            Subject("Here is the matter"),
            DraftBody("Decrypted body of this draft"),
            RecipientsTo(listOf(Recipient("you@proton.ch", "Name"))),
            RecipientsCc(emptyList()),
            RecipientsBcc(emptyList())
        )

        private val draftFieldsWithoutRecipients = DraftFields(
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
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.RecipientsUpdated(true),
            expectedState = aSubmittableState()
        )

        private val EmptyToNotSubmittableToField = TestTransition(
            name = "Should generate not submittable error state when recipients update with no valid recipient",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.RecipientsUpdated(false),
            expectedState = aNotSubmittableState()
        )
        private val SubmittableToNotSubmittableEmptyToField = TestTransition(
            name = "Should generate not-submittable state when removing all valid email addresses",
            currentState = ComposerDraftState.initial(isSubmittable = true),
            operation = ComposerEvent.RecipientsUpdated(false),
            expectedState = aNotSubmittableState(error = Effect.empty())
        )

        private val EmptyToUpgradePlan = TestTransition(
            name = "Should generate a state showing 'upgrade plan' message when free user tries to change sender",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.ErrorFreeUserCannotChangeSender,
            expectedState = aNotSubmittableState(
                premiumFeatureMessage = Effect.of(TextUiModel(R.string.composer_change_sender_paid_feature)),
                error = Effect.empty()
            )
        )

        private val EmptyToSenderAddressesList = TestTransition(
            name = "Should generate a state showing change sender bottom sheet when paid tries to change sender",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.SenderAddressesReceived(addresses.map { SenderUiModel(it.email) }),
            expectedState = aNotSubmittableState(
                error = Effect.empty(),
                senderAddresses = addresses.map { SenderUiModel(it.email) },
                changeSenderBottomSheetVisibility = Effect.of(true)
            )
        )

        private val EmptyToErrorWhenUserPlanUnknown = TestTransition(
            name = "Should generate an error state when failing to determine if user can change sender",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.ErrorVerifyingPermissionsToChangeSender,
            expectedState = aNotSubmittableState(
                error = Effect.of(TextUiModel(R.string.composer_error_change_sender_failed_getting_subscription))
            )
        )

        private val EmptyToUpdatedSender = with(SenderUiModel("updated-sender@proton.ch")) {
            TestTransition(
                name = "Should update the state with the new sender and close bottom sheet when address changes",
                currentState = ComposerDraftState.initial(),
                operation = SenderChanged(this),
                expectedState = aNotSubmittableState(
                    sender = this,
                    error = Effect.empty(),
                    changeSenderBottomSheetVisibility = Effect.of(false)
                )
            )
        }

        private val EmptyToChangeSubjectError = TestTransition(
            name = "Should update the state showing an error when error storing draft subject",
            currentState = aNotSubmittableState(),
            operation = ComposerEvent.ErrorStoringDraftSubject,
            expectedState = aNotSubmittableState(
                error = Effect.of(TextUiModel(R.string.composer_error_store_draft_subject))
            )
        )

        private val DraftBodyChangedActionShouldDoNothing = with(DraftBody("Updated draft body")) {
            TestTransition(
                name = "Should do nothing when draft body changed action is reduced (dedicated event)",
                currentState = ComposerDraftState.initial(),
                operation = ComposerAction.DraftBodyChanged(this),
                expectedState = aNotSubmittableState(error = Effect.empty())
            )
        }

        private val EmptyToUpdatedDraftBody = with(DraftBody("Updated draft body")) {
            val displayBodyUiModel = DraftDisplayBodyUiModel("<html>$this</html>")
            TestTransition(
                name = "Should update the state with the new draft body when it changes",
                currentState = ComposerDraftState.initial(),
                operation = ComposerEvent.OnDraftBodyUpdated(this, displayBodyUiModel),
                expectedState = aNotSubmittableState(
                    error = Effect.empty(),
                    draftBody = this.value,
                    draftDisplayBodyUiModel = displayBodyUiModel
                )
            )
        }

        private val EmptyToCloseComposer = TestTransition(
            name = "Should close the composer",
            currentState = ComposerDraftState.initial(),
            operation = ComposerAction.OnCloseComposer,
            expectedState = aNotSubmittableState(
                error = Effect.empty(),
                closeComposer = Effect.of(Unit),
                closeComposerWithDraftSaved = Effect.empty()
            )
        )

        private val EmptyToCloseComposerWithDraftSaved = TestTransition(
            name = "Should close the composer notifying draft saved",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.OnCloseWithDraftSaved(messageId),
            expectedState = aNotSubmittableState(
                error = Effect.empty(),
                closeComposer = Effect.empty(),
                closeComposerWithDraftSaved = Effect.of(messageId)
            )
        )

        private val SubmittableToSendMessage =
            TestTransition(
                name = "Should update submittable state with message sending after OnSendMessage action",
                currentState = aSubmittableState(),
                operation = ComposerAction.OnSendMessage,
                expectedState = aSubmittableState(
                    closeComposerWithMessageSending = Effect.of(Unit)
                )
            )

        private val SubmittableToOnSendMessageOffline =
            TestTransition(
                name = "Should update submittable state with message sending after OnSendMessageOffline action",
                currentState = aSubmittableState(),
                operation = ComposerEvent.OnSendMessageOffline,
                expectedState = aSubmittableState(
                    closeComposerWithMessageSendingOffline = Effect.of(Unit)
                )
            )

        private val EmptyToLoadingWithOpenExistingDraft = TestTransition(
            name = "Should set state to loading when open of existing draft was requested",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.OpenDraft,
            expectedState = aNotSubmittableState(
                error = Effect.empty(),
                isLoading = true
            )
        )

        @Suppress("VariableMaxLength")
        private val LoadingToFieldsWhenReceivedDraftDataFromRemote = TestTransition(
            name = "Should stop loading and set the received draft data as composer fields when draft data received",
            currentState = ComposerDraftState.initial().copy(isLoading = true),
            operation = ComposerEvent.PrefillDraftDataReceived(
                draftUiModel,
                isDataRefreshed = true,
                isBlockedSendingFromPmAddress = false,
                isBlockedSendingFromDisabledAddress = false,
                bodyShouldTakeFocus = false
            ),
            expectedState = aNotSubmittableState(
                sender = SenderUiModel(draftFieldsWithoutRecipients.sender.value),
                draftBody = draftFieldsWithoutRecipients.body.value,
                draftDisplayBodyUiModel = draftDisplayBody,
                error = Effect.empty()
            )
        )

        @Suppress("VariableMaxLength")
        private val LoadingToFieldsWhenReceivedDraftDataFromLocal = TestTransition(
            name = "Should stop loading and set the received draft data as composer fields when draft data received",
            currentState = ComposerDraftState.initial().copy(isLoading = true),
            operation = ComposerEvent.PrefillDraftDataReceived(
                draftUiModel,
                isDataRefreshed = false,
                isBlockedSendingFromPmAddress = false,
                isBlockedSendingFromDisabledAddress = false,
                bodyShouldTakeFocus = false
            ),
            expectedState = aNotSubmittableState(
                sender = SenderUiModel(draftFieldsWithoutRecipients.sender.value),
                draftBody = draftFieldsWithoutRecipients.body.value,
                draftDisplayBodyUiModel = draftDisplayBody,
                warning = Effect.of(TextUiModel(R.string.composer_warning_local_data_shown))
            )
        )

        @Suppress("VariableMaxLength")
        private val LoadingDraftAndBodyShouldBeFocused = TestTransition(
            name = "Loading Draft with a body that should take focus then FocusTextBody",
            currentState = ComposerDraftState.initial().copy(isLoading = true),
            operation = ComposerEvent.PrefillDraftDataReceived(
                draftUiModel,
                isDataRefreshed = true,
                isBlockedSendingFromPmAddress = false,
                isBlockedSendingFromDisabledAddress = false,
                bodyShouldTakeFocus = true
            ),
            expectedState = aNotSubmittableState(
                sender = SenderUiModel(draftFieldsWithoutRecipients.sender.value),
                draftBody = draftFieldsWithoutRecipients.body.value,
                draftDisplayBodyUiModel = draftDisplayBody,
                focusTextBody = Effect.of(Unit)
            )
        )

        @Suppress("VariableMaxLength")
        private val LoadingToFieldsWhenReceivedDraftDataFromViaShare = TestTransition(
            name = "Should stop loading and set the received draft data as composer fields when draft data received, " +
                " via share",
            currentState = ComposerDraftState.initial().copy(isLoading = true),
            operation = ComposerEvent.PrefillDataReceivedViaShare(draftUiModel),
            expectedState = aNotSubmittableState(
                sender = SenderUiModel(draftFieldsWithoutRecipients.sender.value),
                draftBody = draftFieldsWithoutRecipients.body.value,
                draftDisplayBodyUiModel = draftDisplayBody,
                warning = Effect.empty()
            )
        )

        @Suppress("VariableMaxLength")
        private val LoadingToSendingNoticeWhenReceivedDraftDataWithInvalidSender = TestTransition(
            name = "Should stop loading and show the sending notice when prefilled address in invalid",
            currentState = ComposerDraftState.initial().copy(isLoading = true),
            operation = ComposerEvent.PrefillDraftDataReceived(
                draftUiModelWithoutRecipients,
                isDataRefreshed = true,
                isBlockedSendingFromPmAddress = true,
                isBlockedSendingFromDisabledAddress = false,
                bodyShouldTakeFocus = false
            ),
            expectedState = aNotSubmittableState(
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
            currentState = aNotSubmittableState(),
            operation = ComposerEvent.ReplaceDraftBody(draftFieldsWithoutRecipients.body),
            expectedState = aNotSubmittableState(
                replaceDraftBody = Effect.of(TextUiModel(draftFieldsWithoutRecipients.body.value))
            )
        )

        private val LoadingToErrorWhenErrorLoadingDraftData = TestTransition(
            name = "Should stop loading and display error when failing to receive draft data",
            currentState = ComposerDraftState.initial().copy(isLoading = true),
            operation = ComposerEvent.ErrorLoadingDraftData,
            expectedState = aNotSubmittableState(
                error = Effect.of(TextUiModel(R.string.composer_error_loading_draft)),
                isLoading = false
            )
        )

        private val EmptyToBottomSheetOpened = TestTransition(
            name = "Should open the file picker when add attachments action is chosen",
            currentState = ComposerDraftState.initial(),
            operation = ComposerAction.OnAddAttachments,
            expectedState = ComposerDraftState.initial().copy(
                openImagePicker = Effect.of(Unit)
            )
        )

        private val EmptyToAttachmentsUpdated = TestTransition(
            name = "Should emit attachments when they are updated",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.OnAttachmentsUpdated(
                listOf(
                    AttachmentMetadataWithState(AttachmentMetadataSamples.Invoice, AttachmentState.Uploaded)
                )
            ),
            expectedState = ComposerDraftState.initial().copy(
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

        private val EmptyToOnSendingError = TestTransition(
            name = "Should emit sending error",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.OnSendingError(TextUiModel.Text("SendingError")),
            expectedState = ComposerDraftState.initial().copy(
                sendingErrorEffect = Effect.of(TextUiModel.Text("SendingError"))
            )
        )

        private val EmptyToOnMessagePasswordUpdated = TestTransition(
            name = "Should update state with info whether a message password is set",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.OnMessagePasswordUpdated(
                MessagePassword(
                    UserIdTestData.userId,
                    messageId,
                    "password",
                    null
                )
            ),
            expectedState = ComposerDraftState.initial().copy(
                isMessagePasswordSet = true
            )
        )
        private val SubmittableToRequestConfirmEmptySubject = TestTransition(
            name = "Should update state to request confirmation for sending without subject",
            currentState = aSubmittableState(
                confirmSendingWithoutSubject = Effect.empty()
            ),
            operation = ComposerEvent.ConfirmEmptySubject,
            expectedState = aSubmittableState(
                confirmSendingWithoutSubject = Effect.of(Unit)
            )
        )

        private val SubmittableToConfirmEmptySubject = TestTransition(
            name = "Should update state to confirm sending without subject",
            currentState = aSubmittableState(
                confirmSendingWithoutSubject = Effect.of(Unit)
            ),
            operation = ComposerAction.ConfirmSendingWithoutSubject,
            expectedState = aSubmittableState(
                confirmSendingWithoutSubject = Effect.empty(),
                closeComposerWithMessageSending = Effect.of(Unit)
            )
        )

        private val SubmittableToRejectEmptySubject = TestTransition(
            name = "Should update state to reject sending without subject",
            currentState = aSubmittableState(
                confirmSendingWithoutSubject = Effect.of(Unit),
                changeFocusToField = Effect.empty(),
                showSendingLoading = true
            ),
            operation = ComposerAction.RejectSendingWithoutSubject,
            expectedState = aSubmittableState(
                confirmSendingWithoutSubject = Effect.empty(),
                changeFocusToField = Effect.of(FocusedFieldType.SUBJECT),
                showSendingLoading = false
            )
        )

        private val EmptyToSetExpirationTimeRequested = TestTransition(
            name = "Should update state to open expiration time bottom sheet",
            currentState = ComposerDraftState.initial(),
            operation = ComposerAction.OnSetExpirationTimeRequested,
            expectedState = ComposerDraftState.initial().copy(changeBottomSheetVisibility = Effect.of(true))
        )

        private val EmptyToExpirationTimeSet = TestTransition(
            name = "Should update state to open expiration time bottom sheet",
            currentState = ComposerDraftState.initial(),
            operation = ComposerAction.ExpirationTimeSet(duration = 1.days),
            expectedState = ComposerDraftState.initial().copy(changeBottomSheetVisibility = Effect.of(false))
        )

        private val EmptyToErrorSettingExpirationTime = TestTransition(
            name = "Should update state to an error when setting expiration time failed",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.ErrorSettingExpirationTime,
            expectedState = ComposerDraftState.initial().copy(
                error = Effect.of(TextUiModel(R.string.composer_error_setting_expiration_time))
            )
        )

        private val EmptyToMessageExpirationTimeUpdated = TestTransition(
            name = "Should update state with message expiration time",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.OnMessageExpirationTimeUpdated(
                MessageExpirationTime(UserIdTestData.userId, messageId, 1.days)
            ),
            expectedState = ComposerDraftState.initial().copy(messageExpiresIn = 1.days)
        )

        private val EmptyToConfirmSendExpiringMessage = TestTransition(
            name = "Should update state with an effect when sending an expiring message to external recipients",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.ConfirmSendExpiringMessageToExternalRecipients(
                listOf(RecipientSample.ExternalEncrypted)
            ),
            expectedState = ComposerDraftState.initial().copy(
                confirmSendExpiringMessage = Effect.of(listOf(RecipientSample.ExternalEncrypted))
            )
        )

        private val SubmittableToDiscardDraft = TestTransition(
            name = "Should update state with an effect to show confirmation dialog when discarding a draft",
            currentState = aSubmittableState(),
            operation = ComposerAction.DiscardDraft,
            expectedState = aSubmittableState().copy(confirmDiscardDraft = Effect.of(Unit))
        )

        private val SubmittableToDiscardDraftConfirmed = TestTransition(
            name = "Should update state with an effect to close composer when discarding a draft is confirmed",
            currentState = aSubmittableState(),
            operation = ComposerAction.DiscardDraftConfirmed,
            expectedState = aSubmittableState().copy(closeComposer = Effect.of(Unit))
        )

        private val EmptyToOnMessageSending = TestTransition(
            name = "Should show sending loader when sending",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.OnMessageSending,
            expectedState = ComposerDraftState.initial().copy(showSendingLoading = true)
        )

        private val SendingLoaderToSendingError = TestTransition(
            name = "Should dismiss send message loader when sending fails",
            currentState = aSubmittableState(showSendingLoading = true),
            operation = ComposerEvent.OnSendingError(TextUiModel.Text("SendingError")),
            expectedState = aSubmittableState().copy(
                sendingErrorEffect = Effect.of(TextUiModel.Text("SendingError")),
                showSendingLoading = false
            )
        )

        private val EmptyToAttachmentTooLarge = TestTransition(
            name = "Should emit attachment exceeded file limit",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.AddAttachmentError(AttachmentAddError.AttachmentTooLarge),
            expectedState = ComposerDraftState.initial().copy(
                attachmentsFileSizeExceeded = Effect.of(Unit)
            )
        )

        private val EmptyToAttachmentCountLimit = TestTransition(
            name = "Should emit attachment count limit reached",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.AddAttachmentError(AttachmentAddError.TooManyAttachments),
            expectedState = ComposerDraftState.initial().copy(
                error = Effect.of(TextUiModel(R.string.composer_too_many_attachments_error))
            )
        )

        private val EmptyToAttachmentUnexpectedError = TestTransition(
            name = "Should emit attachment unexpected error",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.AddAttachmentError(AttachmentAddError.InvalidDraftMessage),
            expectedState = ComposerDraftState.initial().copy(
                error = Effect.of(TextUiModel(R.string.composer_unexpected_attachments_error))
            )
        )

        private val EmptyToAttachmentEncryptionFailed = TestTransition(
            name = "Should emit attachment encryption error",
            currentState = ComposerDraftState.initial(),
            operation = ComposerEvent.AddAttachmentError(AttachmentAddError.EncryptionError),
            expectedState = ComposerDraftState.initial().copy(
                attachmentsEncryptionFailed = Effect.of(Unit)
            )
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
            SubmittableToDiscardDraft,
            SubmittableToDiscardDraftConfirmed,
            EmptyToOnMessageSending,
            SendingLoaderToSendingError,
            EmptyToAttachmentCountLimit,
            EmptyToAttachmentTooLarge,
            EmptyToAttachmentEncryptionFailed,
            EmptyToAttachmentUnexpectedError,
            DraftBodyChangedActionShouldDoNothing,
            LoadingDraftAndBodyShouldBeFocused
        )

        private fun aSubmittableState(
            sender: SenderUiModel = SenderUiModel(""),
            draftBody: String = "",
            draftDisplayBodyUiModel: DraftDisplayBodyUiModel = DraftDisplayBodyUiModel(""),
            showSendingLoading: Boolean = false,
            error: Effect<TextUiModel> = Effect.empty(),
            closeComposerWithMessageSending: Effect<Unit> = Effect.empty(),
            closeComposerWithMessageSendingOffline: Effect<Unit> = Effect.empty(),
            confirmSendingWithoutSubject: Effect<Unit> = Effect.empty(),
            changeFocusToField: Effect<FocusedFieldType> = Effect.empty(),
            attachmentsFileSizeExceeded: Effect<Unit> = Effect.empty(),
            attachmentEncryptionFailed: Effect<Unit> = Effect.empty(),
            warning: Effect<TextUiModel> = Effect.empty(),
            replaceDraftBody: Effect<TextUiModel> = Effect.empty()
        ) = ComposerDraftState(
            fields = ComposerFields(
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
            showSendingLoading = showSendingLoading,
            attachmentsFileSizeExceeded = attachmentsFileSizeExceeded,
            attachmentsEncryptionFailed = attachmentEncryptionFailed,
            replaceDraftBody = replaceDraftBody,
            warning = warning,
            isMessagePasswordSet = false,
            messageExpiresIn = Duration.ZERO,
            confirmSendExpiringMessage = Effect.empty(),
            openImagePicker = Effect.empty(),
            confirmDiscardDraft = Effect.empty()
        )

        private fun aNotSubmittableState(
            sender: SenderUiModel = SenderUiModel(""),
            error: Effect<TextUiModel> = Effect.empty(),
            premiumFeatureMessage: Effect<TextUiModel> = Effect.empty(),
            senderAddresses: List<SenderUiModel> = emptyList(),
            changeSenderBottomSheetVisibility: Effect<Boolean> = Effect.empty(),
            draftBody: String = "",
            draftDisplayBodyUiModel: DraftDisplayBodyUiModel = DraftDisplayBodyUiModel(""),
            closeComposer: Effect<Unit> = Effect.empty(),
            closeComposerWithDraftSaved: Effect<MessageId> = Effect.empty(),
            isLoading: Boolean = false,
            attachmentsFileSizeExceeded: Effect<Unit> = Effect.empty(),
            attachmentEncryptionFailed: Effect<Unit> = Effect.empty(),
            warning: Effect<TextUiModel> = Effect.empty(),
            replaceDraftBody: Effect<TextUiModel> = Effect.empty(),
            senderChangedNotice: Effect<TextUiModel> = Effect.empty(),
            focusTextBody: Effect<Unit> = Effect.empty()
        ) = ComposerDraftState(
            fields = ComposerFields(
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
            showSendingLoading = false,
            attachmentsFileSizeExceeded = attachmentsFileSizeExceeded,
            attachmentsEncryptionFailed = attachmentEncryptionFailed,
            replaceDraftBody = replaceDraftBody,
            warning = warning,
            isMessagePasswordSet = false,
            senderChangedNotice = senderChangedNotice,
            messageExpiresIn = Duration.ZERO,
            confirmSendExpiringMessage = Effect.empty(),
            openImagePicker = Effect.empty(),
            confirmDiscardDraft = Effect.empty(),
            focusTextBody = focusTextBody
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
