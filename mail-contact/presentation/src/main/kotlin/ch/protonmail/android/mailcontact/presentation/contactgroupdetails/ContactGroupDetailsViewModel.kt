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

package ch.protonmail.android.mailcontact.presentation.contactgroupdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.usecase.GetContactGroupDetails
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactGroupDetailsViewModel @Inject constructor(
    private val contactGroupDetailsUiModelMapper: ContactGroupDetailsUiModelMapper,
    private val getContactGroupDetails: GetContactGroupDetails,
    private val savedStateHandle: SavedStateHandle,
    observePrimaryUserId: ObservePrimaryUserId
) : ViewModel() {

    private val primaryUserId = observePrimaryUserId().filterNotNull()
    private val mutableState = MutableStateFlow<ContactGroupDetailsState>(ContactGroupDetailsState.Loading)

    val state: StateFlow<ContactGroupDetailsState> = mutableState

    init {
        viewModelScope.launch {
            getContactGroupId()?.let { contactGroupId ->
                getContactGroupDetails(primaryUserId.first(), contactGroupId).fold(
                    ifLeft = { mutableState.emit(ContactGroupDetailsState.Error) },
                    ifRight = {
                        mutableState.emit(
                            ContactGroupDetailsState.Data(
                                contactGroupDetailsUiModelMapper.toUiModel(it)
                            )
                        )
                    }
                )
            } ?: mutableState.emit(ContactGroupDetailsState.Error)
        }
    }

    private fun getContactGroupId() = savedStateHandle.get<String>(
        ContactGroupDetailsScreen.CONTACT_GROUP_DETAILS_ID_KEY
    )?.let {
        ContactGroupId(it)
    }
}
