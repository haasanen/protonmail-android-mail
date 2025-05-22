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

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.mapper.AvatarInformationMapper
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetVisibilityEffect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.model.GroupedContacts
import ch.protonmail.android.mailcontact.domain.usecase.DeleteContact
import ch.protonmail.android.mailcontact.domain.usecase.ObserveGroupedContacts
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.model.ContactEmailListMapper
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupItemUiModelMapper
import ch.protonmail.android.mailcontact.presentation.model.ContactItemUiModelMapper
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModel
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModelMapper
import ch.protonmail.android.mailcontact.presentation.model.GroupedContactListItemsUiModelMapper
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ContactListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val defaultTestContactMetadata = ContactMetadata.Contact(
        id = ContactId("1"),
        avatar = AvatarInformationSample.avatarSample,
        name = "first contact",
        emails = listOf(
            ContactEmail(
                ContactEmailId("contact email id 1"),
                "First contact email",
                true,
                lastUsedTime = 0
            )
        )
    )
    private val defaultTestContactGroupMetadata = ContactMetadata.ContactGroup(
        id = ContactGroupId("label1"),
        name = "first contact group",
        color = "#FF0000",
        members = listOf(
            defaultTestContactMetadata
        )
    )
    private val defaultTestGroupedContacts = GroupedContacts(
        groupedBy = "Label 1",
        contacts = listOf(defaultTestContactMetadata) + listOf(defaultTestContactGroupMetadata)
    )

    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        every { this@mockk.invoke() } returns flowOf(UserIdTestData.userId)
    }
    private val observeGroupedContacts = mockk<ObserveGroupedContacts> {
        coEvery { this@mockk.invoke(any()) } returns flowOf(listOf(defaultTestGroupedContacts).right())
    }

    private val deleteContact = mockk<DeleteContact> {
        coEvery { this@mockk(UserIdTestData.userId, any()) } returns Unit.right()
    }

    private val reducer = ContactListReducer()

    private val colorMapper = ColorMapper()
    private val contactGroupItemUiModelMapper = ContactGroupItemUiModelMapper(colorMapper)
    private val contactEmailListMapper = ContactEmailListMapper()
    private val avatarInformationMapper = AvatarInformationMapper(colorMapper)
    private val contactItemUiModelMapper = ContactItemUiModelMapper(contactEmailListMapper, avatarInformationMapper)
    private val contactListItemUiModelMapper = ContactListItemUiModelMapper(
        contactGroupItemUiModelMapper, contactItemUiModelMapper
    )
    private val groupedContactListItemsUiModelMapper = GroupedContactListItemsUiModelMapper(
        contactListItemUiModelMapper
    )

    private val contactListViewModel by lazy {
        ContactListViewModel(
            observeGroupedContacts,
            deleteContact,
            reducer,
            groupedContactListItemsUiModelMapper,
            observePrimaryUserId
        )
    }

    @Test
    fun `given empty contact list, when init, then emits empty state`() = runTest {
        // Given
        coEvery {
            observeGroupedContacts(userId = UserIdTestData.userId)
        } returns flowOf(emptyList<GroupedContacts>().right())

        // When
        contactListViewModel.state.test {
            // Then
            val actual = awaitItem()
            val expected = ContactListState.Loaded.Empty()

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given contact list, when init, then emits data state`() = runTest {
        // Given
        expectContactsData()

        // When
        contactListViewModel.state.test {
            // Then
            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                groupedContacts = listOf(defaultTestGroupedContacts).map {
                    groupedContactListItemsUiModelMapper.toUiModel(it)
                }
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given error on loading contact list, when init, then emits loading state with error`() = runTest {
        // Given
        coEvery {
            observeGroupedContacts(userId = UserIdTestData.userId)
        } returns flowOf(GetContactError.left())

        // When
        contactListViewModel.state.test {
            // Then
            val actual = awaitItem()
            val expected = ContactListState.Loading(
                errorLoading = Effect.of(TextUiModel(R.string.contact_list_loading_error))
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given contact list, when action open bottom sheet, then emits open state`() = runTest {
        // Given
        expectContactsData()

        // When
        contactListViewModel.state.test {
            awaitItem()

            contactListViewModel.submit(ContactListViewAction.OnOpenBottomSheet)

            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                groupedContacts = listOf(defaultTestGroupedContacts).map {
                    groupedContactListItemsUiModelMapper.toUiModel(it)
                },
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Show),
                bottomSheetType = ContactListState.BottomSheetType.Menu
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given contact list, when action dismiss bottom sheet, then emits open state`() = runTest {
        // Given
        expectContactsData()

        // When
        contactListViewModel.state.test {
            awaitItem()

            contactListViewModel.submit(ContactListViewAction.OnDismissBottomSheet)

            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                groupedContacts = listOf(defaultTestGroupedContacts).map {
                    groupedContactListItemsUiModelMapper.toUiModel(it)
                },
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given contact list, when action dismiss bottom sheet, then emits hide state`() = runTest {
        // Given
        expectContactsData()

        // When
        contactListViewModel.state.test {
            awaitItem()

            contactListViewModel.submit(ContactListViewAction.OnDismissBottomSheet)

            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                groupedContacts = listOf(defaultTestGroupedContacts).map {
                    groupedContactListItemsUiModelMapper.toUiModel(it)
                },
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide)
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `when contact deletion request action is submitted, then shows confirmation dialog`() = runTest {
        // Given
        val contactId = ContactId("1")
        val contactUiModel = contactListItemUiModelMapper.toContactListItemUiModel(
            defaultTestContactMetadata
        ) as ContactListItemUiModel.Contact
        coEvery { deleteContact(UserIdTestData.userId, contactId) } returns Unit.right()
        expectContactsData()

        // When
        contactListViewModel.state.test {
            awaitItem()

            contactListViewModel.submit(ContactListViewAction.OnDeleteContactRequested(contactUiModel))

            // Then
            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                groupedContacts = listOf(defaultTestGroupedContacts).map {
                    groupedContactListItemsUiModelMapper.toUiModel(it)
                },
                showDeleteConfirmDialog = Effect.of(contactUiModel),
                bottomSheetType = ContactListState.BottomSheetType.Menu
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `when contact deletion confirmed, deleteContact is called and confirmation dialog dismissed`() = runTest {
        // Given
        val contactId = ContactId("1")
        val contactUiModel = contactListItemUiModelMapper.toContactListItemUiModel(
            defaultTestContactMetadata
        ) as ContactListItemUiModel.Contact
        coEvery { deleteContact(UserIdTestData.userId, contactId) } returns Unit.right()
        expectContactsData()

        // When
        contactListViewModel.state.test {
            contactListViewModel.submit(
                ContactListViewAction.OnDeleteContactRequested(contactUiModel)
            )

            skipItems(2)

            contactListViewModel.submit(ContactListViewAction.OnDeleteContactConfirmed(contactId))

            // Then
            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                groupedContacts = listOf(defaultTestGroupedContacts).map {
                    groupedContactListItemsUiModelMapper.toUiModel(it)
                },
                showDeleteConfirmDialog = Effect.empty()
            )

            assertEquals(expected, actual)
            coVerify { deleteContact(UserIdTestData.userId, contactId) }

        }
    }

    private fun expectContactsData() {
        coEvery {
            observeGroupedContacts(userId = UserIdTestData.userId)
        } returns flowOf(listOf(defaultTestGroupedContacts).right())
    }
}
