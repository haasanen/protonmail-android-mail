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

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcomposer.domain.repository.ContactsPermissionRepository
import ch.protonmail.android.mailcomposer.presentation.mapper.ContactSuggestionsMapper
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionsField
import ch.protonmail.android.mailcomposer.presentation.model.RecipientUiModel
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsActions
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsStateManager
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.ContactSuggestionQuery
import ch.protonmail.android.mailcontact.domain.usecase.GetContactSuggestions
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.contact.ContactTestData
import ch.protonmail.android.testdata.contact.ContactUiModelTestData
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RecipientsViewModelTest {

    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        every { this@mockk.invoke() } returns flowOf(userId)
    }
    private val getContactSuggestions = mockk<GetContactSuggestions>()
    private val contactsPermissionRepository = mockk<ContactsPermissionRepository> {
        every { this@mockk.observePermissionInteraction() } returns flowOf(true.right())
    }

    private val recipientsStateManager = spyk<RecipientsStateManager>()
    private val contactSuggestionsMapper = spyk<ContactSuggestionsMapper>()

    private val testDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should emit empty suggestions when the search term is empty`() = runTest {
        // Given
        val searchTerm = ""
        val viewModel = viewModel()

        // When
        viewModel.submit(RecipientsActions.UpdateSearchTerm(searchTerm, ContactSuggestionsField.TO))

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.suggestions.isEmpty())
            assertEquals(ContactSuggestionsField.TO, state.suggestionsField)
        }
    }

    @Test
    fun `should update recipients via the recipients state manager when performing the task`() {
        // Given
        val recipients = listOf(
            RecipientUiModel.Valid("aa@bb.cc"),
            RecipientUiModel.Invalid("__")
        )
        val viewModel = viewModel()

        // When
        viewModel.submit(RecipientsActions.UpdateRecipients(recipients, ContactSuggestionsField.BCC))

        // Then
        verify { recipientsStateManager.updateRecipients(recipients, ContactSuggestionsField.BCC) }
        confirmVerified(recipientsStateManager)
    }

    @Test
    fun `should close suggestions when close suggestions is called`() = runTest {
        // Given
        val searchTerm = ""
        val viewModel = viewModel()

        // When
        viewModel.state.test {
            skipItems(1)

            viewModel.submit(RecipientsActions.UpdateSearchTerm(searchTerm, ContactSuggestionsField.TO))
            assertEquals(ContactSuggestionsField.TO, awaitItem().suggestionsField)

            viewModel.submit(RecipientsActions.CloseSuggestions)
            assertEquals(null, awaitItem().suggestionsField)
        }
    }

    @Test
    fun `should emit different fields suggestions when the search field changes`() = runTest {
        // Given
        val searchTerm = ""
        val viewModel = viewModel()

        // When + Then
        viewModel.state.test {
            skipItems(1)

            viewModel.submit(RecipientsActions.UpdateSearchTerm(searchTerm, ContactSuggestionsField.TO))
            assertEquals(ContactSuggestionsField.TO, awaitItem().suggestionsField)

            viewModel.submit(RecipientsActions.UpdateSearchTerm(searchTerm, ContactSuggestionsField.CC))
            assertEquals(ContactSuggestionsField.CC, awaitItem().suggestionsField)

            viewModel.submit(RecipientsActions.UpdateSearchTerm(searchTerm, ContactSuggestionsField.BCC))
            assertEquals(ContactSuggestionsField.BCC, awaitItem().suggestionsField)
        }
    }

    @Test
    fun `should reflect empty suggestions emissions`() = runTest {
        val searchTerm = "searchTerm"

        every { observePrimaryUserId.invoke() } returns flowOf(userId)
        every { contactsPermissionRepository.observePermissionInteraction() } returns flowOf(true.right())
        coEvery {
            getContactSuggestions(userId, ContactSuggestionQuery(searchTerm))
        } returns emptyList<ContactMetadata>().right()
        every { contactSuggestionsMapper.toUiModel(emptyList()) } returns emptyList()

        val viewModel = viewModel()

        // When + Then
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.submit(RecipientsActions.UpdateSearchTerm(searchTerm, ContactSuggestionsField.TO))
            assertTrue(awaitItem().suggestions.isEmpty())
        }
    }

    @Test
    fun `should return contacts suggestions`() = runTest {
        // Given
        val searchTerm = "searchTerm"
        val expectedContacts = listOf(
            ContactTestData.contactSuggestion1,
            ContactTestData.contactGroupSuggestion
        )
        val expectedSuggestions = listOf(
            ContactUiModelTestData.contactSuggestion1,
            ContactUiModelTestData.contactGroupSuggestion
        ).toImmutableList()

        coEvery {
            getContactSuggestions(userId, ContactSuggestionQuery(searchTerm))
        } returns expectedContacts.right()
        coEvery { contactSuggestionsMapper.toUiModel(expectedContacts) } returns expectedSuggestions
        coEvery { contactsPermissionRepository.observePermissionInteraction() } returns flowOf(true.right())

        val viewModel = viewModel()

        // When + Then
        viewModel.state.test {
            skipItems(1)
            viewModel.submit(RecipientsActions.UpdateSearchTerm(searchTerm, ContactSuggestionsField.TO))
            assertEquals(expectedSuggestions, expectMostRecentItem().suggestions)
        }
    }

    @Test
    fun `should emit permission request event upon user action`() = runTest {
        // Given
        val viewModel = viewModel()

        // When
        viewModel.submit(RecipientsActions.RequestContactsPermission)

        // Then
        viewModel.state.test {
            assertEquals(Effect.of(Unit), awaitItem().requestContactsPermission)
        }
    }

    @Test
    fun `should close suggestions when permission is interacted with and no suggestions are displayed`() = runTest {
        // Given
        coEvery {
            getContactSuggestions(userId, ContactSuggestionQuery(any()))
        } returns emptyList<ContactMetadata>().right()

        every {
            contactsPermissionRepository.observePermissionInteraction()
        } returns flowOf(DataError.Local.NoDataCached.left())

        coEvery { contactsPermissionRepository.trackPermissionInteraction() } just runs

        val viewModel = viewModel()

        // When
        viewModel.state.test {
            viewModel.submit(RecipientsActions.MarkContactsPermissionInteraction)
            assertNull(awaitItem().suggestionsField)
            expectNoEvents()
        }
    }

    private fun viewModel() = RecipientsViewModel(
        observePrimaryUserId,
        getContactSuggestions,
        contactSuggestionsMapper,
        contactsPermissionRepository,
        recipientsStateManager
    )

    private companion object {

        val userId = UserId("user-id")
    }
}
