package ch.protonmail.android.uicomponents.chips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.uicomponents.chips.item.ChipItemsList
import ch.protonmail.android.uicomponents.thenIf
import kotlinx.coroutines.launch

@Composable
fun ChipsListTextField(
    state: ChipsListState,
    textFieldState: TextFieldState,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    cursorColor: Color = ProtonTheme.colors.iconAccent,
    textStyle: TextStyle = ProtonTheme.typography.bodyMediumNorm,
    animateChipsCreation: Boolean = false,
    actions: ChipsListTextField.Actions
) {
    val focusManager = LocalFocusManager.current
    val localDensity = LocalDensity.current
    var textMaxWidth by remember { mutableStateOf(Dp.Unspecified) }

    val rect by remember { mutableStateOf(Rect.Zero) }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val keyboardOptions = remember {
        KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            autoCorrectEnabled = false
        )
    }

    // Similar to what we do for the Message body, we need to ensure
    // that the cursor is always on screen when the user types.
    fun bringRectIntoView(rect: Rect) = coroutineScope.launch { bringIntoViewRequester.bringIntoView(rect) }

    LaunchedEffect(textFieldState.text) {
        bringRectIntoView(rect)
    }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                if (textMaxWidth == Dp.Unspecified) {
                    textMaxWidth = with(localDensity) { size.width.toDp() }
                }
            },
        verticalArrangement = Arrangement.Center
    ) {
        val items = state.getItems()

        when (items) {
            ChipItemsList.Empty -> Unit
            is ChipItemsList.Focused -> FocusedChipsList(
                chipItems = items.items,
                animateChipsCreation = animateChipsCreation,
                textMaxWidth = textMaxWidth,
                onClickItem = { actions.onItemClicked(it) }
            )

            is ChipItemsList.Unfocused.Multiple -> UnFocusedChipsList(
                items.item,
                items.counter
            ) { focusRequester?.requestFocus() }

            is ChipItemsList.Unfocused.Single -> UnFocusedChipsList(items.item) { focusRequester?.requestFocus() }
        }

        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .align(Alignment.CenterVertically)
                .testTag(ChipsTestTags.BasicTextField)
                .thenIf(focusRequester != null) {
                    focusRequester(focusRequester!!)
                }
                .thenIf(!state.isFocused() && items !is ChipItemsList.Empty) {
                    height(0.dp)
                }
                .padding(vertical = ProtonDimens.Spacing.Large)
                .padding(start = ProtonDimens.Spacing.Standard)
                .padding(end = ProtonDimens.Spacing.Large)
                .onFocusChanged { actions.onFocusChanged(it) },
            state = textFieldState,
            keyboardOptions = keyboardOptions,
            onKeyboardAction = {
                if (textFieldState.text.isNotEmpty()) {
                    actions.onTriggerChipCreation()
                } else {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            },
            cursorBrush = SolidColor(cursorColor),
            textStyle = textStyle
        )
    }
}

object ChipsListTextField {
    data class Actions(
        val onFocusChanged: (focusChange: FocusState) -> Unit,
        val onItemClicked: (index: Int) -> Unit,
        val onTriggerChipCreation: () -> Unit
    )
}
