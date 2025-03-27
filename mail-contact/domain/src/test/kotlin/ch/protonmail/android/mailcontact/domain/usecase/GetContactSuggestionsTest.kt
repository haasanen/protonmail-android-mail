package ch.protonmail.android.mailcontact.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.model.ContactSuggestionQuery
import ch.protonmail.android.mailcontact.domain.model.DeviceContact
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.mailcontact.domain.repository.DeviceContactsRepository
import ch.protonmail.android.testdata.contact.ContactTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetContactSuggestionsTest {

    private val deviceContactsRepository = mockk<DeviceContactsRepository>()

    private val contactsRepository = mockk<ContactRepository>()

    private val getContactSuggestions = GetContactSuggestions(
        contactsRepository,
        deviceContactsRepository
    )

    @Test
    fun `call get contact suggestions passing device contacts`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val query = ContactSuggestionQuery("test")
        coEvery { deviceContactsRepository.getDeviceContacts(query.value) } returns deviceContacts.right()
        coEvery {
            contactsRepository.getContactSuggestions(userId, deviceContacts, query)
        } returns emptyList<ContactMetadata>().right()

        // When
        getContactSuggestions(userId, query)

        // Then
        coVerify { contactsRepository.getContactSuggestions(userId, deviceContacts, query) }
    }

    @Test
    fun `default to empty list when failing to get device contacts`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val query = ContactSuggestionQuery("test")
        val error = DeviceContactsRepository.DeviceContactsErrors.PermissionDenied
        val deviceContactsFallback = emptyList<DeviceContact>()
        coEvery { deviceContactsRepository.getDeviceContacts(query.value) } returns error.left()
        coEvery {
            contactsRepository.getContactSuggestions(userId, deviceContactsFallback, query)
        } returns emptyList<ContactMetadata>().right()

        // When
        getContactSuggestions(userId, query)

        // Then
        coVerify { contactsRepository.getContactSuggestions(userId, deviceContactsFallback, query) }
    }

    @Test
    fun `returns contact suggestions when repository succeeds`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val query = ContactSuggestionQuery("test")
        val expected = listOf(ContactTestData.contactSuggestion, ContactTestData.contactGroupSuggestion)
        coEvery { deviceContactsRepository.getDeviceContacts(query.value) } returns deviceContacts.right()
        coEvery {
            contactsRepository.getContactSuggestions(userId, deviceContacts, query)
        } returns expected.right()

        // When
        val actual = getContactSuggestions(userId, query)

        // Then
        assertEquals(expected.right(), actual)
    }

    @Test
    fun `returns error when repository fails`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val query = ContactSuggestionQuery("test")
        val expected = DataError.Local.Unknown
        coEvery { deviceContactsRepository.getDeviceContacts(query.value) } returns deviceContacts.right()
        coEvery {
            contactsRepository.getContactSuggestions(userId, deviceContacts, query)
        } returns expected.left()

        // When
        val actual = getContactSuggestions(userId, query)

        // Then
        assertEquals(expected.left(), actual)
    }

    companion object {

        private val deviceContacts = listOf(
            DeviceContact(name = "First Device Contact", email = "first@pvtmail.me"),
            DeviceContact(name = "Second Device Contact", email = "second@pvtmail.me")
        )
    }
}
