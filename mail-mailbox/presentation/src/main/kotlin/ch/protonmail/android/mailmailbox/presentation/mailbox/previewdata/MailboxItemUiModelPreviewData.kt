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

package ch.protonmail.android.mailmailbox.presentation.mailbox.previewdata

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maillabel.presentation.model.LabelUiModel
import ch.protonmail.android.mailmailbox.domain.model.MailboxItemType
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ExpiryInformationUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemLocationUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ParticipantUiModel
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ParticipantsUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentIdUiModel
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentMetadataUiModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import me.proton.core.domain.entity.UserId

object MailboxItemUiModelPreviewData {

    val AccuWeatherAvatar = AvatarUiModel.ParticipantAvatar("A", "test@proton.me", null, Color.Blue)
    const val AccuWeatherName = "AccuWeather"
    val UserId = UserId("user")
    val WeatherForecastConversationId = ConversationId("WeatherForecasts")

    object Conversation {

        val DroidConLondon = MailboxItemUiModel(
            avatar = AvatarUiModel.ParticipantAvatar("D", "test@proton.me", null, Color.Blue),
            type = MailboxItemType.Conversation,
            userId = "userId",
            id = "DroidConLondon",
            conversationId = ConversationId("DroidConLondon"),
            time = TextUiModel.Text("Aug 20th 2022"),
            isRead = true,
            labels = persistentListOf(),
            subject = "DroidCon London",
            participants = ParticipantsUiModel.Participants(
                listOf(ParticipantUiModel(name = "DroidCon", shouldShowOfficialBadge = false)).toImmutableList()
            ),
            shouldShowRepliedIcon = false,
            shouldShowRepliedAllIcon = false,
            shouldShowForwardedIcon = true,
            numMessages = 2,
            isStarred = true,
            locations = persistentListOf(MailboxItemLocationUiModel(R.drawable.ic_proton_archive_box)),
            expiryInformation = ExpiryInformationUiModel.NoExpiry,
            shouldShowCalendarIcon = true,
            shouldOpenInComposer = false,
            attachments = persistentListOf()
        )

        val DroidConLondonWithZeroMessages = MailboxItemUiModel(
            avatar = AvatarUiModel.ParticipantAvatar("D", "test@proton.me", null, Color.Blue),
            type = MailboxItemType.Conversation,
            userId = "userId",
            id = "DroidConLondon",
            conversationId = ConversationId("DroidConLondon"),
            time = TextUiModel.Text("Aug 20th 2022"),
            isRead = true,
            labels = persistentListOf(),
            subject = "DroidCon London",
            participants = ParticipantsUiModel.Participants(
                listOf(ParticipantUiModel(name = "DroidCon", shouldShowOfficialBadge = false)).toImmutableList()
            ),
            shouldShowRepliedIcon = false,
            shouldShowRepliedAllIcon = false,
            shouldShowForwardedIcon = false,
            numMessages = null,
            isStarred = true,
            locations = persistentListOf(),
            expiryInformation = ExpiryInformationUiModel.NoExpiry,
            shouldShowCalendarIcon = false,
            shouldOpenInComposer = false,
            attachments = persistentListOf()
        )

        val WeatherForecast = MailboxItemUiModel(
            avatar = AccuWeatherAvatar,
            type = MailboxItemType.Conversation,
            userId = "userId",
            id = "WeatherForecast",
            conversationId = WeatherForecastConversationId,
            time = Message.WeatherForecastSep.time,
            isRead = false,
            labels = persistentListOf(),
            subject = "Weather Forecast",
            participants = ParticipantsUiModel.Participants(
                listOf(ParticipantUiModel(name = AccuWeatherName, shouldShowOfficialBadge = false)).toImmutableList()
            ),
            shouldShowRepliedIcon = false,
            shouldShowRepliedAllIcon = false,
            shouldShowForwardedIcon = false,
            numMessages = 2,
            isStarred = true,
            locations = persistentListOf(
                MailboxItemLocationUiModel(R.drawable.ic_proton_inbox),
                MailboxItemLocationUiModel(R.drawable.ic_proton_trash)
            ),
            expiryInformation = ExpiryInformationUiModel.NoExpiry,
            shouldShowCalendarIcon = false,
            shouldOpenInComposer = false,
            attachments = persistentListOf()
        )

