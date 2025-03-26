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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.compose.FocusableForm
import ch.protonmail.android.mailcommon.presentation.ui.MailDivider
import ch.protonmail.android.mailcomposer.presentation.model.ComposerFields
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionsField
import ch.protonmail.android.mailcomposer.presentation.model.FocusedFieldType
import ch.protonmail.android.uicomponents.keyboardVisibilityAsState
import timber.log.Timber

@Composable
internal fun ComposerForm(
    emailValidator: (String) -> Boolean,
    recipientsOpen: Boolean,
    initialFocus: FocusedFieldType,
    changeFocusToField: Effect<FocusedFieldType>,
    fields: ComposerFields,
    actions: ComposerFormActions,
    contactSuggestions: Map<ContactSuggestionsField, List<ContactSuggestionUiModel>>,
    areContactSuggestionsExpanded: Map<ContactSuggestionsField, Boolean>,
    modifier: Modifier = Modifier
) {
    val isKeyboardVisible by keyboardVisibilityAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val maxWidthModifier = Modifier.fillMaxWidth()

    var showSubjectAndBody by remember { mutableStateOf(true) }
    var isSubjectFocused by remember { mutableStateOf(false) }

    // Handle visibility of body and subject here, to avoid issues with focus requesters.
    LaunchedEffect(areContactSuggestionsExpanded) {
        showSubjectAndBody = !areContactSuggestionsExpanded.any { it.value }
    }

    FocusableForm(
        fieldList = listOf(
            FocusedFieldType.TO,
            FocusedFieldType.CC,
            FocusedFieldType.BCC,
            FocusedFieldType.SUBJECT,
            FocusedFieldType.BODY
        ),
        initialFocus = initialFocus,
        onFocusedField = {
            Timber.d("Focus changed: onFocusedField: $it")
            actions.onFocusChanged(it)

            isSubjectFocused = it == FocusedFieldType.SUBJECT
        }
    ) { fieldFocusRequesters ->

        ConsumableLaunchedEffect(effect = changeFocusToField) {
            fieldFocusRequesters[it]?.requestFocus()
            if (!isKeyboardVisible) {
                keyboardController?.show()
            }
        }

        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            RecipientFields2(
                fields = fields,
                fieldFocusRequesters = fieldFocusRequesters,
                recipientsOpen = recipientsOpen,
                emailValidator = emailValidator,
                contactSuggestions = contactSuggestions,
                areContactSuggestionsExpanded = areContactSuggestionsExpanded,
                actions = actions
            )

            MailDivider()

            SenderEmailWithSelector(
                modifier = maxWidthModifier.testTag(ComposerTestTags.FromSender),
                selectedEmail = fields.sender.email,
                actions.onChangeSender
            )

            if (showSubjectAndBody) {
                MailDivider()
                SubjectTextField(
                    initialValue = fields.subject,
                    onSubjectChange = actions.onSubjectChanged,
                    modifier = maxWidthModifier
                        .testTag(ComposerTestTags.Subject)
                        .retainFieldFocusOnConfigurationChange(FocusedFieldType.SUBJECT),
                    isFocused = isSubjectFocused
                )
                MailDivider()
            }
        }

        if (showSubjectAndBody) {
            MessageBodyEditor(
                messageBodyUiModel = fields.displayBody,
                onBodyChanged = actions.onBodyChanged,
                onWebViewMeasuresChanged = actions.onWebViewMeasuresChanged,
                modifier = maxWidthModifier
                    .testTag(ComposerTestTags.MessageBody)
                    .retainFieldFocusOnConfigurationChange(FocusedFieldType.BODY)
                    .onGloballyPositioned { coordinates ->
                        val webViewBounds = coordinates.boundsInWindow()
                        actions.onWebViewPositioned(webViewBounds)
                    }
            )
        }
    }
}

