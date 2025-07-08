package ch.protonmail.android.mailcontact.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.repository.ContactGroupRepository
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetContactGroupDetailsTest {

    private val contactGroupRepository = mockk<ContactGroupRepository>()

    private val getContactGroupDetails = GetContactGroupDetails(contactGroupRepository)

    @Test
    fun `should call the correct repository method`() = runTest {
        // Given
        val userId = UserIdTestData.userId
        val contactGroupId = ContactGroupId("1")
        val contactGroup = ContactMetadata.ContactGroup(
            id = contactGroupId,
            name = "name",
            color = "color",
            members = emptyList()
        )
        coEvery {
            contactGroupRepository.getContactGroupDetails(userId, contactGroupId)
        } returns contactGroup.right()

        // When
        val result = getContactGroupDetails(userId, contactGroupId)

        // Then
        assertEquals(contactGroup.right(), result)
        coVerify { contactGroupRepository.getContactGroupDetails(userId, contactGroupId) }
    }
}
