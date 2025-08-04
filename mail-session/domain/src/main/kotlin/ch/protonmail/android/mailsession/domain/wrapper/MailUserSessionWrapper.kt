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

package ch.protonmail.android.mailsession.domain.wrapper

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalAttachmentId
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import uniffi.proton_mail_uniffi.AsyncLiveQueryCallback
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailUserSessionForkResult
import uniffi.proton_mail_uniffi.MailUserSessionUserResult
import uniffi.proton_mail_uniffi.User
import uniffi.proton_mail_uniffi.VoidEventResult

class MailUserSessionWrapper(private val userSession: MailUserSession) {

    fun getRustUserSession() = userSession

    suspend fun fork(): Either<DataError, String> = when (val result = userSession.fork("android", "mail")) {
        is MailUserSessionForkResult.Error -> result.v1.toDataError().left()
        is MailUserSessionForkResult.Ok -> result.v1.right()
    }

    suspend fun pollEvents(): Either<DataError, Unit> = when (val result = userSession.forceEventLoopPoll()) {
        is VoidEventResult.Error -> result.v1.toDataError().left()
        VoidEventResult.Ok -> Unit.right()
    }

    suspend fun imageForSender(address: String, bimi: String?) = userSession.imageForSender(
        address,
        bimi,
        true,
        128u,
        null,
        "png"
    )

    suspend fun getAttachment(attachmentId: LocalAttachmentId) = userSession.getAttachment(attachmentId)

    fun watchUser(callback: AsyncLiveQueryCallback) = userSession.watchUser(callback)

    suspend fun getUser(): Either<DataError, User> = when (val result = userSession.user()) {
        is MailUserSessionUserResult.Error -> result.v1.toDataError().left()
        is MailUserSessionUserResult.Ok -> result.v1.right()
    }
}
