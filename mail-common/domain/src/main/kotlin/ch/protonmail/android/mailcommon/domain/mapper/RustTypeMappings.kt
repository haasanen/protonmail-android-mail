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

package ch.protonmail.android.mailcommon.domain.mapper

import uniffi.proton_mail_uniffi.AttachmentMetadata
import uniffi.proton_mail_uniffi.ComposerDirection
import uniffi.proton_mail_uniffi.ComposerMode
import uniffi.proton_mail_uniffi.Conversation
import uniffi.proton_mail_uniffi.CustomLabel
import uniffi.proton_mail_uniffi.DecryptedMessage
import uniffi.proton_mail_uniffi.Label
import uniffi.proton_mail_uniffi.LabelType
import uniffi.proton_mail_uniffi.Message
import uniffi.proton_mail_uniffi.MessageButtons
import uniffi.proton_mail_uniffi.MimeType
import uniffi.proton_mail_uniffi.PgpScheme
import uniffi.proton_mail_uniffi.PmSignature
import uniffi.proton_mail_uniffi.ShowImages
import uniffi.proton_mail_uniffi.ShowMoved
import uniffi.proton_mail_uniffi.SwipeAction
import uniffi.proton_mail_uniffi.ViewLayout
import uniffi.proton_mail_uniffi.ViewMode

typealias LocalConversation = Conversation
typealias LocalConversationId = ULong
typealias LocalLabelId = ULong
typealias LocalViewMode = ViewMode
typealias LocalLabelType = LabelType
typealias LocalLabel = Label
typealias LocalMessageId = ULong
typealias LocalMessageMetadata = Message
typealias LocalDecryptedMessage = DecryptedMessage
typealias LocalAttachmentMetadata = AttachmentMetadata
typealias LocalMimeType = MimeType
typealias LocalCustomLabel = CustomLabel
typealias LocalMailSettings = MailSettings
typealias LocalPmSignature = PmSignature
typealias LocalComposerMode = ComposerMode
typealias LocalMessageButtons = MessageButtons
typealias LocalShowImages = ShowImages
typealias LocalShowMoved = ShowMoved
typealias LocalViewLayout = ViewLayout
typealias LocalSwipeAction = SwipeAction
typealias LocalPgpScheme = PgpScheme
typealias LocalComposerDirection = ComposerDirection
