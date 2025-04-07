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

package ch.protonmail.android.mailcomposer.presentation.ui

import androidx.compose.ui.geometry.Rect
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionsField
import ch.protonmail.android.mailcomposer.presentation.model.FocusedFieldType
import ch.protonmail.android.mailcomposer.presentation.model.RecipientUiModel
import ch.protonmail.android.mailcomposer.presentation.model.WebViewMeasures
import ch.protonmail.android.mailmessage.domain.model.AttachmentId

internal data class ComposerFormActions(
    val onToggleRecipients: (Boolean) -> Unit,
    val onFocusChanged: (FocusedFieldType) -> Unit,
    val onToChanged: (List<RecipientUiModel>) -> Unit,
    val onCcChanged: (List<RecipientUiModel>) -> Unit,
    val onBccChanged: (List<RecipientUiModel>) -> Unit,
    val onContactSuggestionsDismissed: (ContactSuggestionsField) -> Unit,
    val onContactSuggestionSelected: (
        contact: ContactSuggestionUiModel,
        field: ContactSuggestionsField
    ) -> Unit,
    val onDeviceContactsPromptDenied: () -> Unit,
    val onContactSuggestionTermChanged: (String, ContactSuggestionsField) -> Unit,
    val onSubjectChanged: (String) -> Unit,
    val onBodyChanged: (String) -> Unit,
    val onWebViewMeasuresChanged: (WebViewMeasures) -> Unit,
    val onChangeSender: () -> Unit,
    val onHeaderPositioned: (boundsInWindow: Rect, height: Float) -> Unit,
    val onWebViewPositioned: (boundsInWindow: Rect) -> Unit,
    val onAttachmentRemoveRequested: (AttachmentId) -> Unit
)
