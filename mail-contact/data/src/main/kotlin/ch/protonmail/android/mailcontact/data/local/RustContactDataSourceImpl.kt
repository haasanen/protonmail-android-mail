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

package ch.protonmail.android.mailcontact.data.local

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcontact.data.mapper.GroupedContactsMapper
import ch.protonmail.android.mailcontact.data.usecase.GetRustContactList
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.model.GroupedContacts
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transformLatest
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.MailSessionException
import javax.inject.Inject

class RustContactDataSourceImpl @Inject constructor(
    private val getRustContactList: GetRustContactList,
    private val userSessionRepository: UserSessionRepository,
    private val groupedContactsMapper: GroupedContactsMapper
) : RustContactDataSource {

    override fun observeAllContacts(userId: UserId): Flow<Either<GetContactError, List<ContactMetadata>>> {
        return observeAllGroupedContacts(userId).transformLatest {
            it.onRight { groupedContactsList ->
                val contactMetadataList = mutableListOf<ContactMetadata>()

                groupedContactsList.map { groupedContacts ->
                    contactMetadataList.addAll(groupedContacts.contacts)
                }

                emit(contactMetadataList.right())
            }
            it.onLeft {
                emit(GetContactError.left())
            }
        }

    }

    override fun observeAllGroupedContacts(userId: UserId): Flow<Either<GetContactError, List<GroupedContacts>>> =
        flow {
            try {
                val session = userSessionRepository.getUserSession(userId)
                if (session == null) {
                    Timber.e("rust-message: trying to load message with a null session")
                    emit(GetContactError.left())
                } else {
                    val contacts = getRustContactList(session)

                    val contactMetadata = contacts.map { groupedContactsMapper.toGroupedContacts(it) }
                    emit(contactMetadata.right())
                }

            } catch (e: MailSessionException) {
                Timber.e(e, "rust-message: Failed to get message")
                emit(GetContactError.left())
            }

        }
}
