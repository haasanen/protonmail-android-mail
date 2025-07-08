package ch.protonmail.android.mailcontact.data.repository

import arrow.core.right
import ch.protonmail.android.mailcontact.data.local.RustContactGroupDataSource
import ch.protonmail.android.mailcontact.data.mapper.toLocalContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ContactGroupRepositoryImplTest {

    private val contactGroupDataSource = mockk<RustContactGroupDataSource>()

    private val contactGroupRepository = ContactGroupRepositoryImpl(contactGroupDataSource)

    @Test
    fun `getting contact details should call correct data source method`() = runTest {
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
            contactGroupDataSource.getContactGroupDetails(userId, contactGroupId.toLocalContactGroupId())
        } returns contactGroup.right()

        // When
        val result = contactGroupRepository.getContactGroupDetails(userId, contactGroupId)

        // Then
        assertEquals(contactGroup.right(), result)
        coVerify { contactGroupDataSource.getContactGroupDetails(userId, contactGroupId.toLocalContactGroupId()) }
    }
}
