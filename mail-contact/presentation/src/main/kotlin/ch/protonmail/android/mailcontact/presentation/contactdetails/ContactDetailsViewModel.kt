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

package ch.protonmail.android.mailcontact.presentation.contactdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.usecase.GetContactDetails
import ch.protonmail.android.mailcontact.presentation.contactdetails.mapper.ContactDetailsUiModelMapper
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsState
import ch.protonmail.android.mailcontact.presentation.contactdetails.ui.ContactDetailsScreen
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactDetailsViewModel @Inject constructor(
    private val getContactDetails: GetContactDetails,
    private val contactDetailsUiModelMapper: ContactDetailsUiModelMapper,
    private val savedStateHandle: SavedStateHandle,
    observePrimaryUserId: ObservePrimaryUserId
) : ViewModel() {

    private val primaryUserId = observePrimaryUserId().filterNotNull()
    private val mutableState = MutableStateFlow<ContactDetailsState>(ContactDetailsState.Loading)

    val state: StateFlow<ContactDetailsState> = mutableState

    init {
        viewModelScope.launch {
            getContactId()?.let { contactId ->
                getContactDetails(primaryUserId.first(), contactId).fold(
                    ifLeft = { mutableState.emit(ContactDetailsState.Error) },
                    ifRight = {
                        mutableState.emit(
                            ContactDetailsState.Data(
                                contactDetailsUiModelMapper.toUiModel(it)
                            )
                        )
                    }
                )
            } ?: mutableState.emit(ContactDetailsState.Error)
        }
    }

    private fun getContactId() = savedStateHandle.get<String>(ContactDetailsScreen.CONTACT_DETAILS_ID_KEY)?.let {
        ContactId(it)
    }
}
