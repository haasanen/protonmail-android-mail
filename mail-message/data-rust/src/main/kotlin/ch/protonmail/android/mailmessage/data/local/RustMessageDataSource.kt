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

package ch.protonmail.android.mailmessage.data.local

import ch.protonmail.android.mailcommon.datarust.mapper.LocalDecryptedMessage
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageMetadata
import ch.protonmail.android.mailpagination.domain.model.PageKey
import me.proton.core.domain.entity.UserId

interface RustMessageDataSource {

    suspend fun getMessage(userId: UserId, messageId: LocalMessageId): LocalMessageMetadata?
    suspend fun getMessageBody(
        userId: UserId,
        messageId: LocalMessageId,
        labelId: LocalLabelId?
    ): LocalDecryptedMessage?

    suspend fun getMessages(userId: UserId, pageKey: PageKey): List<LocalMessageMetadata>

    suspend fun getSenderImage(
        userId: UserId,
        address: String,
        bimi: String?
    ): String?

    suspend fun markRead(userId: UserId, messages: List<LocalMessageId>)
    suspend fun markUnread(userId: UserId, messages: List<LocalMessageId>)

    fun disconnect()
}
