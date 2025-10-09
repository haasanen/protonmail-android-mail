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

package ch.protonmail.android.mailconversation.data.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailconversation.data.wrapper.ConversationPaginatorWrapper
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import uniffi.proton_mail_uniffi.ConversationScrollerLiveQueryCallback
import uniffi.proton_mail_uniffi.IncludeSwitch
import uniffi.proton_mail_uniffi.ReadFilter
import uniffi.proton_mail_uniffi.ScrollConversationsForLabelResult
import uniffi.proton_mail_uniffi.scrollConversationsForLabel
import javax.inject.Inject

class CreateRustConversationPaginator @Inject constructor() {

    suspend operator fun invoke(
        session: MailUserSessionWrapper,
        labelId: LocalLabelId,
        unread: Boolean,
        showSpamTrash: Boolean,
        callback: ConversationScrollerLiveQueryCallback
    ): Either<DataError, ConversationPaginatorWrapper> {
        val filterParam = if (unread) ReadFilter.UNREAD else ReadFilter.ALL
        val includeSwitch = if (showSpamTrash) IncludeSwitch.WITH_SPAM_AND_TRASH else IncludeSwitch.DEFAULT
        return when (
            val result = scrollConversationsForLabel(
                session = session.getRustUserSession(),
                labelId = labelId,
                unread = filterParam,
                include = includeSwitch,
                callback
            )
        ) {
            is ScrollConversationsForLabelResult.Error -> result.v1.toDataError().left()
            is ScrollConversationsForLabelResult.Ok -> ConversationPaginatorWrapper(result.v1).right()
        }
    }
}
