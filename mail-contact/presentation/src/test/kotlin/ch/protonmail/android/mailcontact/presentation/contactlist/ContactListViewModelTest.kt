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
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.mailcommon.domain.usecase.IsPaidUser
import ch.protonmail.android.mailcommon.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.mapper.AvatarInformationMapper
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailcontact.domain.usecase.featureflags.IsContactGroupsCrudEnabled
import ch.protonmail.android.mailcontact.domain.usecase.featureflags.IsContactSearchEnabled
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.model.ContactGroupItemUiModelMapper
import ch.protonmail.android.mailcontact.presentation.model.ContactListItemUiModelMapper
import ch.protonmail.android.maillabel.presentation.getHexStringFromColor
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GroupedContacts
import ch.protonmail.android.mailcontact.domain.usecase.ObserveGroupedContacts
import ch.protonmail.android.mailcontact.presentation.model.ContactEmailListMapper
import ch.protonmail.android.mailcontact.presentation.model.ContactItemUiModelMapper
import ch.protonmail.android.maillabel.domain.model.Label
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelType
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

class ContactListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val defaultTestContactGroupLabel = Label(
        labelId = LabelId("LabelId1"),
        parentId = null,
        name = "Label 1",
        type = LabelType.ContactGroup,
        path = "",
        color = Color.Red.getHexStringFromColor(),
        order = 0,
        isNotified = null,
        isExpanded = null,
        isSticky = null
    )
    private val defaultTestContactMetadata = ContactMetadata.Contact(
        userId = UserIdTestData.userId,
        id = ContactId("1"),
        avatar = AvatarInformationSample.avatarSample,
        name = "first contact",
        emails = listOf(
            ContactEmail(
                UserIdTestData.userId,
                ContactEmailId("contact email id 1"),
                "First contact email",
                "firstcontact+alias@protonmail.com",
                0,
                0,
                ContactId("1"),
                "firstcontact@protonmail.com",
                listOf(defaultTestContactGroupLabel.labelId.id),
                true,
                lastUsedTime = 0
            )
        )
    )
    private val defaultTestContactGroupMetadata = ContactMetadata.ContactGroup(
        labelId = LabelId("label1"),
        name = "first contact group",
        color = "#FF0000",
        emails = listOf(
            ContactEmail(
                UserIdTestData.userId,
                ContactEmailId("contact email id 1"),
                "First contact email",
                "firstcontact+alias@protonmail.com",
                0,
                0,
                ContactId("1"),
                "firstcontact@protonmail.com",
                listOf(defaultTestContactGroupLabel.labelId.id),
                true,
                lastUsedTime = 0
            )
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
    private val isContactGroupsCrudEnabledMock = mockk<IsContactGroupsCrudEnabled> {
        every { this@mockk() } returns true
    }
    private val isContactSearchEnabledMock = mockk<IsContactSearchEnabled> {
        every { this@mockk() } returns true
    }

    private val reducer = ContactListReducer()

    private val isPaidUser = mockk<IsPaidUser>()
    private val colorMapper = ColorMapper()
    private val contactGroupItemUiModelMapper = ContactGroupItemUiModelMapper(colorMapper)
    private val contactEmailListMapper = ContactEmailListMapper()
    private val avatarInformationMapper = AvatarInformationMapper(colorMapper)
    private val contactItemUiModelMapper = ContactItemUiModelMapper(contactEmailListMapper, avatarInformationMapper)
    private val contactListItemUiModelMapper = ContactListItemUiModelMapper(
        contactGroupItemUiModelMapper, contactItemUiModelMapper
    )

    private val contactListViewModel by lazy {
        ContactListViewModel(
            observeGroupedContacts,
            isPaidUser,
            reducer,
            contactListItemUiModelMapper,
            isContactGroupsCrudEnabledMock,
            isContactSearchEnabledMock,
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
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                isContactGroupsCrudEnabled = true,
                isContactSearchEnabled = true
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
    fun `when feature flag IsContactGroupsCrudEnabled is false then emit appropriate event`() = runTest {
        // Given
        expectContactsData()
        every { isContactGroupsCrudEnabledMock.invoke() } returns false

        // When
        contactListViewModel.state.test {
            // Then
            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                isContactGroupsCrudEnabled = false,
                isContactSearchEnabled = true
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `when feature flag IsContactSearchEnabled is false then emit appropriate event`() = runTest {
        // Given
        expectContactsData()
        every { isContactSearchEnabledMock.invoke() } returns false

        // When
        contactListViewModel.state.test {
            // Then
            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                isContactGroupsCrudEnabled = true,
                isContactSearchEnabled = false
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
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Show),
                isContactGroupsCrudEnabled = true,
                isContactSearchEnabled = true
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
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide),
                isContactGroupsCrudEnabled = true,
                isContactSearchEnabled = true
            )

            kotlin.test.assertEquals(expected, actual)
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
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide),
                isContactGroupsCrudEnabled = true,
                isContactSearchEnabled = true
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given contact list, when action new contact, then emits open contact form state`() = runTest {
        // Given
        expectContactsData()

        // When
        contactListViewModel.state.test {
            awaitItem()

            contactListViewModel.submit(ContactListViewAction.OnNewContactClick)

            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide),
                openContactForm = Effect.of(Unit),
                isContactGroupsCrudEnabled = true,
                isContactSearchEnabled = true
            )

            assertEquals(expected, actual)
        }
    }


    @Test
    fun `given paid user contact list, when action new contact group, then emits open group form state`() = runTest {
        // Given
        expectContactsData()
        expectPaidUser(true)

        // When
        contactListViewModel.state.test {
            awaitItem()

            contactListViewModel.submit(ContactListViewAction.OnNewContactGroupClick)

            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide),
                openContactGroupForm = Effect.of(Unit),
                isContactGroupsCrudEnabled = true,
                isContactSearchEnabled = true
            )

            kotlin.test.assertEquals(expected, actual)
        }
    }


    @Test
    fun `given free user, when action new contact group, then emits subscription upgrade required error`() = runTest {
        // Given
        expectContactsData()
        expectPaidUser(false)

        // When
        contactListViewModel.state.test {
            awaitItem()

            contactListViewModel.submit(ContactListViewAction.OnNewContactGroupClick)

            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide),
                subscriptionError = Effect.of(TextUiModel.TextRes(R.string.contact_group_form_subscription_error)),
                isContactGroupsCrudEnabled = true,
                isContactSearchEnabled = true
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given contact list, when action import contact, then emits open import state`() = runTest {
        // Given
        expectContactsData()

        // When
        contactListViewModel.state.test {
            awaitItem()

            contactListViewModel.submit(ContactListViewAction.OnImportContactClick)

            val actual = awaitItem()
            val expected = ContactListState.Loaded.Data(
                contacts = contactListItemUiModelMapper.toContactListItemUiModel(
                    listOf(defaultTestGroupedContacts)
                ),
                bottomSheetVisibilityEffect = Effect.of(BottomSheetVisibilityEffect.Hide),
                openImportContact = Effect.of(Unit),
                isContactGroupsCrudEnabled = true,
                isContactSearchEnabled = true
            )

            assertEquals(expected, actual)
        }
    }


    private fun expectContactsData() {
        coEvery {
            observeGroupedContacts(userId = UserIdTestData.userId)
        } returns flowOf(listOf(defaultTestGroupedContacts).right())
    }

    private fun expectPaidUser(value: Boolean) {
        coEvery { isPaidUser(userId = UserIdTestData.userId) } returns value.right()
    }
}
