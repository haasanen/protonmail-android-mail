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

package ch.protonmail.android.mailmessage.presentation.reducer

import ch.protonmail.android.mailcommon.presentation.model.BottomSheetState
import ch.protonmail.android.mailcommon.presentation.sample.ParticipantAvatarSample
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.mailmessage.presentation.model.ContactActionUiModel
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.ContactActionsBottomSheetState
import ch.protonmail.android.testdata.contact.ContactSample
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class ContactActionsBottomSheetReducerTest(
    private val testName: String,
    private val testInput: TestInput
) {

    private val reducer = ContactActionsBottomSheetReducer()

    @Test
    fun `should produce the expected new bottom sheet state`() = with(testInput) {
        val actualState = reducer.newStateFrom(currentState, operation)

        assertEquals(expectedState, actualState, testName)
    }

    companion object {
        private val sampleParticipant = Participant(
            address = "test@proton.me",
            name = "Test User"
        )

        private val primaryUserParticipant = Participant(
            address = "primary@proton.me",
            name = "Primary User"
        )
        private val sampleContact = ContactSample.Stefano

        private val sampleAvatar = ParticipantAvatarSample.ebay
        private val sampleOrigin =
            ContactActionsBottomSheetState.Origin.MessageDetails(MessageId("msg-1"))


        private val transitionsFromLoadingState = listOf(
            TestInput(
                currentState = BottomSheetState(ContactActionsBottomSheetState.Loading),
                operation = ContactActionsBottomSheetState.ContactActionsBottomSheetEvent.ActionData(
                    participant = sampleParticipant,
                    avatarUiModel = sampleAvatar,
                    contactId = sampleContact.id,
                    origin = sampleOrigin,
                    isSenderBlocked = false,
                    isPrimaryUserAddress = false
                ),
                expectedState = BottomSheetState(
                    contentState = ContactActionsBottomSheetState.Data(
                        participant = sampleParticipant,
                        avatarUiModel = sampleAvatar,
                        actions = expectedGroupsFor(
                            participant = sampleParticipant,
                            contactId = sampleContact.id,
                            isSenderBlocked = false,
                            isPrimaryUserAddress = false
                        ),
                        origin = sampleOrigin
                    )
                )
            ),
            TestInput(
                currentState = BottomSheetState(ContactActionsBottomSheetState.Loading),
                operation = ContactActionsBottomSheetState.ContactActionsBottomSheetEvent.ActionData(
                    participant = sampleParticipant,
                    avatarUiModel = sampleAvatar,
                    contactId = sampleContact.id,
                    origin = sampleOrigin,
                    isSenderBlocked = true,
                    isPrimaryUserAddress = false
                ),
                expectedState = BottomSheetState(
                    contentState = ContactActionsBottomSheetState.Data(
                        participant = sampleParticipant,
                        avatarUiModel = sampleAvatar,
                        actions = expectedGroupsFor(
                            participant = sampleParticipant,
                            contactId = sampleContact.id,
                            isSenderBlocked = true,
                            isPrimaryUserAddress = false
                        ),
                        origin = sampleOrigin
                    )
                )
            ),
            TestInput(
                currentState = BottomSheetState(ContactActionsBottomSheetState.Loading),
                operation = ContactActionsBottomSheetState.ContactActionsBottomSheetEvent.ActionData(
                    participant = sampleParticipant,
                    avatarUiModel = sampleAvatar,
                    contactId = null,
                    origin = sampleOrigin,
                    isSenderBlocked = true,
                    isPrimaryUserAddress = false
                ),
                expectedState = BottomSheetState(
                    contentState = ContactActionsBottomSheetState.Data(
                        participant = sampleParticipant,
                        avatarUiModel = sampleAvatar,
                        actions = expectedGroupsFor(
                            participant = sampleParticipant,
                            contactId = null,
                            isSenderBlocked = true,
                            isPrimaryUserAddress = false
                        ),
                        origin = sampleOrigin
                    )
                )
            ),
            TestInput(
                currentState = BottomSheetState(ContactActionsBottomSheetState.Loading),
                operation = ContactActionsBottomSheetState.ContactActionsBottomSheetEvent.ActionData(
                    participant = sampleParticipant,
                    avatarUiModel = sampleAvatar,
                    contactId = null,
                    origin = sampleOrigin,
                    isSenderBlocked = false,
                    isPrimaryUserAddress = false
                ),
                expectedState = BottomSheetState(
                    contentState = ContactActionsBottomSheetState.Data(
                        participant = sampleParticipant,
                        avatarUiModel = sampleAvatar,
                        actions = expectedGroupsFor(
                            participant = sampleParticipant,
                            contactId = null,
                            isSenderBlocked = false,
                            isPrimaryUserAddress = false
                        ),
                        origin = sampleOrigin
                    )
                )
            ),
            TestInput(
                currentState = BottomSheetState(ContactActionsBottomSheetState.Loading),
                operation = ContactActionsBottomSheetState.ContactActionsBottomSheetEvent.ActionData(
                    participant = primaryUserParticipant,
                    avatarUiModel = sampleAvatar,
                    contactId = null,
                    origin = sampleOrigin,
                    isSenderBlocked = false,
                    isPrimaryUserAddress = true
                ),
                expectedState = BottomSheetState(
                    contentState = ContactActionsBottomSheetState.Data(
                        participant = primaryUserParticipant,
                        avatarUiModel = sampleAvatar,
                        actions = expectedGroupsFor(
                            participant = primaryUserParticipant,
                            contactId = null,
                            isSenderBlocked = false,
                            isPrimaryUserAddress = true
                        ),
                        origin = sampleOrigin
                    )
                )
            )
        )

        private fun expectedGroupsFor(
            participant: Participant,
            contactId: ContactId?,
            isSenderBlocked: Boolean,
            isPrimaryUserAddress: Boolean
        ): ContactActionsBottomSheetState.ContactActionsGroups {
            val first = listOf(
                ContactActionUiModel.NewMessage(participant)
            ).toImmutableList()

            val second = listOf(
                ContactActionUiModel.CopyAddress(participant.address),
                ContactActionUiModel.CopyName(participant.name)
            ).toImmutableList()

            val third = if (isPrimaryUserAddress) {
                emptyList()
            } else {
                listOf(
                    when {
                        contactId == null && !isSenderBlocked -> ContactActionUiModel.BlockAddress(participant)
                        contactId == null && isSenderBlocked -> ContactActionUiModel.UnblockAddress(participant)
                        contactId != null && !isSenderBlocked -> ContactActionUiModel.BlockContact(
                            participant, contactId
                        )

                        else -> ContactActionUiModel.UnblockContact(participant)
                    }
                )
            }.toImmutableList()

            return ContactActionsBottomSheetState.ContactActionsGroups(
                firstGroup = first,
                secondGroup = second,
                thirdGroup = third
            )
        }

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = transitionsFromLoadingState
            .map { testInput ->
                val testName = """
                    Current state: ${testInput.currentState}
                    Operation: ${testInput.operation}
                    Next state: ${testInput.expectedState}   
                """.trimIndent()
                arrayOf(testName, testInput)
            }
    }

    data class TestInput(
        val currentState: BottomSheetState,
        val operation: ContactActionsBottomSheetState.ContactActionsBottomSheetOperation,
        val expectedState: BottomSheetState
    )
}
