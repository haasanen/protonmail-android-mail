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

package ch.protonmail.android.mailcommon.datarust.mapper

import uniffi.proton_mail_uniffi.AttachmentMetadata
import uniffi.proton_mail_uniffi.ComposerDirection
import uniffi.proton_mail_uniffi.ComposerMode
import uniffi.proton_mail_uniffi.Conversation
import uniffi.proton_mail_uniffi.DecryptedMessage
import uniffi.proton_mail_uniffi.Id
import uniffi.proton_mail_uniffi.MailSettings
import uniffi.proton_mail_uniffi.Message
import uniffi.proton_mail_uniffi.MessageButtons
import uniffi.proton_mail_uniffi.MimeType
import uniffi.proton_mail_uniffi.PgpScheme
import uniffi.proton_mail_uniffi.PmSignature
import uniffi.proton_mail_uniffi.ShowImages
import uniffi.proton_mail_uniffi.ShowMoved
import uniffi.proton_mail_uniffi.SwipeAction
import uniffi.proton_mail_uniffi.SystemLabel
import uniffi.proton_mail_uniffi.ViewLayout
import uniffi.proton_mail_uniffi.ViewMode

typealias LocalUserId = String
typealias LocalConversation = Conversation
typealias LocalConversationId = Id
typealias LocalLabelId = Id
typealias LocalViewMode = ViewMode
typealias LocalMessageId = Id
typealias LocalMessageMetadata = Message
typealias LocalDecryptedMessage = DecryptedMessage
typealias LocalAttachmentMetadata = AttachmentMetadata
typealias LocalMimeType = MimeType
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
typealias LocalSystemLabel = SystemLabel
typealias LocalAddressId = Id
