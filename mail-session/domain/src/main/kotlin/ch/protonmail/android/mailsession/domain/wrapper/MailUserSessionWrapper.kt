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
import ch.protonmail.android.mailcommon.datarust.mapper.LocalAttachmentId
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import me.proton.core.util.kotlin.CoreLogger
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.MailUserSessionForkResult
import uniffi.proton_mail_uniffi.VoidEventResult

class MailUserSessionWrapper(
    userSession: MailUserSession
) {

    private var session: MailUserSession? = userSession

    fun close() {
        session?.close()
        session = null
        CoreLogger.e("rust", "session closed")
    }

    fun getRustUserSession(): MailUserSession = requireNotNull(session) {
        "Trying to access a closed/destroyed MailUserSession"
    }

    suspend fun fork(): Either<DataError, String> = when (val result = getRustUserSession().fork()) {
        is MailUserSessionForkResult.Error -> result.v1.toDataError().left()
        is MailUserSessionForkResult.Ok -> result.v1.right()
    }

    suspend fun pollEvents(): Either<DataError, Unit> = when (val result = getRustUserSession().pollEvents()) {
        is VoidEventResult.Error -> result.v1.toDataError().left()
        is VoidEventResult.Ok -> Unit.right()
    }

    suspend fun imageForSender(address: String, bimi: String?) = getRustUserSession().imageForSender(
        address = address,
        bimiSelector = bimi,
        displaySenderImage = true,
        size = 128u,
        mode = null,
        format = "png"
    )

    suspend fun getAttachment(attachmentId: LocalAttachmentId) = getRustUserSession().getAttachment(attachmentId)
}
