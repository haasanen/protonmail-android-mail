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

import ch.protonmail.android.mailmessage.data.model.PaginatorParams
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.wrapper.MessagePaginatorWrapper
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.PaginateSearchResult
import uniffi.proton_mail_uniffi.PaginatorSearchOptions
import uniffi.proton_mail_uniffi.paginateSearch
import javax.inject.Inject

class CreateRustSearchPaginator @Inject constructor() {

    suspend operator fun invoke(
        session: MailUserSessionWrapper,
        keyword: String,
        callback: LiveQueryCallback
    ): Either<DataError, MessagePaginatorWrapper> = when (
        val result = paginateSearch(session.getRustUserSession(), PaginatorSearchOptions(keyword), callback)
    ) {
        is PaginateSearchResult.Error -> result.v1.toDataError().left()
        is PaginateSearchResult.Ok -> {
            val params = PaginatorParams(session.getRustUserSession().userId(), keyword = keyword)
            MessagePaginatorWrapper(result.v1, params).right()
        }
    }
}
