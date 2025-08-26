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

package ch.protonmail.android.mailmailbox.presentation.mailbox.mapper

import androidx.compose.ui.graphics.Color
import arrow.core.right
import ch.protonmail.android.mailattachments.domain.model.AttachmentDisposition
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadata
import ch.protonmail.android.mailattachments.domain.model.AttachmentMimeType
import ch.protonmail.android.mailattachments.domain.model.MimeTypeCategory
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.sample.ParticipantAvatarSample
import ch.protonmail.android.mailcommon.presentation.usecase.FormatShortTime
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.sample.LabelSample
import ch.protonmail.android.maillabel.presentation.R
import ch.protonmail.android.maillabel.presentation.model.LabelUiModel
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.domain.usecase.GetParticipantsResolvedNames
import ch.protonmail.android.mailmailbox.domain.usecase.ParticipantsResolvedNamesResult
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ExpiryInformationUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemLocationUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ParticipantUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ParticipantsUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.usecase.FormatMailboxScheduleSendTime
import ch.protonmail.android.mailmailbox.presentation.mailbox.usecase.GetMailboxItemLocationIcon
import ch.protonmail.android.mailmessage.domain.usecase.ResolveParticipantNameResult
import ch.protonmail.android.mailmessage.presentation.mapper.AttachmentMetadataUiModelMapper
import ch.protonmail.android.mailsettings.domain.model.FolderColorSettings
import ch.protonmail.android.mailsnooze.domain.model.NoSnooze
import ch.protonmail.android.mailsnooze.domain.model.SnoozeReminder
import ch.protonmail.android.mailsnooze.presentation.mapper.SnoozeStatusUiModelMapper
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeStatusUiModel
import ch.protonmail.android.testdata.mailbox.MailboxTestData
import ch.protonmail.android.testdata.mailbox.MailboxTestData.buildMailboxItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class MailboxItemUiModelMapperTest {

    private val userId = UserIdSample.Primary
    private val defaultFolderColorSettings = FolderColorSettings(
        useFolderColor = true,
        inheritParentFolderColor = false
    )

    private val mailboxAvatarUiModelMapper: MailboxAvatarUiModelMapper = mockk {
        every { this@mockk.invoke(any()) } returns mockk()
    }
    private val colorMapper: ColorMapper = mockk {
        every { toColor(any()) } returns Color.Unspecified.right()
    }
    private val getMailboxItemLocationIcons = mockk<GetMailboxItemLocationIcon> {
        coEvery {
            this@mockk(
                userId, any(),
                defaultFolderColorSettings, false
            )
        } returns GetMailboxItemLocationIcon.Result.None
    }
    private val formatShortTime: FormatShortTime = mockk()
    private val formatScheduleSendTime: FormatMailboxScheduleSendTime = mockk()

    private val getParticipantsResolvedNames = mockk<GetParticipantsResolvedNames> {
        coEvery {
            this@mockk.invoke(userId, any())
        } returns ParticipantsResolvedNamesResult.Senders(
            listOf(
                ResolveParticipantNameResult(
                    "default mocked name",
                    isProton = false
                )
            )
        )
    }

    private val expiryInformationUiModelMapper: ExpiryInformationUiModelMapper = mockk {
        every { this@mockk.toUiModel(any()) } returns ExpiryInformationUiModel.NoExpiry
    }

    private val attachmentMetadataUiModelMapper: AttachmentMetadataUiModelMapper = AttachmentMetadataUiModelMapper()

    private val snoozeStatusUiModelMapper: SnoozeStatusUiModelMapper = mockk {
        every { this@mockk.toUiModel(any()) } returns SnoozeStatusUiModel.NoStatus
    }

    private val mapper = MailboxItemUiModelMapper(
        mailboxAvatarUiModelMapper = mailboxAvatarUiModelMapper,
        colorMapper = colorMapper,
        formatShortTime = formatShortTime,
        formatScheduleSendTime = formatScheduleSendTime,
        getMailboxItemLocationIcon = getMailboxItemLocationIcons,
        getParticipantsResolvedNames = getParticipantsResolvedNames,
        expiryInformationUiModelMapper = expiryInformationUiModelMapper,
        attachmentMetadataUiModelMapper = attachmentMetadataUiModelMapper,
        snoozeStatusUiModelMapper = snoozeStatusUiModelMapper
    )

    @BeforeTest
    fun setup() {
        mockkConstructor(Duration::class)
        every { formatShortTime(anyConstructed()) } returns TextUiModel.Text("21 Feb")
    }

    @AfterTest
    fun teardown() {
        unmockkConstructor(Duration::class)
    }

    @Test
    fun `when mailbox message item was replied ui model shows reply icon`() = runTest {
        // Given
        val mailboxItem = MailboxTestData.repliedMailboxItem
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertTrue(actual.shouldShowRepliedIcon)
    }

    @Test
    fun `when mailbox message item was replied all ui model shows reply all icon`() = runTest {
        // Given
        val mailboxItem = MailboxTestData.repliedAllMailboxItem
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertTrue(actual.shouldShowRepliedAllIcon)
        assertFalse(actual.shouldShowRepliedIcon)
    }

    @Test
    fun `when mailbox message item was forwarded ui model shows forwarded icon`() = runTest {
        // Given
        val mailboxItem = MailboxTestData.allActionsMailboxItem
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertTrue(actual.shouldShowForwardedIcon)
    }

    @Test
    fun `mailbox items of conversation type never show any of reply, reply-all, forwarded icon`() = runTest {
        // Given
        val mailboxItem = MailboxTestData.mailboxConversationItem
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertFalse(actual.shouldShowRepliedIcon)
        assertFalse(actual.shouldShowRepliedAllIcon)
        assertFalse(actual.shouldShowForwardedIcon)
    }

    @Test
    fun `participant names are correctly resolved in the ui model`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem()
        val resolvedNames = listOf(
            ResolveParticipantNameResult("contact name", isProton = false),
            ResolveParticipantNameResult("display name", isProton = true)
        )
        val expected = ParticipantsUiModel.Participants(
            listOf(
                ParticipantUiModel("contact name", shouldShowOfficialBadge = false),
                ParticipantUiModel("display name", shouldShowOfficialBadge = true)
            ).toImmutableList()
        )
        coEvery {
            getParticipantsResolvedNames.invoke(userId, mailboxItem)
        } returns ParticipantsResolvedNamesResult.Recipients(resolvedNames)
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertEquals(expected, actual.participants)
    }

    @Test
    fun `empty recipient is correctly resolved to No Recipient in the ui model`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem()
        val resolvedNames = listOf(ResolveParticipantNameResult("", isProton = false))
        val expected = ParticipantsUiModel.NoParticipants(
            TextUiModel(ch.protonmail.android.mailmailbox.presentation.R.string.mailbox_default_recipient)
        )
        coEvery {
            getParticipantsResolvedNames.invoke(userId, mailboxItem)
        } returns ParticipantsResolvedNamesResult.Recipients(resolvedNames)
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertEquals(expected, actual.participants)
    }

    @Test
    fun `empty sender is correctly resolved to No Sender in the ui model`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem()
        val resolvedNames = listOf(ResolveParticipantNameResult("", isProton = false))
        val expected = ParticipantsUiModel.NoParticipants(
            TextUiModel(ch.protonmail.android.mailmailbox.presentation.R.string.mailbox_default_sender)
        )
        coEvery {
            getParticipantsResolvedNames.invoke(userId, mailboxItem)
        } returns ParticipantsResolvedNamesResult.Senders(resolvedNames)
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertEquals(expected, actual.participants)
    }

    @Test
    fun `mailbox item time is formatted normally when item is not scheduled`() = runTest {
        // Given
        val time: Long = 1_658_851_202
        val mailboxItem = buildMailboxItem(time = time)
        val result = TextUiModel.Text("18:00")
        every { formatShortTime.invoke(time.seconds) } returns result
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertEquals(result, actual.time)
    }

    @Test
    fun `mailbox item time is formatted as scheduled when item is scheduled`() = runTest {
        // Given
        val instant = Instant.fromEpochSeconds(1_658_851_202)
        val mailboxItem = buildMailboxItem(time = instant.epochSeconds, isScheduled = true)
        val result = TextUiModel.Text("tomorrow at 09:00")
        every { formatScheduleSendTime.invoke(instant) } returns result
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertEquals(result, actual.time)
    }

    @Test
    fun `when mailbox item of conversation type contains two or more messages show messages number`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(type = MailboxItemType.Conversation, numMessages = 2)
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertEquals(2, actual.numMessages)
    }

    @Test
    fun `when mailbox item contains less than two messages do not show messages number`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(type = MailboxItemType.Conversation, numMessages = 1)
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertNull(actual.numMessages)
    }

    @Test
    fun `when mailbox item is starred show starred`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(labelIds = listOf(SystemLabelId.Starred.labelId))
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertTrue(actual.isStarred)
    }

    @Test
    fun `when mailbox item is not starred do not show starred`() = runTest {
        // Given
        val labelIds = listOf(SystemLabelId.Drafts.labelId, SystemLabelId.Archive.labelId)
        val mailboxItem = buildMailboxItem(labelIds = labelIds)
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertFalse(actual.isStarred)
    }

    @Test
    fun `when use case returns location icon to be shown they are mapped to the ui model`() = runTest {
        // Given
        val labelIds = listOf(SystemLabelId.Inbox.labelId, SystemLabelId.Drafts.labelId)
        val mailboxItem = buildMailboxItem(labelIds = labelIds, type = MailboxItemType.Conversation)
        val inboxIconRes = MailboxItemLocationUiModel(R.drawable.ic_proton_inbox)
        val icons = GetMailboxItemLocationIcon.Result.Icon(inboxIconRes)
        coEvery {
            getMailboxItemLocationIcons.invoke(
                userId,
                mailboxItem, defaultFolderColorSettings, false
            )
        } returns icons
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        val expectedIconsRes = listOf(inboxIconRes)
        assertEquals(expectedIconsRes, actual.locations)
    }

    @Test
    fun `when use case returns no location icons to be shown empty list is mapped to the ui model`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem()
        coEvery {
            getMailboxItemLocationIcons.invoke(userId, mailboxItem, defaultFolderColorSettings, false)
        } returns GetMailboxItemLocationIcon.Result.None
        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertEquals(emptyList(), actual.locations)
    }

    @Test
    fun `avatar ui model should be received from the use case`() = runTest {
        // Given
        val avatarUiModel = ParticipantAvatarSample.ebay
        val mailboxItem = buildMailboxItem()
        val resolvedNames = listOf(
            ResolveParticipantNameResult("contact name", isProton = false),
            ResolveParticipantNameResult("display name", isProton = false)
        )
        coEvery {
            getParticipantsResolvedNames.invoke(userId, mailboxItem)
        } returns ParticipantsResolvedNamesResult.Senders(resolvedNames)
        every { mailboxAvatarUiModelMapper.invoke(mailboxItem) } returns avatarUiModel

        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)
        // Then
        assertEquals(avatarUiModel, actual.avatar)
    }

    @Test
    fun `when mailbox item has calendar attachments, show calendar icon`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(calendarAttachmentCount = 1)
        // When
        val mailboxItemUiModel = mapper.toUiModel(
            userId, mailboxItem, defaultFolderColorSettings, false
        )
        // Then
        assertTrue(mailboxItemUiModel.shouldShowCalendarIcon)
    }

    @Test
    fun `when mailbox item has no calendar attachments, don't show calendar icon`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(calendarAttachmentCount = 0)
        // When
        val mailboxItemUiModel = mapper.toUiModel(
            userId, mailboxItem, defaultFolderColorSettings, false
        )
        // Then
        assertFalse(mailboxItemUiModel.shouldShowCalendarIcon)
    }

    @Test
    fun `labels doesn't include folders and contacts groups`() = runTest {
        // given
        val mailboxItemLabels = listOf(
            LabelSample.build(labelId = LabelId("label"), type = LabelType.MessageLabel),
            LabelSample.build(labelId = LabelId("folder"), type = LabelType.MessageFolder),
            LabelSample.build(labelId = LabelId("group"), type = LabelType.ContactGroup)
        )
        val mailboxItem = buildMailboxItem(labels = mailboxItemLabels)

        // when
        val result = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)

        // then
        val expectedLabels = listOf(
            LabelUiModel(
                name = "label",
                color = Color.Unspecified,
                id = "label"
            )
        )
        assertEquals(expectedLabels, result.labels)
    }

    @Test
    fun `when mailbox item does not have all-drafts label, should open as draft is false`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(labelIds = listOf(SystemLabelId.AllMail.labelId))
        // When
        val mailboxItemUiModel = mapper.toUiModel(
            userId, mailboxItem, defaultFolderColorSettings, false
        )
        // Then
        assertFalse(mailboxItemUiModel.shouldOpenInComposer)
    }

    @Test
    fun `when mailbox item has no expiration time, expiryInformation should be NoExpiry`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(expirationTime = 0L)
        every { expiryInformationUiModelMapper.toUiModel(0L) } returns ExpiryInformationUiModel.NoExpiry

        // When
        val mailboxItemUiModel = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)

        // Then
        assertEquals(ExpiryInformationUiModel.NoExpiry, mailboxItemUiModel.expiryInformation)
    }

    @Test
    fun `when mailbox item has expiry time, expiryInformation should be HasExpiry`() = runTest {
        // Given
        val expirationTime = 1_700_000_000L
        val mailboxItem = buildMailboxItem(expirationTime = expirationTime)
        val expectedExpiry = ExpiryInformationUiModel.HasExpiry(
            TextUiModel("Expires in 19 days"),
            isLessThanOneHour = false
        )
        every { expiryInformationUiModelMapper.toUiModel(expirationTime) } returns expectedExpiry

        // When
        val mailboxItemUiModel = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)

        // Then
        assertEquals(expectedExpiry, mailboxItemUiModel.expiryInformation)
    }

    @Test
    fun `when mailbox item has attachments, they are correctly mapped to the UI model`() = runTest {
        // Given
        val attachments = listOf(
            AttachmentMetadata(
                attachmentId = AttachmentId("123"),
                name = "File1.pdf",
                size = 1024L,
                mimeType = AttachmentMimeType(
                    mime = "application/pdf",
                    category = MimeTypeCategory.Pdf
                ),
                disposition = AttachmentDisposition.Attachment,
                includeInPreview = true
            ),
            AttachmentMetadata(
                attachmentId = AttachmentId("456"),
                name = "File2.png", size = 2048L,
                mimeType = AttachmentMimeType(
                    mime = "image/png",
                    category = MimeTypeCategory.Image
                ),
                disposition = AttachmentDisposition.Attachment,
                includeInPreview = true
            ),
            AttachmentMetadata(
                attachmentId = AttachmentId("450"),
                name = "File2.html", size = 2048L,
                mimeType = AttachmentMimeType(
                    mime = "image/png",
                    category = MimeTypeCategory.Text
                ),
                disposition = AttachmentDisposition.Attachment,
                includeInPreview = false
            )
        )
        val mailboxItem = buildMailboxItem(attachments = attachments)
        val expectedUiModels = attachments.subList(0, 2).map { attachmentMetadataUiModelMapper.toUiModel(it) }

        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)

        // Then
        assertEquals(expectedUiModels, actual.attachments)
    }

    @Test
    fun `when mailbox item has no attachments, UI model attachments list should be empty`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(attachments = emptyList())

        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)

        // Then
        assertTrue(actual.attachments.isEmpty())
    }

    @Test
    fun `when mailbox item has not included attachments, UI model attachments list should not be included`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(
            attachments = listOf(
                AttachmentMetadata(
                    attachmentId = AttachmentId("450"),
                    name = "File2.html", size = 2048L,
                    mimeType = AttachmentMimeType(
                        mime = "image/png",
                        category = MimeTypeCategory.Text
                    ),
                    disposition = AttachmentDisposition.Attachment,
                    includeInPreview = false
                )
            )
        )

        // When
        val actual = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)

        // Then
        assertTrue(actual.attachments.isEmpty())
    }

    @Test
    fun `when mailbox item has displaySnoozeReminder then map displaySnoozeReminder`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(calendarAttachmentCount = 1).copy(snoozeStatus = SnoozeReminder)
        // When
        val mailboxItemUiModel = mapper.toUiModel(
            userId, mailboxItem, defaultFolderColorSettings, false
        )
        // Then
        assertTrue(mailboxItemUiModel.displaySnoozeReminder)
    }

    @Test
    fun `when mailbox item has no snooze, snoozeInformation should be NoSecondaryDate`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(expirationTime = 0L)
        every {
            snoozeStatusUiModelMapper
                .toUiModel(NoSnooze)

        } returns SnoozeStatusUiModel.NoStatus

        // When
        val mailboxItemUiModel = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)

        // Then
        assertEquals(SnoozeStatusUiModel.NoStatus, mailboxItemUiModel.snoozedUntil)
    }

    @Test
    fun `when mailbox item has snooze, snoozeInformation should be SecondaryDateInformationUiModel`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem()
        val expected = SnoozeStatusUiModel.SnoozeStatus(
            TextUiModel("Snoozed for 19 days")
        )
        every {
            snoozeStatusUiModelMapper
                .toUiModel(any())
        } returns expected

        // When
        val mailboxItemUiModel = mapper.toUiModel(userId, mailboxItem, defaultFolderColorSettings, false)

        // Then
        assertEquals(expected, mailboxItemUiModel.snoozedUntil)
    }
}
