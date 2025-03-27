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

package ch.protonmail.android.mailcomposer.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserAddressSample
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.usecase.GetInitials
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.MessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.model.MessagePassword
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.domain.usecase.ClearMessageSendingError
import ch.protonmail.android.mailcomposer.domain.usecase.CreateDraftForAction
import ch.protonmail.android.mailcomposer.domain.usecase.CreateEmptyDraft
import ch.protonmail.android.mailcomposer.domain.usecase.DeleteAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.GetExternalRecipients
import ch.protonmail.android.mailcomposer.domain.usecase.IsValidEmailAddress
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessageAttachments
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessagePassword
import ch.protonmail.android.mailcomposer.domain.usecase.ObserveMessageSendingError
import ch.protonmail.android.mailcomposer.domain.usecase.OpenExistingDraft
import ch.protonmail.android.mailcomposer.domain.usecase.ProvideNewDraftId
import ch.protonmail.android.mailcomposer.domain.usecase.SaveMessageExpirationTime
import ch.protonmail.android.mailcomposer.domain.usecase.SendMessage
import ch.protonmail.android.mailcomposer.domain.usecase.StoreDraftWithBody
import ch.protonmail.android.mailcomposer.domain.usecase.StoreDraftWithSubject
import ch.protonmail.android.mailcomposer.domain.usecase.UpdateBccRecipients
import ch.protonmail.android.mailcomposer.domain.usecase.UpdateCcRecipients
import ch.protonmail.android.mailcomposer.domain.usecase.UpdateToRecipients
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.mapper.ContactSuggestionsMapper
import ch.protonmail.android.mailcomposer.presentation.mapper.ParticipantMapper
import ch.protonmail.android.mailcomposer.presentation.model.ComposerAction
import ch.protonmail.android.mailcomposer.presentation.model.ComposerDraftState
import ch.protonmail.android.mailcomposer.presentation.model.ComposerFields
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionsField
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.model.RecipientUiModel
import ch.protonmail.android.mailcomposer.presentation.model.SenderUiModel
import ch.protonmail.android.mailcomposer.presentation.reducer.ComposerReducer
import ch.protonmail.android.mailcomposer.presentation.ui.ComposerScreen
import ch.protonmail.android.mailcomposer.presentation.usecase.BuildDraftDisplayBody
import ch.protonmail.android.mailcomposer.presentation.usecase.FormatMessageSendingError
import ch.protonmail.android.mailcomposer.presentation.usecase.SortContactsForSuggestions
import ch.protonmail.android.mailcontact.domain.DeviceContactsSuggestionsPrompt
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.ContactSuggestionQuery
import ch.protonmail.android.mailcontact.domain.model.DeviceContact
import ch.protonmail.android.mailcontact.domain.usecase.GetContactSuggestions
import ch.protonmail.android.mailcontact.domain.usecase.GetContacts
import ch.protonmail.android.mailcontact.domain.usecase.SearchContacts
import ch.protonmail.android.mailcontact.domain.usecase.SearchDeviceContacts
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.DraftAction
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Recipient
import ch.protonmail.android.mailmessage.domain.model.SendingError
import ch.protonmail.android.mailmessage.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import ch.protonmail.android.mailmessage.domain.sample.RecipientSample
import ch.protonmail.android.mailmessage.presentation.mapper.AttachmentMetadataUiModelMapper
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentGroupUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.NO_ATTACHMENT_LIMIT
import ch.protonmail.android.mailmessage.presentation.sample.AttachmentMetadataUiModelSamples
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.idlingresources.ComposerIdlingResource
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.composer.DraftFieldsTestData
import ch.protonmail.android.testdata.contact.ContactEmailSample
import ch.protonmail.android.testdata.contact.ContactGroupIdSample
import ch.protonmail.android.testdata.contact.ContactSample
import ch.protonmail.android.testdata.contact.ContactTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.NetworkManager
import me.proton.core.util.kotlin.serialize
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class ComposerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @get:Rule
    val loggingTestRule = LoggingTestRule()

    private val storeDraftWithBodyMock = mockk<StoreDraftWithBody>()
    private val storeDraftWithSubjectMock = mockk<StoreDraftWithSubject> {
        coEvery { this@mockk.invoke(any()) } returns Unit.right()
    }
    private val updateToRecipients = mockk<UpdateToRecipients>()
    private val updateCcRecipients = mockk<UpdateCcRecipients>()
    private val updateBccRecipients = mockk<UpdateBccRecipients>()
    private val sendMessageMock = mockk<SendMessage>()
    private val networkManagerMock = mockk<NetworkManager>()
    private val getContactsMock = mockk<GetContacts>()
    private val searchContactsMock = mockk<SearchContacts>()
    private val searchDeviceContactsMock = mockk<SearchDeviceContacts>()
    private val deviceContactsSuggestionsPromptMock = mockk<DeviceContactsSuggestionsPrompt> {
        coEvery { this@mockk.getPromptEnabled() } returns true
        coEvery { this@mockk.setPromptDisabled() } just Runs
    }
    private val participantMapperMock = mockk<ParticipantMapper>()
    private val observePrimaryUserIdMock = mockk<ObservePrimaryUserId>()
    private val composerIdlingResource = spyk<ComposerIdlingResource>()
    private val isValidEmailAddressMock = mockk<IsValidEmailAddress>()
    private val provideNewDraftIdMock = mockk<ProvideNewDraftId>()
    private val savedStateHandle = mockk<SavedStateHandle>()
    private val deleteAttachment = mockk<DeleteAttachment>()
    private val observeMessageAttachments = mockk<ObserveMessageAttachments>()
    private val observeMessageSendingError = mockk<ObserveMessageSendingError>()
    private val clearMessageSendingError = mockk<ClearMessageSendingError>()
    private val formatMessageSendingError = mockk<FormatMessageSendingError>()
    private val observeMessagePassword = mockk<ObserveMessagePassword>()
    private val saveMessageExpirationTime = mockk<SaveMessageExpirationTime>()
    private val observeMessageExpirationTime = mockk<ObserveMessageExpirationTime>()
    private val getExternalRecipients = mockk<GetExternalRecipients>()
    private val createEmptyDraft = mockk<CreateEmptyDraft>()
    private val createDraftForAction = mockk<CreateDraftForAction>()
    private val openExistingDraft = mockk<OpenExistingDraft>()
    private val getContactSuggestions = mockk<GetContactSuggestions>()
    private val contactSuggestionsMapper = ContactSuggestionsMapper(GetInitials())

    private val getInitials = mockk<GetInitials> {
        every { this@mockk(any()) } returns BaseInitials
    }
    private val buildDraftDisplayBody = mockk<BuildDraftDisplayBody> {
        val bodySlot = slot<MessageBodyWithType>()
        every { this@mockk.invoke(capture(bodySlot)) } answers {
            DraftDisplayBodyUiModel("<html> ${bodySlot.captured.messageBody} </html>")
        }
    }
    private val attachmentUiModelMapper = AttachmentMetadataUiModelMapper()
    private val sortContactsForSuggestions = SortContactsForSuggestions(getInitials, testDispatcher)
    private val reducer = ComposerReducer(attachmentUiModelMapper)

    private val viewModel by lazy {
        ComposerViewModel(
            storeDraftWithBodyMock,
            storeDraftWithSubjectMock,
            updateToRecipients,
            updateCcRecipients,
            updateBccRecipients,
            getContactsMock,
            getContactSuggestions,
            searchContactsMock,
            searchDeviceContactsMock,
            deviceContactsSuggestionsPromptMock,
            sortContactsForSuggestions,
            participantMapperMock,
            contactSuggestionsMapper,
            reducer,
            isValidEmailAddressMock,
            composerIdlingResource,
            observeMessageAttachments,
            observeMessageSendingError,
            clearMessageSendingError,
            formatMessageSendingError,
            sendMessageMock,
            networkManagerMock,
            deleteAttachment,
            observeMessagePassword,
            saveMessageExpirationTime,
            observeMessageExpirationTime,
            getExternalRecipients,
            openExistingDraft,
            createEmptyDraft,
            createDraftForAction,
            buildDraftDisplayBody,
            savedStateHandle,
            observePrimaryUserIdMock,
            provideNewDraftIdMock
        )
    }

    @Test
    @Ignore("Missing rust implementation to store attachments")
    fun `should store attachments when attachments are added to the draft`() {
        // Given
        val uri = mockk<Uri>()
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val messageId = MessageIdSample.Invoice
        val expectedSubject = Subject("Subject for the message")
        val expectedSenderEmail = SenderEmail(UserAddressSample.PrimaryAddress.email)
        val expectedDraftBody = DraftBody("I am plaintext")
        val recipientsTo = RecipientsTo(listOf(RecipientSample.John))
        val recipientsCc = RecipientsCc(listOf(RecipientSample.John))
        val recipientsBcc = RecipientsBcc(listOf(RecipientSample.John))
        val expectedFields = DraftFields(
            expectedSenderEmail,
            expectedSubject,
            expectedDraftBody,
            recipientsTo,
            recipientsCc,
            recipientsBcc
        )
        expectInputDraftMessageId { messageId }
        expectInitComposerWithExistingDraftSuccess(expectedUserId, messageId) { expectedFields }
        expectObservedMessageAttachments(expectedUserId, messageId)
        expectNoInputDraftAction()
        expectObserveMessageSendingError(expectedUserId, messageId)
        expectMessagePassword(expectedUserId, messageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, messageId)

        // When
        viewModel.submit(ComposerAction.AttachmentsAdded(listOf(uri)))

        // Then
        // ???
    }

    @Test
    fun `should store the draft body when the body changes`() {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedDraftBody = DraftBody(RawDraftBody)
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.DraftBodyChanged(expectedDraftBody)
        expectStoreDraftBodySucceeds(expectedDraftBody)
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        coVerify {
            storeDraftWithBodyMock(expectedDraftBody)
        }
    }

    @Test
    @Ignore("Need to define how rust API will look like")
    fun `should emit Effect for ReplaceDraftBody when sender changes`() = runTest {
        // Given
        val expectedDraftBody = DraftBody(RawDraftBody)
        val expectedSenderEmail = SenderEmail(UserAddressSample.AliasAddress.email)
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.SenderChanged(SenderUiModel(expectedSenderEmail.value))
        expectStoreDraftBodySucceeds(expectedDraftBody)
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        val expectedReplaceDraftBodyTextUiModel = TextUiModel(expectDraftBodyWithSignature().value)

        // When
        viewModel.submit(action)

        // Then
        assertEquals(
            expectedReplaceDraftBodyTextUiModel,
            viewModel.state.value.replaceDraftBody.consume()
        )
    }

    @Test
    @Ignore("Rust lib to expose change sender API")
    fun `should store draft with sender when sender changes`() = runTest {
        // Given
        val expectedDraftBody = DraftBody(RawDraftBody)
        val expectedSenderEmail = SenderEmail(UserAddressSample.AliasAddress.email)
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.SenderChanged(SenderUiModel(expectedSenderEmail.value))
        expectStoreDraftBodySucceeds(expectedDraftBody)
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        coVerify {
            storeDraftWithBodyMock(expectedDraftBody)
        }
    }

    @Test
    fun `should store draft subject when subject changes`() = runTest {
        // Given
        val expectedSubject = Subject("Subject for the message")
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.SubjectChanged(expectedSubject)
        expectStoreDraftSubjectSucceeds(expectedSubject)
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        coVerify {
            storeDraftWithSubjectMock(
                expectedSubject
            )
        }
    }

    @Test
    fun `should store draft recipients TO when they change`() = runTest {
        // Given
        val expectedRecipients = listOf(
            Recipient("valid@email.com", "Valid Email", false)
        )
        val recipientsUiModels = listOf(
            RecipientUiModel.Valid("valid@email.com"),
            RecipientUiModel.Invalid("invalid email")
        )
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.RecipientsToChanged(recipientsUiModels)
        expectUpdateToRecipientsSucceeds(
            expectedRecipients
        )
        mockParticipantMapper()
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        coVerify {
            updateToRecipients(
                emptyList(),
                expectedRecipients
            )
        }
    }

    @Test
    fun `should store draft recipients CC when they change`() = runTest {
        // Given
        val expectedRecipients = listOf(
            Recipient("valid@email.com", "Valid Email", false)
        )
        val recipientsUiModels = listOf(
            RecipientUiModel.Valid("valid@email.com"),
            RecipientUiModel.Invalid("invalid email")
        )
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.RecipientsCcChanged(recipientsUiModels)
        expectUpdateCcRecipientsSucceeds(
            expectedRecipients
        )
        mockParticipantMapper()
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        coVerify {
            updateCcRecipients(
                emptyList(),
                expectedRecipients
            )
        }
    }

    @Test
    fun `should store draft recipients BCC when they change`() = runTest {
        // Given
        val expectedRecipients = listOf(
            Recipient("valid@email.com", "Valid Email", false)
        )
        val recipientsUiModels = listOf(
            RecipientUiModel.Valid("valid@email.com"),
            RecipientUiModel.Invalid("invalid email")
        )
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.RecipientsBccChanged(recipientsUiModels)
        expectUpdateBccRecipientsSucceeds(
            expectedRecipients
        )
        mockParticipantMapper()
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        coVerify {
            updateBccRecipients(
                emptyList(),
                expectedRecipients
            )
        }
    }

    @Test
    fun `should perform search when ContactSuggestionTermChanged`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedSearchTerm = "proton"
        val suggestionField = ContactSuggestionsField.BCC
        val expectedContacts = listOf(ContactSample.Doe, ContactSample.John)
        val expectedDeviceContacts = emptyList<DeviceContact>()
        val expectedContactGroups = emptyList<ContactMetadata.ContactGroup>()
        val action = ComposerAction.ContactSuggestionTermChanged(expectedSearchTerm, suggestionField)

        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectGetContactSuggestions(expectedUserId, expectedSearchTerm, expectedContacts, expectedContactGroups)
        expectSearchDeviceContacts(expectedSearchTerm, expectedDeviceContacts)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        coVerify { getContactSuggestions(expectedUserId, ContactSuggestionQuery(expectedSearchTerm)) }
    }

    @Test
    fun `should emit ContactSuggestionsDismissed when searchTerm is blank`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedSearchTerm = ""
        val suggestionField = ContactSuggestionsField.BCC

        val expectedContacts = emptyList<ContactMetadata.Contact>()

        val expectedDeviceContacts = emptyList<DeviceContact>()

        val expectedContactGroups = emptyList<ContactMetadata.ContactGroup>()
        val action = ComposerAction.ContactSuggestionTermChanged(expectedSearchTerm, suggestionField)

        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectGetContactSuggestions(expectedUserId, expectedSearchTerm, expectedContacts, expectedContactGroups)
        expectSearchDeviceContacts(expectedSearchTerm, expectedDeviceContacts)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)
        val actual = viewModel.state.value

        // Then
        assertEquals(
            emptyMap(),
            actual.contactSuggestions
        )
        assertEquals(mapOf(ContactSuggestionsField.BCC to false), actual.areContactSuggestionsExpanded)
    }

    @Test
    fun `should call DeviceContactsSuggestionsPrompt when DeviceContactsPromptDenied is emitted`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedSearchTerm = ""

        val expectedContacts = emptyList<ContactMetadata.Contact>()

        val expectedDeviceContacts = emptyList<DeviceContact>()

        val expectedContactGroups = emptyList<ContactMetadata.ContactGroup>()
        val action = ComposerAction.DeviceContactsPromptDenied

        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectGetContactSuggestions(expectedUserId, expectedSearchTerm, expectedContacts, expectedContactGroups)
        expectSearchDeviceContacts(expectedSearchTerm, expectedDeviceContacts)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        viewModel.state.test {
            awaitItem()

            coVerify { deviceContactsSuggestionsPromptMock.setPromptDisabled() }
        }
    }

    @Test
    fun `should emit UpdateContactSuggestions when contact suggestions are found`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedSearchTerm = "contact"
        val suggestionField = ContactSuggestionsField.BCC

        val expectedContacts = listOf(
            ContactSample.Doe.copy(
                emails = listOf(
                    ContactTestData.buildContactEmailWith(
                        address = "address1@proton.ch"
                    )
                )
            ),
            ContactSample.John.copy(
                emails = listOf(
                    ContactTestData.buildContactEmailWith(
                        address = "address2@proton.ch"
                    )
                )
            )
        )

        val expectedDeviceContacts = emptyList<DeviceContact>()

        val expectedContactGroups = listOf(
            ContactMetadata.ContactGroup(
                ContactGroupIdSample.Work,
                "Coworkers contact group",
                "#AABBCC",
                listOf(
                    ContactSample.Doe.copy(
                        emails = listOf(ContactEmailSample.contactEmail1)
                    )
                )
            )
        )
        val action = ComposerAction.ContactSuggestionTermChanged(expectedSearchTerm, suggestionField)

        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectGetContactSuggestions(expectedUserId, expectedSearchTerm, expectedContacts, expectedContactGroups)
        expectSearchDeviceContacts(expectedSearchTerm, expectedDeviceContacts)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)
        val actual = viewModel.state.value

        // Then
        assertEquals(
            mapOf(
                ContactSuggestionsField.BCC to listOf(
                    ContactSuggestionUiModel.Contact(
                        expectedContacts[0].name,
                        initial = expectedContacts[0].name[0].toString(),
                        expectedContacts[0].emails.first().email
                    ),
                    ContactSuggestionUiModel.Contact(
                        expectedContacts[1].name,
                        initial = expectedContacts[1].name[0].toString(),
                        expectedContacts[1].emails.first().email
                    ),
                    ContactSuggestionUiModel.ContactGroup(
                        expectedContactGroups[0].name,
                        expectedContactGroups[0].members.map { it.emails.first().email }
                    )
                )
            ),
            actual.contactSuggestions
        )
        assertEquals(mapOf(ContactSuggestionsField.BCC to true), actual.areContactSuggestionsExpanded)
    }

    @Test
    fun `should emit UpdateContactSuggestions limiting results according to constant max value`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedSearchTerm = "contact"
        val suggestionField = ContactSuggestionsField.BCC

        val expectedContactsExceedingLimit = (1..ComposerViewModel.maxContactAutocompletionCount + 1).map {
            ContactSample.John.copy(
                emails = listOf(
                    ContactTestData.buildContactEmailWith(
                        address = "address$it@proton.ch"
                    )
                )
            )
        }
        val expectedDeviceContacts = emptyList<DeviceContact>()
        val expectedContactGroups = emptyList<ContactMetadata.ContactGroup>()
        val action = ComposerAction.ContactSuggestionTermChanged(expectedSearchTerm, suggestionField)

        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectGetContactSuggestions(
            expectedUserId, expectedSearchTerm, expectedContactsExceedingLimit, expectedContactGroups
        )
        expectSearchDeviceContacts(expectedSearchTerm, expectedDeviceContacts)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)
        advanceUntilIdle()
        val actual = viewModel.state.value

        // Then
        assertEquals(
            ComposerViewModel.Companion.maxContactAutocompletionCount,
            actual.contactSuggestions[ContactSuggestionsField.BCC]!!.size
        )
    }

    @Test
    fun `should dismiss contact suggestions when ContactSuggestionsDismissed is emitted`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val suggestionField = ContactSuggestionsField.BCC

        val action = ComposerAction.ContactSuggestionsDismissed(suggestionField)

        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)
        val actual = viewModel.state.value

        // Then
        assertEquals(mapOf(ContactSuggestionsField.BCC to false), actual.areContactSuggestionsExpanded)
    }

    @Test
    fun `should close composer with draft save when composer is closed while draft was non empty`() = runTest {
        // Given
        val expectedSubject = Subject("Subject for the message")
        val expectedSenderEmail = SenderEmail(UserAddressSample.PrimaryAddress.email)
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftBody = DraftBody("I am plaintext")
        val recipientsTo = RecipientsTo(listOf(RecipientSample.John))
        val recipientsCc = RecipientsCc(listOf(RecipientSample.John))
        val recipientsBcc = RecipientsBcc(listOf(RecipientSample.John))
        mockParticipantMapper()
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId) {
            DraftFields(
                sender = expectedSenderEmail,
                subject = expectedSubject,
                body = expectedDraftBody,
                recipientsTo = recipientsTo,
                recipientsCc = recipientsCc,
                recipientsBcc = recipientsBcc
            )
        }

        // When
        viewModel.submit(ComposerAction.OnCloseComposer)

        // Then
        assertEquals(Effect.of(Unit), viewModel.state.value.closeComposerWithDraftSaved)
    }

    @Test
    fun `should send message when send button is clicked`() = runTest {
        // Given
        val expectedSubject = Subject("Subject for the message")
        val expectedSenderEmail = SenderEmail(UserAddressSample.PrimaryAddress.email)
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftBody = DraftBody("I am plaintext")
        val recipientsTo = RecipientsTo(listOf(RecipientSample.John))
        val recipientsCc = RecipientsCc(listOf(RecipientSample.John))
        val recipientsBcc = RecipientsBcc(listOf(RecipientSample.John))
        mockParticipantMapper()
        expectNetworkManagerIsConnected()
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectSendMessageSucceeds(expectedUserId)
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectNoExternalRecipients(expectedUserId, recipientsTo, recipientsCc, recipientsBcc)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId) {
            DraftFields(
                sender = expectedSenderEmail,
                subject = expectedSubject,
                body = expectedDraftBody,
                recipientsTo = recipientsTo,
                recipientsCc = recipientsCc,
                recipientsBcc = recipientsBcc
            )
        }

        // When
        viewModel.submit(ComposerAction.OnSendMessage)

        // Then
        coVerifyOrder {
            sendMessageMock(expectedUserId)
        }
        assertEquals(Effect.of(Unit), viewModel.state.value.closeComposerWithMessageSending)
    }

    @Test
    fun `should send message in offline when send button is clicked while offline`() = runTest {
        // Given
        val expectedSubject = Subject("Subject for the message")
        val expectedSenderEmail = SenderEmail(UserAddressSample.PrimaryAddress.email)
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftBody = DraftBody("I am plaintext")
        val recipientsTo = RecipientsTo(listOf(RecipientSample.John))
        val recipientsCc = RecipientsCc(listOf(RecipientSample.John))
        val recipientsBcc = RecipientsBcc(listOf(RecipientSample.John))
        mockParticipantMapper()
        expectNetworkManagerIsDisconnected()
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectNoExternalRecipients(expectedUserId, recipientsTo, recipientsCc, recipientsBcc)
        expectSendMessageSucceeds(expectedUserId)
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId) {
            DraftFields(
                sender = expectedSenderEmail,
                subject = expectedSubject,
                body = expectedDraftBody,
                recipientsTo = recipientsTo,
                recipientsCc = recipientsCc,
                recipientsBcc = recipientsBcc
            )
        }

        // When
        viewModel.submit(ComposerAction.OnSendMessage)

        // Then
        coVerifyOrder {
            sendMessageMock(expectedUserId)
        }
        assertEquals(Effect.of(Unit), viewModel.state.value.closeComposerWithMessageSendingOffline)
    }

    @Test
    fun `should close composer without saving draft when fields are empty and composer is closed`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectContacts()
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(ComposerAction.OnCloseComposer)

        // Then
        assertEquals(Effect.of(Unit), viewModel.state.value.closeComposer)
    }

    @Test
    fun `emits state with primary sender address when available`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        val expectedDraftFields = expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        val actual = viewModel.state.value

        // Then
        assertEquals(SenderUiModel(expectedDraftFields.sender.value), actual.fields.sender)
    }

    @Test
    fun `emits state with initialization error when creating new empty draft fails`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftFails(expectedUserId) { DataError.Local.NoDataCached }

        // When
        val actual = viewModel.state.value

        // Then
        assertEquals(TextUiModel(R.string.composer_error_invalid_sender), actual.error.consume())
    }

    @Test
    @Ignore("To be re-enabled when adding back change sender feature")
    fun `emits state with user addresses when sender can be changed`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val addresses = listOf(UserAddressSample.PrimaryAddress, UserAddressSample.AliasAddress)
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(ComposerAction.ChangeSenderRequested)

        // Then
        val currentState = viewModel.state.value
        val expected = addresses.map { SenderUiModel(it.email) }
        assertEquals(expected, currentState.senderAddresses)
    }

    @Test
    @Ignore("To be re-enabled when adding back change sender feature")
    fun `emits state with upgrade plan to change sender when user cannot change sender`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(ComposerAction.ChangeSenderRequested)

        // Then
        val currentState = viewModel.state.value
        val expected = TextUiModel(R.string.composer_change_sender_paid_feature)
        assertEquals(expected, currentState.premiumFeatureMessage.consume())
    }

    @Test
    @Ignore("To be re-enabled when adding back change sender feature")
    fun `emits state with error when cannot determine if user can change sender`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(ComposerAction.ChangeSenderRequested)

        // Then
        val currentState = viewModel.state.value
        val expected = TextUiModel(R.string.composer_error_change_sender_failed_getting_subscription)
        assertEquals(expected, currentState.error.consume())
    }

    @Test
    @Ignore("To be re-enabled when adding back change sender feature")
    fun `emits state with new sender address when sender changed`() = runTest {
        // Given
        val expectedDraftBody = DraftBody("")
        val expectedSenderEmail = SenderEmail(UserAddressSample.AliasAddress.email)
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.SenderChanged(SenderUiModel(expectedSenderEmail.value))
        expectStoreDraftBodySucceeds(expectedDraftBody)
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        val currentState = viewModel.state.value
        assertEquals(SenderUiModel(expectedSenderEmail.value), currentState.fields.sender)
    }

    @Test
    @Ignore("Rust lib to expose change sender API")
    fun `emits state with saving draft with new sender error when save draft with sender returns error`() = runTest {
        // Given
        val expectedDraftBody = DraftBody("")
        val expectedSenderEmail = SenderEmail(UserAddressSample.AliasAddress.email)
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.SenderChanged(SenderUiModel(expectedSenderEmail.value))
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectStoreDraftBodyFails(expectedDraftBody) {
            DataError.Local.SaveDraftError.SaveFailed
        }
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        val currentState = viewModel.state.value
        assertEquals(TextUiModel(R.string.composer_error_store_draft_sender_address), currentState.error.consume())
        loggingTestRule.assertErrorLogged(
            "Store draft $expectedMessageId with new sender ${expectedSenderEmail.value} failed"
        )
    }

    @Test
    fun `emits state with saving draft body error when save draft body returns error`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftBody = DraftBody("updated-draft")
        val action = ComposerAction.DraftBodyChanged(expectedDraftBody)
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectStoreDraftBodyFails(expectedDraftBody) {
            DataError.Local.SaveDraftError.SaveFailed
        }
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        val currentState = viewModel.state.value
        assertEquals(TextUiModel(R.string.composer_error_store_draft_body), currentState.error.consume())
    }

    @Test
    fun `emits state with saving draft subject error when save draft subject returns error`() = runTest {
        // Given
        val expectedSubject = Subject("Subject for the message")
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val action = ComposerAction.SubjectChanged(expectedSubject)
        expectStoreDraftSubjectFails(expectedSubject) {
            DataError.Local.SaveDraftError.Unknown
        }
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        val currentState = viewModel.state.value
        assertEquals(TextUiModel(R.string.composer_error_store_draft_subject), currentState.error.consume())
        loggingTestRule.assertErrorLogged(
            "Store draft $expectedMessageId with new subject $expectedSubject failed"
        )
    }

    @Test
    fun `emits state with saving draft recipients error when save draft TO returns error`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedRecipients = listOf(
            Recipient("valid@email.com", "Valid Email", false)
        )
        val recipientsUiModels = listOf(
            RecipientUiModel.Valid("valid@email.com"),
            RecipientUiModel.Invalid("invalid email")
        )
        val action = ComposerAction.RecipientsToChanged(recipientsUiModels)
        expectUpdateDraftToRecipientsFails(expectedRecipients) {
            DataError.Local.SaveDraftError.DuplicateRecipient
        }
        mockParticipantMapper()
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        val currentState = viewModel.state.value
        assertEquals(TextUiModel(R.string.composer_error_store_draft_recipients), currentState.error.consume())
    }

    @Test
    fun `emits state with saving draft recipient error when save draft CC returns error`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedRecipients = listOf(
            Recipient("valid@email.com", "Valid Email", false)
        )
        val recipientsUiModels = listOf(
            RecipientUiModel.Valid("valid@email.com"),
            RecipientUiModel.Invalid("invalid email")
        )
        val action = ComposerAction.RecipientsCcChanged(recipientsUiModels)
        expectUpdateDraftCcRecipientsFails(expectedRecipients) {
            DataError.Local.SaveDraftError.DuplicateRecipient
        }
        mockParticipantMapper()
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        val currentState = viewModel.state.value
        assertEquals(TextUiModel(R.string.composer_error_store_draft_recipients), currentState.error.consume())
    }

    @Test
    fun `emits state with saving draft recipients error when save draft BCC returns error`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedRecipients = listOf(
            Recipient("valid@email.com", "Valid Email", false)
        )
        val recipientsUiModels = listOf(
            RecipientUiModel.Valid("valid@email.com"),
            RecipientUiModel.Invalid("invalid email")
        )
        val action = ComposerAction.RecipientsBccChanged(recipientsUiModels)
        expectUpdateDraftBccRecipientsFails(expectedRecipients) {
            DataError.Local.SaveDraftError.DuplicateRecipient
        }
        mockParticipantMapper()
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId)

        // When
        viewModel.submit(action)

        // Then
        val currentState = viewModel.state.value
        assertEquals(TextUiModel(R.string.composer_error_store_draft_recipients), currentState.error.consume())
    }

    @Test
    fun `emits state with loading draft content when draftId was given as input`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftId = expectInputDraftMessageId { MessageIdSample.RemoteDraft }
        val draftFields = existingDraftFields
        // Simulate a small delay in getDecryptedDraftFields to ensure the "loading" state was emitted
        expectInitComposerWithExistingDraftSuccess(expectedUserId, expectedDraftId, 100) { draftFields }
        expectObservedMessageAttachments(expectedUserId, expectedDraftId)
        expectNoInputDraftAction()
        expectObserveMessageSendingError(expectedUserId, expectedDraftId)
        expectMessagePassword(expectedUserId, expectedDraftId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedDraftId)

        // When
        val actual = viewModel.state.value

        // Then
        assertTrue(actual.isLoading)
        coVerify { openExistingDraft(expectedUserId, expectedDraftId) }
    }

    @Test
    fun `emits state with remote draft fields to be prefilled when getting decrypted draft fields succeeds`() =
        runTest {
            // Given
            val expectedUserId = expectedUserId { UserIdSample.Primary }
            val expectedDraftId = expectInputDraftMessageId { MessageIdSample.RemoteDraft }
            val expectedDraftFields = existingDraftFields
            val expectedDisplayBody = DraftDisplayBodyUiModel("<html> ${expectedDraftFields.body.value} </html>")
            expectInitComposerWithExistingDraftSuccess(expectedUserId, expectedDraftId) { existingDraftFields }
            expectObservedMessageAttachments(expectedUserId, expectedDraftId)
            expectNoInputDraftAction()
            expectObserveMessageSendingError(expectedUserId, expectedDraftId)
            expectMessagePassword(expectedUserId, expectedDraftId)
            expectNoFileShareVia()
            expectObserveMessageExpirationTime(expectedUserId, expectedDraftId)

            // When
            val actual = viewModel.state.value

            // Then
            val expectedComposerFields = ComposerFields(
                expectedDraftId,
                SenderUiModel(expectedDraftFields.sender.value),
                expectedDraftFields.recipientsTo.value.map { RecipientUiModel.Valid(it.address) },
                emptyList(),
                emptyList(),
                expectedDraftFields.subject.value,
                expectedDisplayBody,
                expectedDraftFields.body.value
            )
            assertEquals(expectedComposerFields, actual.fields)
        }

    @Test
    fun `emits state with local draft fields to be prefilled when getting decrypted draft fields succeeds`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftId = expectInputDraftMessageId { MessageIdSample.RemoteDraft }
        val expectedDraftFields = existingDraftFields
        val expectedDisplayBody = DraftDisplayBodyUiModel("<html> ${expectedDraftFields.body.value} </html>")
        expectInitComposerWithExistingDraftSuccess(expectedUserId, expectedDraftId) { existingDraftFields }
        expectObservedMessageAttachments(expectedUserId, expectedDraftId)
        expectInputDraftAction { DraftAction.Compose }
        expectObserveMessageSendingError(expectedUserId, expectedDraftId)
        expectMessagePassword(expectedUserId, expectedDraftId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedDraftId)

        // When
        val actual = viewModel.state.value

        // Then
        val expectedComposerFields = ComposerFields(
            expectedDraftId,
            SenderUiModel(expectedDraftFields.sender.value),
            expectedDraftFields.recipientsTo.value.map { RecipientUiModel.Valid(it.address) },
            emptyList(),
            emptyList(),
            expectedDraftFields.subject.value,
            expectedDisplayBody,
            expectedDraftFields.body.value
        )
        assertEquals(expectedComposerFields, actual.fields)
        expectStoreDraftSubjectSucceeds(expectedDraftFields.subject)
    }

    @Test
    fun `emits state with composer fields to be prefilled when getting parent message draft fields succeeds`() =
        runTest {
            // Given
            val expectedUserId = expectedUserId { UserIdSample.Primary }
            val expectedDraftId = expectedMessageId { MessageIdSample.EmptyDraft }
            val expectedParentId = MessageIdSample.Invoice
            val expectedAction = expectInputDraftAction { DraftAction.Reply(expectedParentId) }
            expectNoInputDraftMessageId()
            val expectedDraftFields = expectInitComposerForActionSuccess(
                expectedUserId, expectedAction
            ) { draftFieldsWithQuotedBody }
            val expectedDisplayBody = DraftDisplayBodyUiModel("<html> ${expectedDraftFields.body.value} </html>")
            expectObservedMessageAttachments(expectedUserId, expectedDraftId)
            expectObserveMessageSendingError(expectedUserId, expectedDraftId)
            expectMessagePassword(expectedUserId, expectedDraftId)
            expectNoFileShareVia()
            expectObserveMessageExpirationTime(expectedUserId, expectedDraftId)

            // When
            val actual = viewModel.state.value

            // Then
            val expectedComposerFields = ComposerFields(
                expectedDraftId,
                SenderUiModel(expectedDraftFields.sender.value),
                expectedDraftFields.recipientsTo.value.map { RecipientUiModel.Valid(it.address) },
                emptyList(),
                emptyList(),
                expectedDraftFields.subject.value,
                expectedDisplayBody,
                expectedDraftFields.body.value
            )
            assertEquals(expectedComposerFields, actual.fields)
        }

    @Test
    @Ignore("TBD how rust lib will expose information of sender address being changed")
    fun `emits state with valid sender and notice effect when parent draft sender is invalid`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedParentId = MessageIdSample.Invoice
        val expectedAction = expectInputDraftAction { DraftAction.Reply(expectedParentId) }
        expectNoInputDraftMessageId()
        val expectedValidEmail = SenderEmail("valid-to-use-instead@proton.me")
        expectInitComposerForActionSuccess(
            expectedUserId, expectedAction
        ) { draftFieldsWithQuotedBody }
        expectObservedMessageAttachments(expectedUserId, expectedDraftId)
        expectObserveMessageSendingError(expectedUserId, expectedDraftId)
        expectMessagePassword(expectedUserId, expectedDraftId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedDraftId)

        // When
        val actual = viewModel.state.value

        // Then
        assertEquals(SenderUiModel(expectedValidEmail.value), actual.fields.sender)
        assertEquals(
            Effect.of(TextUiModel(R.string.composer_sender_changed_pm_address_is_a_paid_feature)),
            actual.senderChangedNotice
        )
    }

    @Test
    fun `emits state with error loading existing draft when getting decrypted draft fields fails`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftId = expectInputDraftMessageId { MessageIdSample.RemoteDraft }
        expectInitComposerWithExistingDraftError(expectedUserId, expectedDraftId) { DataError.Local.NoDataCached }
        expectObservedMessageAttachments(expectedUserId, expectedDraftId)
        expectNoInputDraftAction()
        expectObserveMessageSendingError(expectedUserId, expectedDraftId)
        expectMessagePassword(expectedUserId, expectedDraftId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedDraftId)

        // When
        val actual = viewModel.state.value

        // Then
        assertEquals(TextUiModel(R.string.composer_error_loading_draft), actual.error.consume())
    }

    @Test
    @Ignore("To be re-enabled when adding back attachments feature")
    fun `emits state with an effect to open the file picker when add attachments action is submitted`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftId = expectInputDraftMessageId { MessageIdSample.RemoteDraft }
        expectInitComposerWithExistingDraftSuccess(expectedUserId, expectedDraftId) { existingDraftFields }
        expectNoInputDraftAction()
        expectObservedMessageAttachments(expectedUserId, expectedDraftId)
        expectObserveMessageSendingError(expectedUserId, expectedDraftId)
        expectMessagePassword(expectedUserId, expectedDraftId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedDraftId)

        // When
        viewModel.submit(ComposerAction.OnAddAttachments)

        // Then
        val actual = viewModel.state.value
        assertEquals(Unit, actual.openImagePicker.consume())
    }

    @Test
    fun `emits state with updated attachments when the attachments change`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftId = expectInputDraftMessageId { MessageIdSample.Invoice }
        val expectedSubject = Subject("Subject for the message")
        val expectedSenderEmail = SenderEmail(UserAddressSample.PrimaryAddress.email)
        val expectedDraftBody = DraftBody("I am plaintext")
        val recipientsTo = RecipientsTo(listOf(RecipientSample.John))
        val recipientsCc = RecipientsCc(listOf(RecipientSample.John))
        val recipientsBcc = RecipientsBcc(listOf(RecipientSample.John))
        val expectedFields = DraftFields(
            expectedSenderEmail,
            expectedSubject,
            expectedDraftBody,
            recipientsTo,
            recipientsCc,
            recipientsBcc
        )
        expectNoInputDraftAction()
        expectInitComposerWithExistingDraftSuccess(expectedUserId, expectedDraftId) { expectedFields }
        expectObservedMessageAttachments(expectedUserId, expectedDraftId)
        expectObserveMessageSendingError(expectedUserId, expectedDraftId)
        expectMessagePassword(expectedUserId, expectedDraftId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedDraftId)

        // When
        viewModel.state.test {

            // Then
            val expected = AttachmentGroupUiModel(
                limit = NO_ATTACHMENT_LIMIT,
                attachments = listOf(AttachmentMetadataUiModelSamples.DeletableInvoice)
            )
            val actual = awaitItem().attachments
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `delete compose action triggers delete attachment use case`() = runTest {
        // Given
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val messageId = MessageIdSample.Invoice
        val expectedSubject = Subject("Subject for the message")
        val expectedSenderEmail = SenderEmail(UserAddressSample.PrimaryAddress.email)
        val expectedDraftBody = DraftBody("I am plaintext")
        val expectedAttachmentId = AttachmentId("attachment_id")
        val recipientsTo = RecipientsTo(listOf(RecipientSample.John))
        val recipientsCc = RecipientsCc(listOf(RecipientSample.John))
        val recipientsBcc = RecipientsBcc(listOf(RecipientSample.John))
        val expectedFields = DraftFields(
            expectedSenderEmail,
            expectedSubject,
            expectedDraftBody,
            recipientsTo,
            recipientsCc,
            recipientsBcc
        )
        expectInputDraftMessageId { messageId }
        expectInitComposerWithExistingDraftSuccess(expectedUserId, messageId) { expectedFields }
        expectObservedMessageAttachments(expectedUserId, messageId)
        expectNoInputDraftAction()
        expectAttachmentDeleteSucceeds(expectedUserId, messageId, expectedAttachmentId)
        expectObserveMessageSendingError(expectedUserId, messageId)
        expectMessagePassword(expectedUserId, messageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, messageId)

        // When
        viewModel.submit(ComposerAction.RemoveAttachment(expectedAttachmentId))

        // Then
        coVerify { deleteAttachment(expectedUserId, messageId, expectedAttachmentId) }
    }

    @Test
    @Ignore("To be re-enabled when adding back attachments feature")
    fun `emit state with effect when attachment file size exceeded`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val messageId = MessageIdSample.Invoice
        val expectedSubject = Subject("Subject for the message")
        val expectedSenderEmail = SenderEmail(UserAddressSample.PrimaryAddress.email)
        val expectedDraftBody = DraftBody("I am plaintext")
        val recipientsTo = RecipientsTo(listOf(RecipientSample.John))
        val recipientsCc = RecipientsCc(listOf(RecipientSample.John))
        val recipientsBcc = RecipientsBcc(listOf(RecipientSample.John))
        val expectedFields = DraftFields(
            expectedSenderEmail,
            expectedSubject,
            expectedDraftBody,
            recipientsTo,
            recipientsCc,
            recipientsBcc
        )
        expectInputDraftMessageId { messageId }
        expectInitComposerWithExistingDraftSuccess(expectedUserId, messageId) { expectedFields }
        expectObservedMessageAttachments(expectedUserId, messageId)
        expectNoInputDraftAction()
        expectObserveMessageSendingError(expectedUserId, messageId)
        expectMessagePassword(expectedUserId, messageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, messageId)

        // When
        viewModel.submit(ComposerAction.AttachmentsAdded(listOf(uri)))

        // Then
        viewModel.state.test {
            val expected = Effect.of(Unit)
            val actual = awaitItem().attachmentsFileSizeExceeded
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `should update state with message password info when message password changes`() = runTest {
        // Given
        val userId = expectedUserId { UserIdSample.Primary }
        val messageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(userId, messageId)
        expectObserveMessageSendingError(userId, messageId)
        expectMessagePassword(userId, messageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(userId, messageId)
        expectInitComposerWithNewEmptyDraftSucceeds(userId)

        // When
        viewModel.state.test {
            // Then
            assertTrue(awaitItem().isMessagePasswordSet)
        }
    }

    @Test
    fun `should set recipient to state when recipient was given as an input`() = runTest {
        // Given
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedRecipient = RecipientSample.NamelessRecipient
        val expectedAction = DraftAction.ComposeToAddresses(listOf(expectedRecipient.address))

        expectNoInputDraftMessageId()
        expectedMessageId { expectedMessageId }
        expectContacts()
        mockParticipantMapper()
        expectInputDraftAction { expectedAction }
        expectUpdateToRecipientsSucceeds(
            listOf(expectedRecipient)
        )
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectAddressValidation(expectedRecipient.address, true)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId) {
            DraftFieldsTestData.EmptyDraftWithPrimarySender
        }

        assertEquals(viewModel.state.value.fields.to.first(), RecipientUiModel.Valid(expectedRecipient.address))
    }

    @Test
    @Ignore("To be re-enabled when adding back expiration time feature")
    fun `should emit state for showing bottom sheet when action for setting expiration time is submitted`() = runTest {
        // Given
        val userId = expectedUserId { UserIdSample.Primary }
        val messageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(userId, messageId)
        expectObserveMessageSendingError(userId, messageId)
        expectMessagePassword(userId, messageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(userId, messageId)
        expectInitComposerWithNewEmptyDraftSucceeds(userId)

        // When
        viewModel.submit(ComposerAction.OnSetExpirationTimeRequested)

        // Then
        viewModel.state.test {
            assertEquals(Effect.of(true), awaitItem().changeBottomSheetVisibility)
        }
    }

    @Test
    @Ignore("To be re-enabled when adding back expiration time feature")
    fun `should emit state for hiding bottom sheet when action for saving expiration time is submitted`() = runTest {
        // Given
        val userId = expectedUserId { UserIdSample.Primary }
        val messageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expirationTime = 1.days
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(userId, messageId)
        expectObserveMessageSendingError(userId, messageId)
        expectMessagePassword(userId, messageId)
        expectNoFileShareVia()
        expectSaveExpirationTimeForDraft(userId, messageId, expirationTime)
        expectObserveMessageExpirationTime(userId, messageId)
        expectInitComposerWithNewEmptyDraftSucceeds(userId)

        // When
        viewModel.submit(ComposerAction.ExpirationTimeSet(duration = expirationTime))

        // Then
        viewModel.state.test {
            coVerify { saveMessageExpirationTime(userId, messageId, expirationTime) }
            assertEquals(Effect.of(false), awaitItem().changeBottomSheetVisibility)
        }
    }

    @Test
    @Ignore("To be re-enabled when adding back expiration time feature")
    fun `should emit state for showing an error when saving expiration time has failed`() = runTest {
        // Given
        val userId = expectedUserId { UserIdSample.Primary }
        val messageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expirationTime = 1.days
        expectNoInputDraftMessageId()
        expectInputDraftAction { DraftAction.Compose }
        expectObservedMessageAttachments(userId, messageId)
        expectObserveMessageSendingError(userId, messageId)
        expectMessagePassword(userId, messageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(userId, messageId)
        coEvery {
            saveMessageExpirationTime(userId, messageId, 1.days)
        } returns DataError.Local.DbWriteFailed.left()
        expectInitComposerWithNewEmptyDraftSucceeds(userId)

        // When
        viewModel.submit(ComposerAction.ExpirationTimeSet(duration = expirationTime))

        // Then
        viewModel.state.test {
            coVerify { saveMessageExpirationTime(userId, messageId, expirationTime) }
            assertEquals(Effect.of(TextUiModel(R.string.composer_error_setting_expiration_time)), awaitItem().error)
        }
    }

    @Test
    fun `should emit state with message expiration time when the expiration time has changed`() = runTest {
        // Given
        val userId = expectedUserId { UserIdSample.Primary }
        val messageId = expectedMessageId { MessageIdSample.EmptyDraft }
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectObservedMessageAttachments(userId, messageId)
        expectObserveMessageSendingError(userId, messageId)
        expectMessagePassword(userId, messageId)
        expectNoFileShareVia()
        expectInitComposerWithNewEmptyDraftSucceeds(userId)
        val messageExpirationTime = expectObserveMessageExpirationTime(userId, messageId)

        // Then
        viewModel.state.test {
            assertEquals(messageExpirationTime.expiresIn, awaitItem().messageExpiresIn)
        }
    }

    @Test
    fun `should emit event to confirm sending expiring message when there are external recipients and no password`() =
        runTest {
            // Given
            val expectedSubject = Subject("Subject for the message")
            val expectedSenderEmail = SenderEmail(UserAddressSample.PrimaryAddress.email)
            val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
            val expectedUserId = expectedUserId { UserIdSample.Primary }
            val expectedDraftBody = DraftBody("I am plaintext")
            val recipientsTo = RecipientsTo(listOf(RecipientSample.John))
            val recipientsCc = RecipientsCc(listOf(RecipientSample.John))
            val recipientsBcc = RecipientsBcc(listOf(RecipientSample.John))
            mockParticipantMapper()
            expectNetworkManagerIsDisconnected()
            expectNoInputDraftMessageId()
            expectNoInputDraftAction()
            expectSendMessageSucceeds(expectedUserId)
            expectObservedMessageAttachments(expectedUserId, expectedMessageId)
            expectObserveMessageSendingError(expectedUserId, expectedMessageId)
            expectNoMessagePassword(expectedUserId, expectedMessageId)
            expectNoFileShareVia()
            expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
            expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId) {
                DraftFields(
                    sender = expectedSenderEmail,
                    subject = expectedSubject,
                    body = expectedDraftBody,
                    recipientsTo = recipientsTo,
                    recipientsCc = recipientsCc,
                    recipientsBcc = recipientsBcc
                )
            }
            val externalRecipients = expectExternalRecipients(expectedUserId, recipientsTo, recipientsCc, recipientsBcc)

            // When
            viewModel.submit(ComposerAction.OnSendMessage)

            // Then
            viewModel.state.test {
                assertEquals(Effect.of(externalRecipients), awaitItem().confirmSendExpiringMessage)
            }
        }

    @Test
    fun `should send message when sending an expiring message to external recipients was confirmed`() = runTest {
        // Given
        val expectedSubject = Subject("Subject for the message")
        val expectedSenderEmail = SenderEmail(UserAddressSample.PrimaryAddress.email)
        val expectedMessageId = expectedMessageId { MessageIdSample.EmptyDraft }
        val expectedUserId = expectedUserId { UserIdSample.Primary }
        val expectedDraftBody = DraftBody("I am plaintext")
        val recipientsTo = RecipientsTo(listOf(RecipientSample.John))
        val recipientsCc = RecipientsCc(listOf(RecipientSample.John))
        val recipientsBcc = RecipientsBcc(listOf(RecipientSample.John))
        mockParticipantMapper()
        expectNetworkManagerIsDisconnected()
        expectNoInputDraftMessageId()
        expectNoInputDraftAction()
        expectSendMessageSucceeds(expectedUserId)
        expectObservedMessageAttachments(expectedUserId, expectedMessageId)
        expectObserveMessageSendingError(expectedUserId, expectedMessageId)
        expectMessagePassword(expectedUserId, expectedMessageId)
        expectNoFileShareVia()
        expectObserveMessageExpirationTime(expectedUserId, expectedMessageId)
        expectExternalRecipients(expectedUserId, recipientsTo, recipientsCc, recipientsBcc)
        expectInitComposerWithNewEmptyDraftSucceeds(expectedUserId) {
            DraftFields(
                sender = expectedSenderEmail,
                subject = expectedSubject,
                body = expectedDraftBody,
                recipientsTo = recipientsTo,
                recipientsCc = recipientsCc,
                recipientsBcc = recipientsBcc
            )
        }

        // When
        viewModel.submit(ComposerAction.SendExpiringMessageToExternalRecipientsConfirmed)

        // Then
        coVerifyOrder {
            sendMessageMock(expectedUserId)
        }
    }

    @AfterTest
    fun tearDown() {
        unmockkObject(ComposerDraftState.Companion)
    }

    private fun expectInitComposerForActionSuccess(
        userId: UserId,
        action: DraftAction,
        draftFields: () -> DraftFields
    ) = draftFields().also {
        coEvery { createDraftForAction(userId, action) } returns it.right()
    }

    // This is both used to mock the result of the "composer init" in the cases where we
    // create a new draft (eg. Compose, Reply, ComposeTo...)
    // and also as a hack to initialize the composer's state to an expected one to test
    private fun expectInitComposerWithNewEmptyDraftSucceeds(
        userId: UserId,
        result: () -> DraftFields = { DraftFieldsTestData.EmptyDraftWithPrimarySender }
    ) = result().also { draftFields ->
        coEvery { createEmptyDraft(userId) } coAnswers { draftFields.right() }
    }

    private fun expectInitComposerWithExistingDraftError(
        userId: UserId,
        draftId: MessageId,
        error: () -> DataError
    ) = error().also { coEvery { openExistingDraft(userId, draftId) } returns it.left() }

    private fun expectInitComposerWithExistingDraftSuccess(
        userId: UserId,
        draftId: MessageId,
        responseDelay: Long = 0L,
        result: () -> DraftFields
    ) = result().also { draftFields ->
        coEvery { openExistingDraft(userId, draftId) } coAnswers {
            delay(responseDelay)
            draftFields.right()
        }
    }

    private fun expectInputDraftAction(draftAction: () -> DraftAction) = draftAction().also {
        every { savedStateHandle.get<String>(ComposerScreen.SerializedDraftActionKey) } returns it.serialize()
    }

    private fun expectNoInputDraftAction() {
        every { savedStateHandle.get<String>(ComposerScreen.SerializedDraftActionKey) } returns null
    }

    private fun expectNoFileShareVia() {
        every { savedStateHandle.get<String>(ComposerScreen.DraftActionForShareKey) } returns null
    }

    private fun expectNoInputDraftMessageId() {
        every { savedStateHandle.get<String>(ComposerScreen.DraftMessageIdKey) } returns null
    }

    private fun expectDraftBodyWithSignature() = DraftBody(
        """
            Email body


            Signature
        """.trimIndent()
    )

    private fun expectObserveMessageSendingError(
        expectedUserId: UserId,
        expectedMessageId: MessageId,
        sendingError: SendingError? = null
    ) {
        coEvery { observeMessageSendingError(expectedUserId, expectedMessageId) } returns if (sendingError != null) {
            flowOf(sendingError)
        } else {
            flowOf()
        }
    }

    private fun expectInputDraftMessageId(draftId: () -> MessageId) = draftId().also {
        every { savedStateHandle.get<String>(ComposerScreen.DraftMessageIdKey) } returns it.id
    }

    private fun expectSendMessageSucceeds(expectedUserId: UserId) {
        coEvery { sendMessageMock.invoke(expectedUserId) } returns Unit
    }

    private fun expectNetworkManagerIsConnected() {
        every { networkManagerMock.isConnectedToNetwork() } returns true
    }

    private fun expectNetworkManagerIsDisconnected() {
        every { networkManagerMock.isConnectedToNetwork() } returns false
    }

    private fun expectedMessageId(messageId: () -> MessageId): MessageId = messageId().also {
        every { provideNewDraftIdMock() } returns it
    }

    private fun expectedUserId(userId: () -> UserId): UserId = userId().also {
        coEvery { observePrimaryUserIdMock() } returns flowOf(it)
    }

    private fun expectInitComposerWithNewEmptyDraftFails(userId: UserId, dataError: () -> DataError) =
        dataError().also {
            coEvery { createEmptyDraft(userId) } returns it.left()
        }

    private fun expectStoreDraftBodySucceeds(expectedDraftBody: DraftBody) {
        coEvery {
            storeDraftWithBodyMock(expectedDraftBody)
        } returns Unit.right()
    }

    private fun expectStoreDraftBodyFails(expectedDraftBody: DraftBody, error: () -> DataError) = error().also {
        coEvery {
            storeDraftWithBodyMock(expectedDraftBody)
        } returns it.left()
    }

    private fun expectStoreDraftSubjectSucceeds(expectedSubject: Subject) {
        coEvery {
            storeDraftWithSubjectMock(
                expectedSubject
            )
        } returns Unit.right()
    }

    private fun expectStoreDraftSubjectFails(expectedSubject: Subject, error: () -> DataError) = error().also {
        coEvery {
            storeDraftWithSubjectMock(
                expectedSubject
            )
        } returns it.left()
    }

    private fun expectUpdateBccRecipientsSucceeds(expectedRecipients: List<Recipient>) {
        coEvery {
            updateBccRecipients(emptyList(), expectedRecipients)
        } returns Unit.right()
    }

    private fun expectUpdateDraftBccRecipientsFails(expectedRecipients: List<Recipient>, error: () -> DataError) =
        error().also {
            coEvery {
                updateBccRecipients(emptyList(), expectedRecipients)
            } returns it.left()
        }

    private fun expectUpdateCcRecipientsSucceeds(expectedRecipients: List<Recipient>) {
        coEvery {
            updateCcRecipients(emptyList(), expectedRecipients)
        } returns Unit.right()
    }

    private fun expectUpdateDraftCcRecipientsFails(expectedRecipients: List<Recipient>, error: () -> DataError) =
        error().also {
            coEvery {
                updateCcRecipients(emptyList(), expectedRecipients)
            } returns it.left()
        }

    private fun expectUpdateToRecipientsSucceeds(expectedRecipients: List<Recipient>) {
        coEvery {
            updateToRecipients(emptyList(), expectedRecipients)
        } returns Unit.right()
    }

    private fun expectUpdateDraftToRecipientsFails(expectedRecipients: List<Recipient>, error: () -> DataError) =
        error().also {
            coEvery {
                updateToRecipients(emptyList(), expectedRecipients)
            } returns it.left()
        }

    private fun expectContacts(): List<ContactMetadata.Contact> {
        val expectedContacts = listOf(ContactSample.Doe, ContactSample.John)
        coEvery { getContactsMock.invoke(UserIdSample.Primary) } returns expectedContacts.right()
        return expectedContacts
    }

    private fun expectGetContactSuggestions(
        expectedUserId: UserId,
        expectedSearchTerm: String,
        expectedContacts: List<ContactMetadata.Contact>,
        expectedContactGroups: List<ContactMetadata.ContactGroup>
    ) {
        coEvery {
            getContactSuggestions.invoke(expectedUserId, ContactSuggestionQuery(expectedSearchTerm))
        } returns (expectedContacts + expectedContactGroups).right()
    }

    private fun expectSearchDeviceContacts(
        expectedSearchTerm: String,
        expectedDeviceContacts: List<DeviceContact>
    ): List<DeviceContact> {
        coEvery {
            searchDeviceContactsMock.invoke(expectedSearchTerm)
        } returns expectedDeviceContacts.right()
        return expectedDeviceContacts
    }

    private fun expectObservedMessageAttachments(userId: UserId, messageId: MessageId) {
        every {
            observeMessageAttachments(userId, messageId)
        } returns flowOf(listOf(AttachmentMetadataSamples.Invoice))
    }

    private fun expectAttachmentDeleteSucceeds(
        userId: UserId,
        messageId: MessageId,
        attachmentId: AttachmentId
    ) {
        coEvery { deleteAttachment(userId, messageId, attachmentId) } returns Unit.right()
    }

    private fun expectMessagePassword(userId: UserId, messageId: MessageId) {
        val messagePassword = MessagePassword(userId, messageId, "password", null)
        coEvery { observeMessagePassword(userId, messageId) } returns flowOf(messagePassword)
    }

    private fun expectNoMessagePassword(userId: UserId, messageId: MessageId) {
        coEvery { observeMessagePassword(userId, messageId) } returns flowOf(null)
    }

    private fun expectAddressValidation(address: String, expectedResult: Boolean) {
        every { isValidEmailAddressMock(address) } returns expectedResult
    }

    private fun expectSaveExpirationTimeForDraft(
        userId: UserId,
        messageId: MessageId,
        expirationTime: Duration
    ) {
        coEvery { saveMessageExpirationTime(userId, messageId, expirationTime) } returns Unit.right()
    }

    private fun expectObserveMessageExpirationTime(userId: UserId, messageId: MessageId) =
        MessageExpirationTime(userId, messageId, 1.days).also {
            coEvery { observeMessageExpirationTime(userId, messageId) } returns flowOf(it)
        }

    private fun mockParticipantMapper() {
        val expectedContacts = expectContacts()
        every {
            participantMapperMock.recipientUiModelToParticipant(
                RecipientUiModel.Valid("valid@email.com"),
                expectedContacts
            )
        } returns Recipient("valid@email.com", "Valid Email", false)
        every {
            participantMapperMock.recipientUiModelToParticipant(
                RecipientUiModel.Valid(RecipientSample.John.address),
                any()
            )
        } returns Recipient(RecipientSample.John.address, RecipientSample.John.name, false)
        every {
            participantMapperMock.recipientUiModelToParticipant(
                RecipientUiModel.Valid(RecipientSample.NamelessRecipient.address),
                any()
            )
        } returns Recipient(RecipientSample.NamelessRecipient.address, "", false)
    }

    private fun expectNoExternalRecipients(
        userId: UserId,
        recipientsTo: RecipientsTo,
        recipientsCc: RecipientsCc,
        recipientsBcc: RecipientsBcc
    ) {
        coEvery { getExternalRecipients(userId, recipientsTo, recipientsCc, recipientsBcc) } returns emptyList()
    }

    private fun expectExternalRecipients(
        userId: UserId,
        recipientsTo: RecipientsTo,
        recipientsCc: RecipientsCc,
        recipientsBcc: RecipientsBcc
    ) = listOf(RecipientSample.ExternalEncrypted).also {
        coEvery { getExternalRecipients(userId, recipientsTo, recipientsCc, recipientsBcc) } returns it
    }

    companion object TestData {

        const val RawDraftBody = "I'm a message body"

        val existingDraftFields = DraftFields(
            SenderEmail("author@proton.me"),
            Subject("Here is the matter"),
            DraftBody("Decrypted body of this draft"),
            RecipientsTo(listOf(Recipient("you@proton.ch", "Name"))),
            RecipientsCc(emptyList()),
            RecipientsBcc(emptyList())
        )

        val draftFieldsWithQuotedBody = DraftFields(
            SenderEmail("author@proton.me"),
            Subject("Here is the matter"),
            DraftBody(""),
            RecipientsTo(listOf(Recipient("you@proton.ch", "Name"))),
            RecipientsCc(emptyList()),
            RecipientsBcc(emptyList())
        )

        const val BaseInitials = "AB"
    }
}
