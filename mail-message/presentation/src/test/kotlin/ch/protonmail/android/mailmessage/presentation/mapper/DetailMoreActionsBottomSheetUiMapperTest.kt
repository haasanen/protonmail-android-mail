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

package ch.protonmail.android.mailmessage.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState
import org.junit.Test
import kotlin.test.assertEquals

internal class DetailMoreActionsBottomSheetUiMapperTest {

    private val mapper = DetailMoreActionsBottomSheetUiMapper()

    @Test
    fun `should map to the correct header ui model`() {
        // Given
        val expected = DetailMoreActionsBottomSheetState.DetailDataUiModel(
            headerSubjectText = TextUiModel(ExpectedSubject),
            messageIdInConversation = ExpectedMessageId
        )

        // When
        val actual = mapper.toHeaderUiModel(ExpectedSubject, ExpectedMessageId)

        // Then
        assertEquals(expected, actual)
    }

    private companion object {

        const val ExpectedSender = "Sender"
        const val ExpectedSubject = "A subject"
        const val ExpectedMessageId = "messageId"
        const val SingleRecipientCount = 1
        const val PluralRecipientCount = 10
    }
}
