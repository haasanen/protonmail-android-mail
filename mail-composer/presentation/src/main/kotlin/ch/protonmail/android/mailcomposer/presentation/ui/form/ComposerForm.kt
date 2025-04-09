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

package ch.protonmail.android.mailcomposer.presentation.ui.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.compose.FocusableForm
import ch.protonmail.android.mailcommon.presentation.ui.MailDivider
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import ch.protonmail.android.mailcomposer.presentation.model.FocusedFieldType
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsStateManager
import ch.protonmail.android.mailcomposer.presentation.model.WebViewMeasures
import ch.protonmail.android.mailcomposer.presentation.ui.ComposerTestTags
import ch.protonmail.android.mailcomposer.presentation.ui.MessageBodyEditor
import ch.protonmail.android.mailcomposer.presentation.ui.SenderEmailWithSelector
import ch.protonmail.android.mailcomposer.presentation.ui.SubjectTextField
import ch.protonmail.android.mailcomposer.presentation.viewmodel.RecipientsViewModel
import ch.protonmail.android.uicomponents.keyboardVisibilityAsState
import timber.log.Timber

@Composable
internal fun ComposerForm(
    changeFocusToField: Effect<FocusedFieldType>,
    senderEmail: String,
    recipientsStateManager: RecipientsStateManager,
    subjectTextField: TextFieldState,
    bodyInitialValue: DraftDisplayBodyUiModel,
    focusTextBody: Effect<Unit>,
    actions: ComposerForm.Actions,
    formHeightPx: Float,
    modifier: Modifier = Modifier
) {

    val recipientsViewModel = hiltViewModel<RecipientsViewModel, RecipientsViewModel.Factory> { factory ->
        factory.create(recipientsStateManager)
    }

    val isKeyboardVisible by keyboardVisibilityAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val maxWidthModifier = Modifier.fillMaxWidth()

    var showSubjectAndBody by remember { mutableStateOf(true) }
    var isSubjectFocused by remember { mutableStateOf(false) }

    FocusableForm(
        fieldList = listOf(
            FocusedFieldType.TO,
            FocusedFieldType.CC,
            FocusedFieldType.BCC,
            FocusedFieldType.SUBJECT,
            FocusedFieldType.BODY
        ),
        initialFocus = FocusedFieldType.TO,
        onFocusedField = {
            Timber.d("Focus changed: onFocusedField: $it")

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        val headerBounds = coordinates.boundsInWindow()
                        val headerHeight = coordinates.boundsInParent().height
                        actions.onHeaderPositioned(headerBounds, headerHeight)
                    }
            ) {
                RecipientFields(
                    fieldFocusRequesters = fieldFocusRequesters,
                    onToggleSuggestions = { isShown -> showSubjectAndBody = isShown },
                    viewModel = recipientsViewModel,
                    formHeightPx = formHeightPx
                )

                if (showSubjectAndBody) {
                    MailDivider()

                    SenderEmailWithSelector(
                        modifier = maxWidthModifier.testTag(ComposerTestTags.FromSender),
                        selectedEmail = senderEmail,
                        onChangeSender = actions.onChangeSender
                    )
                    MailDivider()

                    SubjectTextField(
                        textFieldState = subjectTextField,
                        isFocused = isSubjectFocused,
                        modifier = maxWidthModifier
                            .testTag(ComposerTestTags.Subject)
                            .retainFieldFocusOnConfigurationChange(FocusedFieldType.SUBJECT)
                    )
                    MailDivider()

                }
            }

            if (showSubjectAndBody) {
                MessageBodyEditor(
                    messageBodyUiModel = bodyInitialValue,
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
}

internal object ComposerForm {
    data class Actions(
        val onChangeSender: () -> Unit,
        val onBodyChanged: (String) -> Unit,
        val onWebViewMeasuresChanged: (WebViewMeasures) -> Unit,
        val onHeaderPositioned: (boundsInWindow: Rect, height: Float) -> Unit,
        val onWebViewPositioned: (boundsInWindow: Rect) -> Unit
    )
}
