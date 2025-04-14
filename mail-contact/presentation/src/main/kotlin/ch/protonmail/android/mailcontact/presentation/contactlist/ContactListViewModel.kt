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

package ch.protonmail.android.mailcontact.presentation.contactlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.usecase.DeleteContact
import ch.protonmail.android.mailcontact.domain.usecase.ObserveGroupedContacts
import ch.protonmail.android.mailcontact.presentation.model.GroupedContactListItemsUiModelMapper
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val observeGroupedContacts: ObserveGroupedContacts,
    private val deleteContact: DeleteContact,
    private val reducer: ContactListReducer,
    private val groupedContactListItemsUiModelMapper: GroupedContactListItemsUiModelMapper,
    observePrimaryUserId: ObservePrimaryUserId
) : ViewModel() {

    private val primaryUserId = observePrimaryUserId().filterNotNull()

    private val mutableState: MutableStateFlow<ContactListState> = MutableStateFlow(ContactListState.Loading())
    val state: StateFlow<ContactListState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            flowContactListEvent(userId = primaryUserId())
                .onEach { contactListEvent -> emitNewStateFor(contactListEvent) }
                .launchIn(viewModelScope)
        }
    }

    internal fun submit(action: ContactListViewAction) {
        viewModelScope.launch {
            when (action) {
                ContactListViewAction.OnOpenBottomSheet -> emitNewStateFor(ContactListEvent.OpenBottomSheet)
                ContactListViewAction.OnOpenContactSearch -> emitNewStateFor(ContactListEvent.OpenContactSearch)
                ContactListViewAction.OnDismissBottomSheet -> emitNewStateFor(ContactListEvent.DismissBottomSheet)
                is ContactListViewAction.OnDeleteContactConfirmed -> handleOnDeleteContactConfirmed(action.contactId)
                is ContactListViewAction.OnDeleteContactRequested -> emitNewStateFor(
                    ContactListEvent.DeleteContactRequested(action.contact)
                )
            }
        }
    }

    private suspend fun handleOnDeleteContactConfirmed(contactId: ContactId) {
        Timber.d("Deleting contact with id: $contactId")
        deleteContact(primaryUserId(), contactId)

        emitNewStateFor(ContactListEvent.DeleteContactConfirmed)
    }


    private fun flowContactListEvent(userId: UserId): Flow<ContactListEvent> =
        observeGroupedContacts(userId).mapLatest { contactsEither ->
            contactsEither.fold(
                ifRight = { contactList ->
                    ContactListEvent.ContactListLoaded(
                        groupedContactsList = contactList.map { groupedContactListItemsUiModelMapper.toUiModel(it) }
                    )
                },
                ifLeft = {
                    Timber.e("Error while observing contacts")
                    ContactListEvent.ErrorLoadingContactList
                }
            )
        }


    private fun emitNewStateFor(event: ContactListEvent) {
        val currentState = state.value
        mutableState.update { reducer.newStateFrom(currentState, event) }
    }

    private suspend fun primaryUserId() = primaryUserId.first()
}
