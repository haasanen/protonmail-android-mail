package ch.protonmail.android.mailcontact.data.repository

import arrow.core.right
import ch.protonmail.android.mailcontact.data.local.RustContactDataSource
import ch.protonmail.android.mailcontact.data.mapper.toLocalContactId
import ch.protonmail.android.mailcontact.domain.model.ContactDetailCard
import ch.protonmail.android.testdata.contact.ContactIdTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ContactRepositoryImplTest {

    private val contactDataSource = mockk<RustContactDataSource>()

    private val contactRepository = ContactRepositoryImpl(contactDataSource)

    @Test
    fun `getting contact details should call correct data source method`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val contactId = ContactIdTestData.contactId1
        val contactDetailCard = mockk<ContactDetailCard>(relaxed = true)
        coEvery {
            contactDataSource.getContactDetails(userId, contactId.toLocalContactId())
        } returns contactDetailCard.right()

        // When
        val result = contactRepository.getContactDetails(userId, contactId)

        // Then
        assertEquals(contactDetailCard.right(), result)
        coVerify { contactDataSource.getContactDetails(userId, contactId.toLocalContactId()) }
    }
}
