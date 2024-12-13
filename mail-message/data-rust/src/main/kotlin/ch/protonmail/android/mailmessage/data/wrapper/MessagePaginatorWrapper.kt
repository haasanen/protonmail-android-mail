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

package ch.protonmail.android.mailmessage.data.wrapper

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.datarust.mapper.LocalMessageMetadata
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.model.PaginatorParams
import uniffi.proton_mail_uniffi.MessagePaginator
import uniffi.proton_mail_uniffi.MessagePaginatorNextPageResult
import uniffi.proton_mail_uniffi.MessagePaginatorReloadResult

class MessagePaginatorWrapper(
    private val rustPaginator: MessagePaginator,
    val params: PaginatorParams
) {

    suspend fun nextPage(): Either<DataError, List<LocalMessageMetadata>> =
        when (val result = rustPaginator.nextPage()) {
            is MessagePaginatorNextPageResult.Error -> result.v1.toDataError().left()
            is MessagePaginatorNextPageResult.Ok -> result.v1.right()
        }

    suspend fun reload(): Either<DataError, List<LocalMessageMetadata>> = when (val result = rustPaginator.reload()) {
        is MessagePaginatorReloadResult.Error -> result.v1.toDataError().left()
        is MessagePaginatorReloadResult.Ok -> result.v1.right()
    }

    fun destroy() {
        rustPaginator.handle().disconnect()
    }
}
