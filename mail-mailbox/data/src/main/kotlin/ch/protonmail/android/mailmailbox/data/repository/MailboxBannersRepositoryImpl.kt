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

package ch.protonmail.android.mailmailbox.data.repository

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.mapper.toLocalLabelId
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailmailbox.data.mapper.toAutoDeleteBanner
import ch.protonmail.android.mailmailbox.data.usecase.RustGetAutoDeleteBanner
import ch.protonmail.android.mailmailbox.domain.model.AutoDeleteBanner
import ch.protonmail.android.mailmailbox.domain.repository.MailboxBannersRepository
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

class MailboxBannersRepositoryImpl @Inject constructor(
    private val rustGetAutoDeleteBanner: RustGetAutoDeleteBanner,
    private val userSessionRepository: UserSessionRepository
) : MailboxBannersRepository {

    override suspend fun getAutoDeleteBanner(userId: UserId, labelId: LabelId): Either<DataError, AutoDeleteBanner> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("Trying to get auto-delete banner with null session; Failing.")
            return DataError.Local.NoUserSession.left()
        }

        return rustGetAutoDeleteBanner(session, labelId.toLocalLabelId())
            .onLeft { Timber.e("Getting the auto-delete banner failed.") }
            .flatMap {
                it?.toAutoDeleteBanner()?.right() ?: DataError.Local.NotFound.left()
            }
    }
}
