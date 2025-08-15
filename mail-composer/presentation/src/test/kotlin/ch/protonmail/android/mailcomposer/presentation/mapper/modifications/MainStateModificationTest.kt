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

package ch.protonmail.android.mailcomposer.presentation.mapper.modifications

import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftFields
import ch.protonmail.android.mailcomposer.domain.model.DraftMimeType
import ch.protonmail.android.mailcomposer.domain.model.DraftRecipient
import ch.protonmail.android.mailcomposer.domain.model.RecipientsBcc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsCc
import ch.protonmail.android.mailcomposer.domain.model.RecipientsTo
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.domain.model.Subject
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.model.DraftUiModel
import ch.protonmail.android.mailcomposer.presentation.model.SenderUiModel
import ch.protonmail.android.mailcomposer.presentation.reducer.modifications.MainStateModification
import kotlinx.collections.immutable.toImmutableList
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class MainStateModificationTest(
    @Suppress("unused") private val testName: String,
    private val initialState: ComposerState.Main,
    private val modification: MainStateModification,
    private val expectedState: ComposerState.Main
) {

    @Test
    fun `should apply the modification`() {
        val updatedState = modification.apply(initialState)
        assertEquals(expectedState, updatedState)
    }

    companion object {

        private val initialState = ComposerState.Main.initial()

        private val senderEmail = SenderEmail("sender-email@proton.me")
        private val draftDisplayBody = DraftDisplayBodyUiModel("<html>draft display body</html>")
        private val draftFields = DraftFields(
            SenderEmail("author@proton.me"),
            Subject("Here is the matter"),
            DraftBody("Decrypted body of this draft"),
            DraftMimeType.Html,
            RecipientsTo(listOf(DraftRecipient.SingleRecipient("Name", "you@proton.ch"))),
            RecipientsCc(emptyList()),
            RecipientsBcc(emptyList())
        )
        private val draftUiModel = DraftUiModel(draftFields, draftDisplayBody)

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "set sender and quoted content from initial state",
                initialState,
                MainStateModification.OnDraftReady(draftUiModel),
                initialState.copy(
                    sender = SenderUiModel(draftUiModel.draftFields.sender.value)
                )
            ),
            arrayOf(
                "update loading type (save)",
                initialState,
                MainStateModification.UpdateLoading(ComposerState.LoadingType.Initial),
                initialState.copy(loadingType = ComposerState.LoadingType.Initial)
            ),
            arrayOf(
                "update loading type (initial)",
                initialState,
                MainStateModification.UpdateLoading(ComposerState.LoadingType.Save),
                initialState.copy(loadingType = ComposerState.LoadingType.Save)
            ),
            arrayOf(
                "update loading type (save)",
                initialState,
                MainStateModification.UpdateLoading(ComposerState.LoadingType.None),
                initialState.copy(loadingType = ComposerState.LoadingType.None)
            ),
            arrayOf(
                "update senders list",
                initialState,
                MainStateModification.SendersListReady(
                    listOf(
                        SenderUiModel("sender1@example.com"),
                        SenderUiModel("sender2@example.com")
                    )
                ),
                initialState.copy(
                    senderAddresses = listOf(
                        SenderUiModel("sender1@example.com"),
                        SenderUiModel("sender2@example.com")
                    ).toImmutableList()
                )
            ),
            arrayOf(
                "update submittable to true",
                initialState,
                MainStateModification.UpdateSubmittable(true),
                initialState.copy(isSubmittable = true)
            ),
            arrayOf(
                "update submittable to false",
                initialState.copy(isSubmittable = true),
                MainStateModification.UpdateSubmittable(false),
                initialState.copy(isSubmittable = false)
            ),
            arrayOf(
                "update sender when sender changes",
                initialState.copy(),
                MainStateModification.UpdateSender(senderEmail),
                initialState.copy(
                    sender = SenderUiModel(senderEmail.value)
                )
            )
        )
    }
}
