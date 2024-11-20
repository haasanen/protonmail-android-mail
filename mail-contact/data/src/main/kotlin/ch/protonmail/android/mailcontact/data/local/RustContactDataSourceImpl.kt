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
import ch.protonmail.android.mailcommon.datarust.mapper.LocalContactId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalGroupedContacts
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcontact.data.ContactRustCoroutineScope
import ch.protonmail.android.mailcontact.data.mapper.GroupedContactsMapper
import ch.protonmail.android.mailcontact.data.usecase.CreateRustContactWatcher
import ch.protonmail.android.mailcontact.data.usecase.RustDeleteContact
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.model.GroupedContacts
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.ContactsLiveQueryCallback
import uniffi.proton_mail_uniffi.MailSessionException
import uniffi.proton_mail_uniffi.WatchedContactList
import javax.inject.Inject

class RustContactDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val groupedContactsMapper: GroupedContactsMapper,
    private val createRustContactWatcher: CreateRustContactWatcher,
    private val rustDeleteContact: RustDeleteContact,
    @ContactRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustContactDataSource {

    private var contactListWatcher: WatchedContactList? = null
    private val mutex = Mutex()
    private val contactListMutableStatusFlow = MutableStateFlow<List<GroupedContacts>>(emptyList())
    private val contactListStatusFlow: Flow<List<GroupedContacts>> = contactListMutableStatusFlow
        .asStateFlow()

    private val contactListUpdatedCallback = object : ContactsLiveQueryCallback {
        override fun onUpdate(contacts: List<LocalGroupedContacts>) {
            Timber.d("rust-contact-data-source: contact list updated")
            coroutineScope.launch {
                mutex.withLock {
                    contactListMutableStatusFlow.value = contacts.map { groupedContactsMapper.toGroupedContacts(it) }
                }
            }
        }
    }

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
            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                emit(GetContactError.left())
            } else {
                initialiseOrUpdateContactListWatcher(session)
                emitAll(contactListStatusFlow.map { it.right() })
            }
        }

    private suspend fun initialiseOrUpdateContactListWatcher(session: MailUserSessionWrapper) {
        mutex.withLock {
            if (contactListWatcher == null) {
                contactListWatcher = createRustContactWatcher(session, contactListUpdatedCallback)
                contactListWatcher?.let {
                    contactListMutableStatusFlow.value = it.contactList.map { groupedContacts ->
                        groupedContactsMapper.toGroupedContacts(groupedContacts)
                    }
                    Timber.d("rust-contact-data-source: contact watcher created")
                }
                Timber.d("rust-contact-data-source: contact watcher created")
            }
        }
    }

    override suspend fun deleteContact(userId: UserId, contactId: LocalContactId): Either<DataError.Local, Unit> {
        return try {
            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-contact: trying to load message with a null session")
                DataError.Local.Unknown.left()
            } else {
                rustDeleteContact(session, contactId)
                Unit.right()
            }
        } catch (e: MailSessionException) {
            Timber.e(e, "rust-contact: Failed to delete contact")
            return DataError.Local.Unknown.left()
        }
    }
}
