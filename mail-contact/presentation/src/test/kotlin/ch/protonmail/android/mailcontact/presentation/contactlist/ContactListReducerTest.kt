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

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetVisibilityEffect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.model.GroupedContactListItemsUiModel
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class ContactListReducerTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val reducer = ContactListReducer()

    @Test
    fun `should produce the expected state`() = with(testInput) {
        val actualState = reducer.newStateFrom(currentState, event)

        assertEquals(expectedState, actualState, testName)
    }

    companion object {

        private val loadedContactListItemUiModels1 = listOf(
            ContactListItemUiModel.Contact(
                id = ContactId("1"),
                name = "first contact",
                emailSubtext = TextUiModel("firstcontact+alias@protonmail.com"),
                avatar = AvatarUiModel.ParticipantAvatar(
                    "FC", "firstcontact+alias@protonmail.com",
                    null, Color.Unspecified
                )
            ),
            ContactListItemUiModel.ContactGroup(
                id = ContactGroupId("Id2"),
                name = "Name 2",
                memberCount = 3,
                color = Color.Red
            )
        )

        private val loadedContactListItemUiModels2 = listOf(
            ContactListItemUiModel.ContactGroup(
                id = ContactGroupId("Id1"),
                name = "Name 1",
                memberCount = 2,
                color = Color.Blue
            ),
            ContactListItemUiModel.Contact(
                id = ContactId("1.1"),
                name = "first contact bis",
                emailSubtext = TextUiModel("firstcontactbis@protonmail.com"),
                avatar = AvatarUiModel.ParticipantAvatar(
                    "FB", "firstcontactbis@protonmail.com",
                    null, Color.Unspecified
                )
            )
        )

        private val groupedContacts = listOf(
            GroupedContactListItemsUiModel(
                contacts = loadedContactListItemUiModels1
            ),
            GroupedContactListItemsUiModel(
                contacts = loadedContactListItemUiModels2
            )
        )
        private val emptyLoadingState = ContactListState.Loading()
        private val errorLoadingState = ContactListState.Loading(
            errorLoading = Effect.of(TextUiModel(R.string.contact_list_loading_error))
        )
        private val emptyLoadedState = ContactListState.Loaded.Empty()
        private val dataLoadedState = ContactListState.Loaded.Data(groupedContacts = groupedContacts)

        private val transitionsFromLoadingState = listOf(
            TestInput(
                currentState = emptyLoadingState,
                event = ContactListEvent.ContactListLoaded(groupedContacts),
                expectedState = dataLoadedState
            ),
            TestInput(
                currentState = emptyLoadingState,
                event = ContactListEvent.ContactListLoaded(emptyList()),
                expectedState = ContactListState.Loaded.Empty()
            ),
            TestInput(
                currentState = emptyLoadingState,
                event = ContactListEvent.ErrorLoadingContactList,
                expectedState = errorLoadingState
            ),
            TestInput(
                currentState = emptyLoadingState,
                event = ContactListEvent.OpenBottomSheet,
                expectedState = emptyLoadingState
            ),
            TestInput(
                currentState = emptyLoadingState,
                event = ContactListEvent.OpenContactSearch,
                expectedState = emptyLoadingState
            ),
            TestInput(
                currentState = emptyLoadingState,
                event = ContactListEvent.DismissBottomSheet,
                expectedState = emptyLoadingState
            )
        )

        private val transitionsFromEmptyLoadedState = listOf(
            TestInput(
                currentState = emptyLoadedState,
                event = ContactListEvent.ContactListLoaded(groupedContacts),
                expectedState = dataLoadedState
            ),
            TestInput(
                currentState = emptyLoadedState.copy(
                    bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
                ),
                event = ContactListEvent.ContactListLoaded(emptyList()),
                expectedState = ContactListState.Loaded.Empty().copy(
                    bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
                )
            ),
            TestInput(
                currentState = emptyLoadedState,
                event = ContactListEvent.ErrorLoadingContactList,
                expectedState = errorLoadingState
            ),
            TestInput(
                currentState = emptyLoadedState,
                event = ContactListEvent.OpenBottomSheet,
                expectedState = emptyLoadedState.copy(
                    bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Show)
                )
            ),
            TestInput(
                currentState = emptyLoadedState,
                event = ContactListEvent.OpenContactSearch,
                expectedState = emptyLoadedState.copy(
                    openContactSearch = Effect.of(true)
                )
            ),
            TestInput(
                currentState = emptyLoadedState,
                event = ContactListEvent.DismissBottomSheet,
                expectedState = emptyLoadedState.copy(
                    bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
                )
            )
        )

        private val transitionsFromDataLoadedState = listOf(
            TestInput(
                currentState = dataLoadedState,
                event = ContactListEvent.ContactListLoaded(groupedContacts),
                expectedState = dataLoadedState
            ),
            TestInput(
                currentState = dataLoadedState,
                event = ContactListEvent.ContactListLoaded(emptyList()),
                expectedState = ContactListState.Loaded.Empty()
            ),
            TestInput(
                currentState = dataLoadedState,
                event = ContactListEvent.ErrorLoadingContactList,
                expectedState = errorLoadingState
            ),
            TestInput(
                currentState = dataLoadedState,
                event = ContactListEvent.OpenBottomSheet,
                expectedState = dataLoadedState.copy(
                    bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Show),
                    bottomSheetType = ContactListState.BottomSheetType.Menu
                )
            ),
            TestInput(
                currentState = dataLoadedState,
                event = ContactListEvent.OpenContactSearch,
                expectedState = dataLoadedState.copy(
                    openContactSearch = Effect.of(true)
                )
            ),
            TestInput(
                currentState = dataLoadedState,
                event = ContactListEvent.DismissBottomSheet,
                expectedState = dataLoadedState.copy(
                    bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
                )
            )
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = (
            transitionsFromLoadingState +
                transitionsFromEmptyLoadedState +
                transitionsFromDataLoadedState
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
        val currentState: ContactListState,
        val event: ContactListEvent,
        val expectedState: ContactListState
    )
}
