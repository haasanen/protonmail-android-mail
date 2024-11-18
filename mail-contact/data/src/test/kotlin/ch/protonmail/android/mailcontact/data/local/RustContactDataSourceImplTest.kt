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

package ch.protonmail.android.mailcontact.data.local

import arrow.core.getOrElse
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcontact.data.mapper.ContactGroupItemMapper
import ch.protonmail.android.mailcontact.data.mapper.ContactItemMapper
import ch.protonmail.android.mailcontact.data.mapper.ContactItemTypeMapper
import ch.protonmail.android.mailcontact.data.mapper.GroupedContactsMapper
import ch.protonmail.android.mailcontact.data.usecase.CreateRustContactWatcher
import ch.protonmail.android.mailcontact.data.usecase.RustDeleteContact
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.GetContactError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.contact.rust.LocalContactTestData
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.ContactsLiveQueryCallback
import uniffi.proton_mail_uniffi.MailSessionException
import kotlin.test.assertEquals
import uniffi.proton_mail_uniffi.MailUserSession
import uniffi.proton_mail_uniffi.WatchedContactList
import kotlin.test.Test

class RustContactDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val mockWatcher = mockk<WatchedContactList>(relaxed = true)

    private val userSessionRepository = mockk<UserSessionRepository>()
    private val createRustContactWatcher = mockk<CreateRustContactWatcher>()
    private val rustDeleteContact = mockk<RustDeleteContact>()
    private val contactListUpdatedCallbackSlot = slot<ContactsLiveQueryCallback>()

    private val contactItemMapper = ContactItemMapper()
    private val contactGroupItemMapper = ContactGroupItemMapper()
    private val contactItemTypeMapper = ContactItemTypeMapper(contactItemMapper, contactGroupItemMapper)
    private val groupedContactsMapper = GroupedContactsMapper(contactItemTypeMapper)

    private val rustContactDataSource = RustContactDataSourceImpl(
        userSessionRepository,
        groupedContactsMapper,
        createRustContactWatcher,
        rustDeleteContact,
        testCoroutineScope
    )

    @Test
    fun `observing all contacts emits contact metadata list when grouped contacts are loaded`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val session = mockk<MailUserSession>()
        val localGroupedContactList = listOf(
            LocalContactTestData.groupedContactsByA, LocalContactTestData.groupedContactsByB
        )
        val expectedContacts = localGroupedContactList
            .map { groupedContactsMapper.toGroupedContacts(it) }
            .flatMap { it.contacts }

        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustContactWatcher(session, capture(contactListUpdatedCallbackSlot)) } returns mockWatcher
        every { mockWatcher.contactList } returns localGroupedContactList

        // When
        val result = rustContactDataSource.observeAllContacts(userId).first()

        // Then
        assertTrue(result.isRight())
        assertEquals(expectedContacts.right(), result)
    }

    @Test
    fun `observeAllContacts should return error when session is null`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = rustContactDataSource.observeAllContacts(userId).first()

        // Then
        assertTrue(result.isLeft())
        assertEquals(GetContactError, result.swap().getOrElse { emptyList<ContactMetadata>() })
    }

    @Test
    fun `observeAllGroupedContacts should initialize watcher and emit contact groups`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val session = mockk<MailUserSession>()
        val localGroupedContactList = listOf(
            LocalContactTestData.groupedContactsByA, LocalContactTestData.groupedContactsByB
        )
        val expectedGroupedContactList = localGroupedContactList
            .map { groupedContactsMapper.toGroupedContacts(it) }

        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustContactWatcher(session, capture(contactListUpdatedCallbackSlot)) } returns mockWatcher
        every { mockWatcher.contactList } returns localGroupedContactList

        // When
        val result = rustContactDataSource.observeAllGroupedContacts(userId).first()

        // Then
        assertTrue(result.isRight())
        assertEquals(expectedGroupedContactList.right(), result)
    }

    @Test
    fun `deleteContact should call rustDeleteContact when session is available`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val contactId = LocalContactTestData.contactId1
        val session = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { rustDeleteContact(session, contactId) } just Runs

        // When
        val result = rustContactDataSource.deleteContact(userId, contactId)

        // Then
        assertTrue(result.isRight())
        coVerify { rustDeleteContact(session, contactId) }
    }

    @Test
    fun `deleteContact should return error when session is null`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val contactId = LocalContactTestData.contactId1
        coEvery { userSessionRepository.getUserSession(userId) } returns null

        // When
        val result = rustContactDataSource.deleteContact(userId, contactId)

        // Then
        assertTrue(result.isLeft())
        assertEquals(DataError.Local.Unknown, result.swap().getOrNull())
    }

    @Test
    fun `deleteContact should return error when MailSessionException is thrown`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val contactId = LocalContactTestData.contactId1
        val session = mockk<MailUserSession>()
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { rustDeleteContact(session, contactId) } throws MailSessionException.ContactException("")

        // When
        val result = rustContactDataSource.deleteContact(userId, contactId)

        // Then
        assertTrue(result.isLeft())
        assertEquals(DataError.Local.Unknown, result.swap().getOrNull())
    }

    @Test
    fun `should create watcher on first invocation`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val session = mockk<MailUserSession>()
        val callbackSlot = slot<ContactsLiveQueryCallback>()
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustContactWatcher(session, capture(callbackSlot)) } returns mockWatcher

        // When
        rustContactDataSource.observeAllGroupedContacts(userId).first()

        // Then
        coVerify(exactly = 1) { createRustContactWatcher(session, any()) }
    }

    @Test
    fun `should not recreate watcher if already exists`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val session = mockk<MailUserSession>()
        val callbackSlot = slot<ContactsLiveQueryCallback>()
        coEvery { userSessionRepository.getUserSession(userId) } returns session
        coEvery { createRustContactWatcher(session, capture(callbackSlot)) } returns mockWatcher

        // First call to initialize the watcher
        rustContactDataSource.observeAllGroupedContacts(userId).first()

        // Second call should not recreate the watcher
        rustContactDataSource.observeAllGroupedContacts(userId).first()

        // Then
        coVerify(exactly = 1) { createRustContactWatcher(session, any()) }
    }
}

