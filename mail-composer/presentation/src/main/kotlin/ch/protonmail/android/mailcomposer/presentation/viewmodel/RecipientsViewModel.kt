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

package ch.protonmail.android.mailcomposer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcomposer.domain.repository.ContactsPermissionRepository
import ch.protonmail.android.mailcomposer.presentation.mapper.ContactSuggestionsMapper
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionsField
import ch.protonmail.android.mailcomposer.presentation.model.RecipientUiModel
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsStateManager
import ch.protonmail.android.mailcomposer.presentation.viewmodel.ComposerViewModel.Companion.maxContactAutocompletionCount
import ch.protonmail.android.mailcontact.domain.model.ContactSuggestionQuery
import ch.protonmail.android.mailcontact.domain.usecase.GetContactSuggestions
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel(assistedFactory = RecipientsViewModel.Factory::class)
internal class RecipientsViewModel @AssistedInject constructor(
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val getContactSuggestions: GetContactSuggestions,
    private val contactSuggestionsMapper: ContactSuggestionsMapper,
    private val contactsPermissionRepository: ContactsPermissionRepository,
    @Assisted val recipientsStateManager: RecipientsStateManager
) : ViewModel() {

    private val searchTerm = MutableStateFlow("")

    private val mutableContactSuggestionsFieldFlow = MutableStateFlow<ContactSuggestionsField?>(null)
    val contactSuggestionsFieldFlow = mutableContactSuggestionsFieldFlow.asStateFlow()

    val contactsSuggestions = observeContactsSuggestions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val contactsPermissionDenied = contactsPermissionRepository.observePermissionDenied()
        .map { it.getOrNull() == true }

    fun denyContactsPermission() {
        viewModelScope.launch { contactsPermissionRepository.trackPermissionDenied() }
    }

    fun updateSearchTerm(term: String, contactSuggestionsField: ContactSuggestionsField) {
        searchTerm.update { term }
        mutableContactSuggestionsFieldFlow.update { contactSuggestionsField }
    }

    fun updateRecipients(values: List<RecipientUiModel>, type: ContactSuggestionsField) {
        recipientsStateManager.updateRecipients(values, type)
    }

    fun closeSuggestions() {
        mutableContactSuggestionsFieldFlow.update { null }
    }

    @OptIn(FlowPreview::class)
    private fun observeContactsSuggestions(): Flow<List<ContactSuggestionUiModel>> = combine(
        primaryUserId(),
        searchTerm
    ) { userId, searchTerm ->

        if (searchTerm.isBlank()) return@combine emptyList<ContactSuggestionUiModel>()

        return@combine getContactSuggestions(userId, ContactSuggestionQuery(searchTerm)).fold(
            ifLeft = {
                return@fold emptyList<ContactSuggestionUiModel>()
            },
            ifRight = { contacts ->
                val contactsLimited = contacts.take(maxContactAutocompletionCount)
                return@fold contactSuggestionsMapper.toUiModel(contactsLimited)
            }
        )
    }.debounce(SuggestionsDebounce)

    private fun primaryUserId() = observePrimaryUserId.invoke().filterNotNull()

    @AssistedFactory
    interface Factory {

        fun create(recipientsStateManager: RecipientsStateManager): RecipientsViewModel
    }

    private companion object {

        val SuggestionsDebounce = 200.milliseconds
    }
}
