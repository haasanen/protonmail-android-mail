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

package ch.protonmail.android.mailmessage.data.usecase

import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailmessage.data.wrapper.MessagePaginatorWrapper
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.PaginatorFilter
import uniffi.proton_mail_uniffi.paginateMessagesForLabel
import javax.inject.Inject

class CreateRustMessagesPaginator @Inject constructor() {

    suspend operator fun invoke(
        session: MailUserSessionWrapper,
        labelId: LocalLabelId,
        unread: Boolean,
        callback: LiveQueryCallback
    ): MessagePaginatorWrapper {
        val filterParam = if (unread) true else null
        return MessagePaginatorWrapper(
            paginateMessagesForLabel(session.rustObject(), labelId, PaginatorFilter(filterParam), callback)
        )
    }
}
