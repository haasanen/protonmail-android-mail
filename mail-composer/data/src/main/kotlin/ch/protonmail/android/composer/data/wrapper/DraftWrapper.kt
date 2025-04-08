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
import uniffi.proton_mail_uniffi.VoidDraftSaveSendResult

class DraftWrapper(private val rustDraft: Draft) {

    fun attachmentList(): AttachmentsWrapper = AttachmentsWrapper(rustDraft.attachmentList())

    fun subject(): String = rustDraft.subject()

    fun sender(): String = rustDraft.sender()

    fun body(): String = rustDraft.body()

    fun recipientsTo(): ComposerRecipientListWrapper = ComposerRecipientListWrapper(rustDraft.toRecipients())

    fun recipientsCc(): ComposerRecipientListWrapper = ComposerRecipientListWrapper(rustDraft.ccRecipients())

    fun recipientsBcc(): ComposerRecipientListWrapper = ComposerRecipientListWrapper(rustDraft.bccRecipients())

    suspend fun messageId() = rustDraft.messageId()

    suspend fun save(): VoidDraftSaveSendResult = rustDraft.save()

    suspend fun send(): VoidDraftSaveSendResult = rustDraft.send()

    suspend fun setSubject(subject: String): VoidDraftSaveSendResult = rustDraft.setSubject(subject)

    suspend fun setBody(body: String): VoidDraftSaveSendResult = rustDraft.setBody(body)
}
