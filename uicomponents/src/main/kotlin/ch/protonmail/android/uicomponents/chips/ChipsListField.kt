package ch.protonmail.android.uicomponents.chips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.uicomponents.chips.ChipsListState.Companion.ChipsCreationRegex
import ch.protonmail.android.uicomponents.chips.item.ChipItem
import ch.protonmail.android.uicomponents.thenIf

@Composable
fun ChipsListField(
    label: String,
    value: List<ChipItem>,
    clearCurrentText: Boolean,
    modifier: Modifier = Modifier,
    chipValidator: (String) -> Boolean = { true },
    focusRequester: FocusRequester? = null,
    focusOnClick: Boolean = true,
    actions: ChipsListField.Actions,
    chevronIconContent: @Composable () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val state by remember { mutableStateOf(ChipsListState(chipValidator, actions.onListChanged)) }
    var textFieldValue by remember { mutableStateOf(initialTextFieldValue) }

    val chipsListActions = remember {
        ChipsListTextField.Actions(
            onTextChanged = { value ->
                state.type(value.text)
                textFieldValue = if (ChipsCreationRegex.containsMatchIn(value.text)) initialTextFieldValue else value
                actions.onSuggestionTermTyped(textFieldValue.text)
            },
            onFocusChanged = { focusChange ->
                state.setFocusState(focusChange.isFocused)
                if (!focusChange.hasFocus) {
                    state.createChip()
                    textFieldValue = initialTextFieldValue
                    actions.onSuggestionsDismissed()
                }
            },
            onItemDeleted = {
                it?.let { state.onDelete(it) } ?: state.onDelete()
            },
            onTriggerChipCreation = {
                state.createChip()
                textFieldValue = initialTextFieldValue
                actions.onSuggestionsDismissed()
            }
        )
    }

    if (clearCurrentText) {
        textFieldValue = initialTextFieldValue
        state.type("")
    }

    state.updateItems(value)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = label,
                modifier = Modifier
                    .testTag(ChipsTestTags.FieldPrefix)
                    .align(Alignment.Top)
                    .padding(vertical = ProtonDimens.Spacing.Large)
                    .padding(start = ProtonDimens.Spacing.Large),
                color = ProtonTheme.colors.textHint,
                style = ProtonTheme.typography.bodyMedium
            )

            ChipsListTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .thenIf(focusOnClick) {
                        clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { focusRequester?.requestFocus() }
                        )
                    },
                textFieldValue = textFieldValue,
                state = state,
                focusRequester = focusRequester,
                actions = chipsListActions
            )

            chevronIconContent()
        }
    }
}

private val initialTextFieldValue = TextFieldValue("")

object ChipsListField {
    data class Actions(
        val onSuggestionTermTyped: (String) -> Unit,
        val onSuggestionsDismissed: () -> Unit,
        val onListChanged: (List<ChipItem>) -> Unit
    )
}
