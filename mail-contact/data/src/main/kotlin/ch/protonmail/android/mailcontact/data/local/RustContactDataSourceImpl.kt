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
import ch.protonmail.android.mailcommon.data.mapper.LocalContactId
import ch.protonmail.android.mailcommon.data.mapper.LocalGroupedContacts
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcontact.data.ContactRustCoroutineScope
import ch.protonmail.android.mailcontact.data.mapper.ContactSuggestionsMapper
import ch.protonmail.android.mailcontact.data.mapper.DeviceContactsMapper
import ch.protonmail.android.mailcontact.data.mapper.GroupedContactsMapper
import ch.protonmail.android.mailcontact.data.usecase.CreateRustContactWatcher
import ch.protonmail.android.mailcontact.data.usecase.RustDeleteContact
import ch.protonmail.android.mailcontact.data.usecase.RustGetContactSuggestions
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.ContactSuggestionQuery
import ch.protonmail.android.mailcontact.domain.model.DeviceContact
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.model.GroupedContacts
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.ContactsLiveQueryCallback
import uniffi.proton_mail_uniffi.VoidActionResult
import javax.inject.Inject

class RustContactDataSourceImpl @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val groupedContactsMapper: GroupedContactsMapper,
    private val contactSuggestionMapper: ContactSuggestionsMapper,
    private val deviceContactsMapper: DeviceContactsMapper,
    private val createRustContactWatcher: CreateRustContactWatcher,
    private val rustDeleteContact: RustDeleteContact,
    private val rustGetContactSuggestions: RustGetContactSuggestions,
    @ContactRustCoroutineScope private val coroutineScope: CoroutineScope
) : RustContactDataSource {

    override suspend fun getContactSuggestions(
        userId: UserId,
        deviceContacts: List<DeviceContact>,
        query: ContactSuggestionQuery
    ): Either<DataError, List<ContactMetadata>> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-contact: trying to get contact suggestions with a null session")
            return DataError.Local.Unknown.left()
        }

        val localDeviceContacts = deviceContactsMapper.toLocalDeviceContact(deviceContacts)
        return rustGetContactSuggestions(session, localDeviceContacts, query.value)
            .map { contactSuggestionMapper.toContactSuggestions(it) }
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
        callbackFlow {
            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-contact-data-source: trying to load contacts with a null session")
                send(GetContactError.left())
                close()
                return@callbackFlow
            }

            val contactListUpdatedCallback = object : ContactsLiveQueryCallback {
                override fun onUpdate(contacts: List<LocalGroupedContacts>) {
                    coroutineScope.launch {
                        Timber.d("rust-contact-data-source: contact list updated")
                        send(contacts.map { groupedContactsMapper.toGroupedContacts(it) }.right())
                    }
                }
            }

            val contactListWatcher = createRustContactWatcher(session, contactListUpdatedCallback)
                .onLeft {
                    send(GetContactError.left())
                    close()
                    Timber.e("rust-contact-data-source: failed creating contact watcher $it")
                }
                .onRight { watcher ->
                    send(
                        watcher.contactList.map { contacts ->
                            groupedContactsMapper.toGroupedContacts(contacts)
                        }.right()
                    )
                    Timber.d("rust-contact-data-source: contact watcher created")
                }

            awaitClose {
                contactListWatcher.getOrNull()?.handle?.disconnect()
                contactListWatcher.getOrNull()?.destroy()
                Timber.d("rust-contact-data-source: contact watcher disconnected")
            }
        }

    override suspend fun deleteContact(userId: UserId, contactId: LocalContactId): Either<DataError.Local, Unit> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-contact: trying to load message with a null session")
            return DataError.Local.Unknown.left()
        }

        return when (rustDeleteContact(session, contactId)) {
            is VoidActionResult.Error -> {
                Timber.e("rust-contact: Failed to delete contact")
                return DataError.Local.Unknown.left()
            }

            VoidActionResult.Ok -> Unit.right()
        }
    }
}
