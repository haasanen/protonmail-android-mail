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

package ch.protonmail.android.mailcontact.presentation.contactsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcontact.domain.usecase.SearchContacts
import ch.protonmail.android.mailcontact.presentation.model.ContactSearchUiModelMapper
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class ContactSearchViewModel @Inject constructor(
    private val reducer: ContactSearchReducer,
    private val contactSearchUiModelMapper: ContactSearchUiModelMapper,
    private val searchContacts: SearchContacts,
    observePrimaryUserId: ObservePrimaryUserId
) : ViewModel() {

    private val mutableState = MutableStateFlow(initialState)
    private val primaryUserId = observePrimaryUserId().filterNotNull()
    private val actionMutex = Mutex()

    val state: StateFlow<ContactSearchState> = mutableState

    private var searchContactsJob: Job? = null

    internal fun submit(action: ContactSearchViewAction) {
        viewModelScope.launch {
            actionMutex.withLock {
                when (action) {
                    is ContactSearchViewAction.OnSearchValueChanged -> handleOnSearchValueChanged(action)
                    is ContactSearchViewAction.OnSearchValueCleared -> handleOnSearchValueCleared()
                }
            }
        }
    }
    private suspend fun handleOnSearchValueChanged(action: ContactSearchViewAction.OnSearchValueChanged) {
        // Cancel the previous search job if it's still running
        searchContactsJob?.cancel()

        // Only proceed if the search value is not blank
        if (action.searchValue.isNotBlank()) {
            searchContactsJob = searchContacts(
                userId = primaryUserId(),
                query = action.searchValue
            ).map { contacts ->

                // Map the contacts to the required UI model
                val contactsUiModel = contactSearchUiModelMapper.toContactListItemUiModel(
                    contacts.getOrNull() ?: emptyList()
                )

                // Emit the new state with the loaded contacts
                emitNewStateFor(
                    ContactSearchEvent.ContactsLoaded(contacts = contactsUiModel)
                )
            }.launchIn(viewModelScope)
        } else {
            // Emit a state indicating that the contacts have been cleared
            emitNewStateFor(ContactSearchEvent.ContactsCleared)
        }
    }


    private fun handleOnSearchValueCleared() {
        emitNewStateFor(
            ContactSearchEvent.ContactsCleared
        )
    }

    private suspend fun primaryUserId() = primaryUserId.first()

    private fun emitNewStateFor(event: ContactSearchEvent) {
        val currentState = state.value
        mutableState.value = reducer.newStateFrom(currentState, event)
    }

    companion object {

        val initialState: ContactSearchState = ContactSearchState(
            contactUiModels = null
        )
    }
}
