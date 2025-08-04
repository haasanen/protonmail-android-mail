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

package ch.protonmail.android.composer.data.wrapper

import uniffi.proton_mail_uniffi.Draft
import uniffi.proton_mail_uniffi.DraftScheduleSendOptionsResult
import uniffi.proton_mail_uniffi.UnixTimestamp
import uniffi.proton_mail_uniffi.VoidDraftSaveResult
import uniffi.proton_mail_uniffi.VoidDraftSendResult

class DraftWrapper(private val rustDraft: Draft) {

    fun attachmentList(): AttachmentsWrapper = AttachmentsWrapper(rustDraft.attachmentList())

    fun embeddedImage(cid: String) = rustDraft.getEmbeddedAttachmentSync(cid)

    fun subject(): String = rustDraft.subject()

    fun sender(): String = rustDraft.sender()

    fun body(): String = rustDraft.body()

    fun recipientsTo(): ComposerRecipientListWrapper = ComposerRecipientListWrapper(rustDraft.toRecipients())

    fun recipientsCc(): ComposerRecipientListWrapper = ComposerRecipientListWrapper(rustDraft.ccRecipients())

    fun recipientsBcc(): ComposerRecipientListWrapper = ComposerRecipientListWrapper(rustDraft.bccRecipients())

    suspend fun messageId() = rustDraft.messageId()

    suspend fun save(): VoidDraftSaveResult = rustDraft.save()

    suspend fun send(): VoidDraftSendResult = rustDraft.send()

    suspend fun setSubject(subject: String): VoidDraftSaveResult = rustDraft.setSubject(subject)

    suspend fun setBody(body: String): VoidDraftSaveResult = rustDraft.setBody(body)

    fun scheduleSendOptions(): DraftScheduleSendOptionsResult = rustDraft.scheduleSendOptions()

    suspend fun scheduleSend(timestamp: UnixTimestamp) = rustDraft.schedule(timestamp)

    suspend fun listSenderAddresses() = rustDraft.listSenderAddresses()

    suspend fun changeSender(address: String) = rustDraft.changeSenderAddress(address)

    suspend fun isPasswordProtected() = rustDraft.isPasswordProtected()

    suspend fun setPassword(password: String, hint: String) = rustDraft.setPassword(password, hint)

    suspend fun removePassword() = rustDraft.removePassword()

    suspend fun getPassword() = rustDraft.getPassword()
}
