package ch.protonmail.android.mailcontact.presentation.contactgroupdetails

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.usecase.GetContactGroupDetails
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.AvatarUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class ContactGroupDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getContactGroupDetails = mockk<GetContactGroupDetails>()
    private val colorMapper = mockk<ColorMapper> {
        every { toColor(any()) } returns Color.Blue.right()
    }
    private val contactGroupDetailsUiModelMapper = ContactGroupDetailsUiModelMapper(colorMapper)
    private val savedStateHandle = mockk<SavedStateHandle>()
    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()

    private val viewModel by lazy {
        ContactGroupDetailsViewModel(
            contactGroupDetailsUiModelMapper = contactGroupDetailsUiModelMapper,
            getContactGroupDetails = getContactGroupDetails,
            savedStateHandle = savedStateHandle,
            observePrimaryUserId = observePrimaryUserId
        )
    }

    @Test
    fun `state should emit mapped data when getting contact group details was successful`() = runTest {
        // Given
        val contactGroupId = ContactGroupId("contactGroupId")
        val contactGroup = ContactMetadata.ContactGroup(
            id = contactGroupId,
            name = "Proton Mail",
            color = "color",
            members = listOf(
                ContactEmail(
                    id = ContactId("contactEmailId"),
                    email = "proton@protonmail.com",
                    isProton = false,
                    lastUsedTime = 0,
                    name = "Proton",
                    avatarInformation = AvatarInformation("P", "color")
                )
            )
        )
        coEvery {
            savedStateHandle.get<String>(ContactGroupDetailsScreen.CONTACT_GROUP_DETAILS_ID_KEY)
        } returns contactGroupId.id
        coEvery { observePrimaryUserId() } returns flowOf(UserIdTestData.userId)
        coEvery { getContactGroupDetails(UserIdTestData.userId, contactGroupId) } returns contactGroup.right()

        // When
        viewModel.state.test {

            // Then
            val expected = ContactGroupDetailsState.Data(
                uiModel = ContactGroupDetailsUiModel(
                    color = Color.Blue,
                    name = "Proton Mail",
                    memberCount = 1,
                    members = listOf(
                        ContactGroupMemberUiModel(
                            id = ContactId("contactEmailId"),
                            avatarUiModel = AvatarUiModel.Initials("P", Color.Blue),
                            name = "Proton",
                            emailAddress = "proton@protonmail.com"
                        )
                    )
                )
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `state should emit error when getting contact details was not successful`() = runTest {
        // Given
        val contactGroupId = ContactGroupId("contactGroupId")
        coEvery {
            savedStateHandle.get<String>(ContactGroupDetailsScreen.CONTACT_GROUP_DETAILS_ID_KEY)
        } returns contactGroupId.id
        coEvery { observePrimaryUserId() } returns flowOf(UserIdTestData.userId)
        coEvery {
            getContactGroupDetails(
                UserIdTestData.userId,
                contactGroupId
            )
        } returns DataError.Local.CryptoError.left()

        // When
        viewModel.state.test {

            // Then
            assertEquals(ContactGroupDetailsState.Error, awaitItem())
        }
    }

    @Test
    fun `state should emit error when contact id is null`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(ContactGroupDetailsScreen.CONTACT_GROUP_DETAILS_ID_KEY) } returns null
        coEvery { observePrimaryUserId() } returns flowOf(UserIdTestData.userId)

        // When
        viewModel.state.test {

            // Then
            assertEquals(ContactGroupDetailsState.Error, awaitItem())
        }
    }
}