        val MultipleRecipientWithLabel = MailboxItemUiModel(
            avatar = AvatarUiModel.ParticipantAvatar("D", "test@proton.me", null, Color.Blue),
            type = MailboxItemType.Conversation,
            userId = "userId",
            id = "DroidConLondon",
            conversationId = ConversationId("DroidConLondon"),
            time = TextUiModel.Text("Aug 20th 2022"),
            isRead = true,
            labels = persistentListOf(
                LabelUiModel("Long Test", Color.Red, id = "longTest"),
                LabelUiModel("Test", Color.Blue, id = "test"),
                LabelUiModel("Even Longer Test", Color.Cyan, id = "evenLongerTest"),
                LabelUiModel("Short", Color.Blue, id = "short"),
                LabelUiModel("1234567890123", Color.Blue, id = "random"),
                LabelUiModel("Very important mail label", Color.Green, id = "important")
            ),
            subject = "DroidCon London",
            participants = ParticipantsUiModel.Participants(
                listOf(
                    ParticipantUiModel(name = "FirstRecipient", shouldShowOfficialBadge = false),
                    ParticipantUiModel(name = "SecondRecipient", shouldShowOfficialBadge = false),
                    ParticipantUiModel(name = "ThirdRecipient", shouldShowOfficialBadge = false)
                ).toImmutableList()
            ),
            shouldShowRepliedIcon = true,
            shouldShowRepliedAllIcon = true,
            shouldShowForwardedIcon = true,
            numMessages = 2,
            isStarred = true,
            locations = persistentListOf(),
            expiryInformation = ExpiryInformationUiModel.NoExpiry,
            shouldShowCalendarIcon = true,
            shouldOpenInComposer = false,
            attachments = persistentListOf(
                AttachmentMetadataUiModel(
                    id = AttachmentIdUiModel("0"),
                    name = "Account_statement_01.pdf",
                    icon = R.drawable.ic_file_type_pdf,
                    contentDescription = R.string.attachment_type_pdf,
                    size = 5678L
                ),
                AttachmentMetadataUiModel(
                    id = AttachmentIdUiModel("1"),
                    name = "Account_statement_all.zip",
                    icon = R.drawable.ic_file_type_zip,
                    contentDescription = R.string.attachment_type_archive,
                    size = 5678L
                )
            )

        )

        val LongSubjectWithIcons = MailboxItemUiModel(
            avatar = AccuWeatherAvatar,
            type = MailboxItemType.Conversation,
            userId = "userId",
            id = "WeatherForecast",
            conversationId = WeatherForecastConversationId,
            time = Message.WeatherForecastSep.time,
            isRead = false,
            labels = persistentListOf(),
            subject = "This is a really long subject without any information",
            participants = ParticipantsUiModel.Participants(
                listOf(ParticipantUiModel(name = AccuWeatherName, shouldShowOfficialBadge = false)).toImmutableList()
            ),
            shouldShowRepliedIcon = false,
            shouldShowRepliedAllIcon = false,
            shouldShowForwardedIcon = false,
            numMessages = 2,
            isStarred = true,
            locations = persistentListOf(
                MailboxItemLocationUiModel(R.drawable.ic_proton_inbox),
                MailboxItemLocationUiModel(R.drawable.ic_proton_trash)
            ),
            expiryInformation = ExpiryInformationUiModel.NoExpiry,
            shouldShowCalendarIcon = false,
            shouldOpenInComposer = false,
            attachments = persistentListOf()
        )

