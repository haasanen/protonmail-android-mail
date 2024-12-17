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

package ch.protonmail.android.mailcontact.presentation.managemembers

import app.cash.turbine.test
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcontact.domain.usecase.ObserveContacts
import ch.protonmail.android.mailcontact.presentation.model.ManageMembersUiModel
import ch.protonmail.android.mailcontact.presentation.model.ManageMembersUiModelMapper
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
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ManageMembersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(TestDispatcherProvider().Main)

    private val defaultTestContact = ContactMetadata.Contact(
        id = ContactId("ContactId1"),
        name = "John Doe",
        avatar = AvatarInformationSample.avatarSample,
        emails = listOf(
            ContactEmail(
                ContactEmailId("ContactEmailId1"),
                "johndoe+alias@protonmail.com",
                true,
                lastUsedTime = 0
            ),
            ContactEmail(
                ContactEmailId("ContactEmailId2"),
                "janedoe@protonmail.com",
                true,
                lastUsedTime = 0
            )
        )
    )
    private val defaultTestSelectedContactIds = listOf(ContactId("ContactId2"))
    private val defaultTestManageMembersUiModel = listOf(
        ManageMembersUiModel(
            id = ContactId("ContactId1"),
            name = "John Doe",
            email = "johndoe+alias@protonmail.com",
            initials = "JD",
            isSelected = false,
            isDisplayed = true
        ),
        ManageMembersUiModel(
            id = ContactId("ContactId2"),
            name = "Jane Doe",
            email = "janedoe@protonmail.com",
            initials = "JD",
            isSelected = true,
            isDisplayed = true
        )
    )

    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        every { this@mockk.invoke() } returns flowOf(UserIdTestData.userId)
    }

    private val manageMembersUiModelMapperMock = mockk<ManageMembersUiModelMapper>()
    private val observeContactsMock = mockk<ObserveContacts>()

    private val reducer = ManageMembersReducer()

    private val manageMembersViewModel by lazy {
        ManageMembersViewModel(
            observeContactsMock,
            reducer,
            manageMembersUiModelMapperMock,
            observePrimaryUserId
        )
    }

    @Test
    fun `given contact list, when init, then emits data state`() = runTest {
        // Given
        val contacts = listOf(defaultTestContact)
        expectContactsData(contacts)
        expectUiModelMapper(contacts, defaultTestSelectedContactIds, defaultTestManageMembersUiModel)

        // When
        manageMembersViewModel.state.test {
            awaitItem()

            manageMembersViewModel.initViewModelWithData(defaultTestSelectedContactIds)

            // Then
            val actual = awaitItem()
            val expected = ManageMembersState.Data(
                members = defaultTestManageMembersUiModel
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given contact list, when on done click, then emits on done state`() = runTest {
        // Given
        val contacts = listOf(defaultTestContact)
        expectContactsData(contacts)
        expectUiModelMapper(contacts, defaultTestSelectedContactIds, defaultTestManageMembersUiModel)

        // When
        manageMembersViewModel.state.test {
            awaitItem()

            manageMembersViewModel.initViewModelWithData(defaultTestSelectedContactIds)

            awaitItem()

            manageMembersViewModel.submit(ManageMembersViewAction.OnDoneClick)

            // Then
            val actual = awaitItem()
            val expected = ManageMembersState.Data(
                members = defaultTestManageMembersUiModel,
                onDone = Effect.of(listOf(ContactId("ContactId2")))
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given contact list, when on member click, then emits updated state`() = runTest {
        // Given
        val contacts = listOf(defaultTestContact)
        expectContactsData(contacts)
        expectUiModelMapper(contacts, defaultTestSelectedContactIds, defaultTestManageMembersUiModel)

        // When
        manageMembersViewModel.state.test {
            awaitItem()

            manageMembersViewModel.initViewModelWithData(defaultTestSelectedContactIds)

            awaitItem()

            manageMembersViewModel.submit(ManageMembersViewAction.OnMemberClick(ContactId("ContactId1")))

            // Then
            val actual = awaitItem()
            val updatedMembers = defaultTestManageMembersUiModel.toMutableList().apply {
                this[0] = this[0].copy(isSelected = true)
            }
            val expected = ManageMembersState.Data(
                members = updatedMembers
            )

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `given contact list, when on search value change, then emits updated state`() = runTest {
        // Given
        val contacts = listOf(defaultTestContact)
        expectContactsData(contacts)
        expectUiModelMapper(contacts, defaultTestSelectedContactIds, defaultTestManageMembersUiModel)

        // When
        manageMembersViewModel.state.test {
            awaitItem()

            manageMembersViewModel.initViewModelWithData(defaultTestSelectedContactIds)

            awaitItem()

            manageMembersViewModel.submit(ManageMembersViewAction.OnSearchValueChanged("John"))

            // Then
            val actual = awaitItem()
            val updatedMembers = defaultTestManageMembersUiModel.toMutableList().apply {
                this[1] = this[1].copy(isDisplayed = false)
            }
            val expected = ManageMembersState.Data(
                members = updatedMembers
            )

            assertEquals(expected, actual)
        }
    }

    private fun expectContactsData(contacts: List<ContactMetadata.Contact>) {
        coEvery {
            observeContactsMock(userId = UserIdTestData.userId)
        } returns flowOf(contacts.right())
    }

    private fun expectUiModelMapper(
        contacts: List<ContactMetadata.Contact>,
        selectedContactIds: List<ContactId>,
        manageMembersUiModel: List<ManageMembersUiModel>
    ) {
        every {
            manageMembersUiModelMapperMock.toManageMembersUiModelList(
                contacts = contacts,
                selectedContactIds = selectedContactIds
            )
        } returns manageMembersUiModel
    }
}
