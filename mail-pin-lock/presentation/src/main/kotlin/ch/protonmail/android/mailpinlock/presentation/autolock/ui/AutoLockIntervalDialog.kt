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

package ch.protonmail.android.mailpinlock.presentation.autolock.ui

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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.compose.PickerDialog
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailpinlock.presentation.R
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockIntervalState.Data
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockIntervalState.Loading
import ch.protonmail.android.mailpinlock.presentation.autolock.model.intervalFor
import ch.protonmail.android.mailpinlock.presentation.autolock.viewmodel.AutoLockIntervalViewModel

const val TEST_TAG_AUTOLOCK_INTEVALS_DIALOG = "AutoLockIntervalsDialogTestTag"

@Composable
fun AutoLockIntervalDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: AutoLockIntervalViewModel = hiltViewModel()
) {
    val effects = viewModel.effects.collectAsStateWithLifecycle().value
    ConsumableLaunchedEffect(effects.close) {
        onDismiss()
    }
    when (
        val state = viewModel.state.collectAsStateWithLifecycle(Loading).value
    ) {
        is Data -> {
            AutoLockIntervalDialog(
                modifier = modifier,
                onDismiss = onDismiss,
                onIntervalSelected = { viewModel.onIntervalSelected(state.intervalFor(it)) },
                selectedInterval = state.selectedInterval,
                choices = state.intervalChoices
            )
        }

        is Loading -> Unit
    }
}

@Composable
fun AutoLockIntervalDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onIntervalSelected: (TextUiModel) -> Unit,
    selectedInterval: TextUiModel,
    choices: List<TextUiModel>
) {
    PickerDialog(
        title = stringResource(R.string.mail_pinlock_settings_change_interval_title),
        selectedValue = selectedInterval,
        values = choices,
        onDismissRequest = onDismiss,
        onValueSelected = onIntervalSelected,
        modifier = modifier.testTag(TEST_TAG_AUTOLOCK_INTEVALS_DIALOG)
    )
}

@Preview(name = "Theme settings screen")
@Composable
fun PreviewAutoLockIntervalDialog() {
    AutoLockIntervalDialog(
        onDismiss = {},
        onIntervalSelected = {},
        selectedInterval = TextUiModel(R.string.mail_pinlock_settings_autolock_description_five_minutes),
        choices = listOf(
            TextUiModel(R.string.mail_pinlock_settings_autolock_description_five_minutes),
            TextUiModel(R.string.mail_pinlock_settings_autolock_description_five_minutes),
            TextUiModel(R.string.mail_pinlock_settings_autolock_description_five_minutes)
        )
    )
}