        val LongSubjectWithoutIcons = MailboxItemUiModel(
            avatar = AccuWeatherAvatar,
            type = MailboxItemType.Conversation,
            userId = "userId",
            id = "WeatherForecast",
            conversationId = WeatherForecastConversationId,
            time = Message.WeatherForecastSep.time,
            isRead = false,
            labels = persistentListOf(),
            subject = "This is a really long subject without any information",
            participants = ParticipantsUiModel.Participants(
                listOf(ParticipantUiModel(name = AccuWeatherName, shouldShowOfficialBadge = false)).toImmutableList()
            ),
            shouldShowRepliedIcon = false,
            shouldShowRepliedAllIcon = false,
            shouldShowForwardedIcon = false,
            numMessages = 2,
            isStarred = false,
            locations = persistentListOf(),
            expiryInformation = ExpiryInformationUiModel.NoExpiry,
            shouldShowCalendarIcon = false,
            shouldOpenInComposer = false,
            attachments = persistentListOf()
        )

        val NoParticipant = MailboxItemUiModel(
            avatar = AccuWeatherAvatar,
            type = MailboxItemType.Conversation,
            userId = "userId",
            id = "WeatherForecast",
            conversationId = WeatherForecastConversationId,
            time = Message.WeatherForecastSep.time,
            isRead = false,
            labels = persistentListOf(),
            subject = "This is a really long subject without any information",
            participants = ParticipantsUiModel.NoParticipants(message = TextUiModel(R.string.mailbox_default_sender)),
            shouldShowRepliedIcon = false,
            shouldShowRepliedAllIcon = false,
            shouldShowForwardedIcon = false,
            numMessages = 2,
            isStarred = false,
            locations = persistentListOf(),
            expiryInformation = ExpiryInformationUiModel.NoExpiry,
            shouldShowCalendarIcon = false,
            shouldOpenInComposer = false,
            attachments = persistentListOf()
        )
    }

    object Message {

        val WeatherForecastAug = MailboxItemUiModel(
            avatar = AccuWeatherAvatar,
            type = MailboxItemType.Message,
            userId = "userId",
            id = "WeatherForecastAugust2022",
            conversationId = WeatherForecastConversationId,
            time = TextUiModel.Text("Jul 30th 2022"),
            isRead = true,
            labels = persistentListOf(),
            subject = "Weather Forecast for August 2022",
            participants = ParticipantsUiModel.Participants(
                listOf(ParticipantUiModel(name = AccuWeatherName, shouldShowOfficialBadge = false)).toImmutableList()
            ),
            shouldShowRepliedIcon = false,
            shouldShowRepliedAllIcon = false,
            shouldShowForwardedIcon = false,
            numMessages = 1,
            isStarred = true,
            locations = persistentListOf(),
            expiryInformation = ExpiryInformationUiModel.NoExpiry,
            shouldShowCalendarIcon = false,
            shouldOpenInComposer = false,
            attachments = persistentListOf()
        )

        val WeatherForecastSep = MailboxItemUiModel(
            avatar = AccuWeatherAvatar,
            type = MailboxItemType.Message,
            userId = "userId",
            id = "WeatherForecastSeptember2022",
            conversationId = WeatherForecastConversationId,
            time = TextUiModel.TextRes(R.string.yesterday),
            isRead = false,
            labels = persistentListOf(),
            subject = "Weather Forecast for September 2022",
            participants = ParticipantsUiModel.Participants(
                listOf(ParticipantUiModel(name = AccuWeatherName, shouldShowOfficialBadge = false)).toImmutableList()
            ),
            shouldShowRepliedIcon = false,
            shouldShowRepliedAllIcon = false,
            shouldShowForwardedIcon = false,
            numMessages = 1,
            isStarred = true,
            locations = persistentListOf(),
            expiryInformation = ExpiryInformationUiModel.NoExpiry,
            shouldShowCalendarIcon = false,
            shouldOpenInComposer = false,
            attachments = persistentListOf()
        )
    }
}
