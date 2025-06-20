package ch.protonmail.android.mailcontact.presentation.contactdetails

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcontact.domain.model.ContactDetailCard
import ch.protonmail.android.mailcontact.domain.usecase.GetContactDetails
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.contactdetails.mapper.ContactDetailsUiModelMapper
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.AvatarUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsState
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.HeaderUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.QuickActionType
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.QuickActionUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.ui.ContactDetailsScreen
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import ch.protonmail.android.testdata.contact.ContactIdTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class ContactDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getContactDetails = mockk<GetContactDetails>()
    private val contactDetailsUiModelMapper = ContactDetailsUiModelMapper()
    private val savedStateHandle = mockk<SavedStateHandle>()
    private val observePrimaryUserId = mockk<ObservePrimaryUserId>()

    private val viewModel by lazy {
        ContactDetailsViewModel(
            getContactDetails = getContactDetails,
            contactDetailsUiModelMapper = contactDetailsUiModelMapper,
            savedStateHandle = savedStateHandle,
            observePrimaryUserId = observePrimaryUserId
        )
    }

    @Test
    fun `state should emit mapped data when getting contact details was successful`() = runTest {
        // Given
        val contactId = ContactIdTestData.contactId1
        val contactDetailCard = ContactDetailCard(contactId, emptyList())
        coEvery { savedStateHandle.get<String>(ContactDetailsScreen.CONTACT_DETAILS_ID_KEY) } returns contactId.id
        coEvery { observePrimaryUserId() } returns flowOf(UserIdTestData.userId)
        coEvery { getContactDetails(UserIdTestData.userId, contactId) } returns contactDetailCard.right()

        // When
        viewModel.state.test {

            // Then
            val expected = ContactDetailsState.Data(
                uiModel = ContactDetailsUiModel(
                    avatarUiModel = AvatarUiModel.Initials(
                        value = "P",
                        color = Color.Blue
                    ),
                    headerUiModel = HeaderUiModel(
                        displayName = "Proton Mail",
                        displayEmailAddress = "proton@pm.me"
                    ),
                    quickActionUiModels = listOf(
                        QuickActionUiModel(
                            quickActionType = QuickActionType.Message,
                            icon = R.drawable.ic_proton_pen_square,
                            label = R.string.contact_details_quick_action_message,
                            isEnabled = false
                        ),
                        QuickActionUiModel(
                            quickActionType = QuickActionType.Call,
                            icon = R.drawable.ic_proton_phone,
                            label = R.string.contact_details_quick_action_call,
                            isEnabled = false
                        ),
                        QuickActionUiModel(
                            quickActionType = QuickActionType.Share,
                            icon = R.drawable.ic_proton_arrow_up_from_square,
                            label = R.string.contact_details_quick_action_share,
                            isEnabled = true
                        )
                    ),
                    contactDetailsItemGroupUiModels = emptyList()
                )
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `state should emit error when getting contact details was not successful`() = runTest {
        // Given
        val contactId = ContactIdTestData.contactId1
        coEvery { savedStateHandle.get<String>(ContactDetailsScreen.CONTACT_DETAILS_ID_KEY) } returns contactId.id
        coEvery { observePrimaryUserId() } returns flowOf(UserIdTestData.userId)
        coEvery { getContactDetails(UserIdTestData.userId, contactId) } returns DataError.Local.Unknown.left()

        // When
        viewModel.state.test {

            // Then
            assertEquals(ContactDetailsState.Error, awaitItem())
        }
    }

    @Test
    fun `state should emit error when contact id is null`() = runTest {
        // Given
        coEvery { savedStateHandle.get<String>(ContactDetailsScreen.CONTACT_DETAILS_ID_KEY) } returns null
        coEvery { observePrimaryUserId() } returns flowOf(UserIdTestData.userId)

        // When
        viewModel.state.test {

            // Then
            assertEquals(ContactDetailsState.Error, awaitItem())
        }
    }
}
