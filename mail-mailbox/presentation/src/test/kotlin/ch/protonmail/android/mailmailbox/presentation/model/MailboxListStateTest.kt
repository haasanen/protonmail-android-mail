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

package ch.protonmail.android.mailmailbox.presentation.model

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.LoadingBarUiState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxListState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.hasClearableOperations
import ch.protonmail.android.mailmailbox.presentation.mailbox.previewdata.MailboxSearchStateSampleData
import ch.protonmail.android.mailmessage.presentation.model.AvatarImagesUiModel
import org.junit.Assert
import org.junit.Test

class MailboxListStateTest {

    @Test
    fun `hasClearableOperations is false when not in ViewMode`() {
        // Given
        val state = MailboxListState.Loading
        Assert.assertFalse(state.hasClearableOperations())
    }

    @Test
    fun `hasClearableOperations is false when in Search State`() {
        // Given
        val systemLabel =
            MailLabel.System(id = MailLabelId.System(LabelId("test")), systemLabelId = SystemLabelId.Spam, 1)
        val state = MailboxListState.Data.ViewMode(
            currentMailLabel = systemLabel,
            openItemEffect = Effect.empty(),
            scrollToMailboxTop = Effect.empty(),
            refreshErrorEffect = Effect.empty(),
            refreshOngoing = false,
            swipeActions = null,
            searchState = MailboxSearchStateSampleData.SearchLoading,
            shouldShowFab = true,
            avatarImagesUiModel = AvatarImagesUiModel.Empty,
            loadingBarState = LoadingBarUiState.Hide
        )
        Assert.assertFalse(state.hasClearableOperations())
    }

    @Test
    fun `hasClearableOperations is true when in viewMode and Not in Search State`() {
        // Given
        val systemLabel =
            MailLabel.System(id = MailLabelId.System(LabelId("test")), systemLabelId = SystemLabelId.Spam, 1)
        val state = MailboxListState.Data.ViewMode(
            currentMailLabel = systemLabel,
            openItemEffect = Effect.empty(),
            scrollToMailboxTop = Effect.empty(),
            refreshErrorEffect = Effect.empty(),
            refreshOngoing = false,
            swipeActions = null,
            searchState = MailboxSearchStateSampleData.NotSearching,
            shouldShowFab = true,
            avatarImagesUiModel = AvatarImagesUiModel.Empty,
            loadingBarState = LoadingBarUiState.Hide
        )
        Assert.assertTrue(state.hasClearableOperations())
    }
}
