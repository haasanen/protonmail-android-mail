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

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.testdata.contact.ContactGroupIdSample
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class ContactSearchReducerTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val reducer = ContactSearchReducer()

    @Test
    fun `should produce the expected state`() = with(testInput) {
        val actualState = reducer.newStateFrom(currentState, event)

        assertEquals(expectedState, actualState, testName)
    }

    companion object {

        private val searchResultsContacts = listOf(
            ContactListItemUiModel.Contact(
                id = ContactId("result 1 ID"),
                name = "result 1 name",
                emailSubtext = TextUiModel.Text("result1@proton.me"),
                avatar = AvatarUiModel.ParticipantAvatar(
                    "R1", "result1@proton.me",
                    null, Color.Unspecified
                )
            ),
            ContactListItemUiModel.Contact(
                id = ContactId("result 2 ID"),
                name = "result 2 name",
                emailSubtext = TextUiModel.Text("result2@proton.me"),
                avatar = AvatarUiModel.ParticipantAvatar(
                    "R2", "result2@proton.me",
                    null, Color.Unspecified
                )
            ),
            // Added ContactGroup to the search results
            ContactListItemUiModel.ContactGroup(
                id = ContactGroupIdSample.School,
                name = "result 3 group name",
                memberCount = 10,
                color = Color(1f, 1f, 1f)
            )
        ).toImmutableList()

        private val emptyState = ContactSearchState(
            contactUiModels = null,
            searchValue = ""
        )
        private val noResultsState = ContactSearchState(
            contactUiModels = emptyList<ContactListItemUiModel>().toImmutableList(),
            searchValue = ""
        )
        private val someResultsContactsState = ContactSearchState(
            contactUiModels = searchResultsContacts,
            searchValue = ""
        )

        // Updated tests to handle ContactsLoaded and ContactsCleared events only

        private val transitionsFromEmptyState = listOf(
            TestInput(
                currentState = emptyState,
                event = ContactSearchEvent.ContactsLoaded(
                    contacts = emptyList()
                ),
                expectedState = noResultsState
            ),
            TestInput(
                currentState = emptyState,
                event = ContactSearchEvent.ContactsLoaded(
                    contacts = searchResultsContacts
                ),
                expectedState = someResultsContactsState
            ),
            TestInput(
                currentState = emptyState,
                event = ContactSearchEvent.ContactsCleared,
                expectedState = emptyState
            )
        )

        private val transitionsFromNoResultsState = listOf(
            TestInput(
                currentState = noResultsState,
                event = ContactSearchEvent.ContactsLoaded(
                    contacts = emptyList()
                ),
                expectedState = noResultsState
            ),
            TestInput(
                currentState = noResultsState,
                event = ContactSearchEvent.ContactsLoaded(
                    contacts = searchResultsContacts
                ),
                expectedState = someResultsContactsState
            ),
            TestInput(
                currentState = noResultsState,
                event = ContactSearchEvent.ContactsCleared,
                expectedState = emptyState
            )
        )

        private val transitionsFromSomeResultsState = listOf(
            TestInput(
                currentState = someResultsContactsState,
                event = ContactSearchEvent.ContactsLoaded(
                    contacts = emptyList()
                ),
                expectedState = noResultsState
            ),
            TestInput(
                currentState = someResultsContactsState,
                event = ContactSearchEvent.ContactsLoaded(
                    contacts = searchResultsContacts.take(1)
                ),
                expectedState = someResultsContactsState.copy(
                    contactUiModels = searchResultsContacts.take(1).toImmutableList()
                )
            ),
            TestInput(
                currentState = someResultsContactsState,
                event = ContactSearchEvent.ContactsCleared,
                expectedState = emptyState
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = (
            transitionsFromEmptyState +
                transitionsFromNoResultsState +
                transitionsFromSomeResultsState
            )
            .map { testInput ->
                val testName = """
                    Current state: ${testInput.currentState}
                    Event: ${testInput.event}
                    Next state: ${testInput.expectedState}
                    
                """.trimIndent()
                arrayOf(testName, testInput)
            }
    }

    data class TestInput(
        val currentState: ContactSearchState,
        val event: ContactSearchEvent,
        val expectedState: ContactSearchState
    )
}

