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

import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailmessage.data.wrapper.MailboxWrapper
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId

interface RustMailbox {

    suspend fun switchToMailbox(userId: UserId, labelId: LocalLabelId)
    fun observeMailbox(): Flow<MailboxWrapper>
    fun observeMailbox(labelId: LocalLabelId): Flow<MailboxWrapper>

    @MissingRustApi
    // markMessagesRead Rust function requires current label id as a parameter
    // Therefore this function is added temporarily to get the current label id
    fun observeCurrentLabelId(): Flow<LocalLabelId>
}
