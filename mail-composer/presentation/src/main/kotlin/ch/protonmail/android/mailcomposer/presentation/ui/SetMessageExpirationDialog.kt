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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.mailcomposer.presentation.model.ExpirationTimeOption
import ch.protonmail.android.mailcomposer.presentation.model.ExpirationTimeUiModel
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun SetMessageExpirationDialog(
    expirationTime: ExpirationTimeUiModel,
    onTimePicked: (ExpirationTimeUiModel) -> Unit,
    onDismiss: () -> Unit
) {

    val selectedItem = rememberSaveable(stateSaver = SelectedItemSaver) { mutableStateOf(expirationTime) }
    val showCustomExpirationTimeDialog = rememberSaveable { mutableStateOf(false) }

    when {
        showCustomExpirationTimeDialog.value -> CustomExpirationDateTimePicker(
            onTimePicked = onTimePicked,
            onDismiss = onDismiss,
            initialTime = expirationTime.customTime ?: Clock.System.now()
        )

        else -> ExpirationTimeOptionsDialog(
            onTimePicked = {
                selectedItem.value = it
                if (it.selectedOption == ExpirationTimeOption.Custom) {
                    showCustomExpirationTimeDialog.value = true
                } else {
                    onTimePicked(it)
                }
            },
            onDismiss = onDismiss,
            selectedItem.value
        )
    }
}

@Preview
@Composable
fun PreviewSetExpirationTimeDialog() {
    SetMessageExpirationDialog(
        expirationTime = ExpirationTimeUiModel(ExpirationTimeOption.OneHour),
        onTimePicked = {},
        onDismiss = {}
    )
}

private val SelectedItemSaver = mapSaver(
    save = {
        mapOf(
            KEY_SELECTED_OPTION to it.selectedOption.name,
            KEY_SELECTED_CUSTOM_TIME to it.customTime?.epochSeconds
        )
    },
    restore = { map ->
        val selectedOption = map[KEY_SELECTED_OPTION]?.let {
            ExpirationTimeOption.valueOf(it.toString())
        } ?: ExpirationTimeOption.None
        val customTime = map[KEY_SELECTED_CUSTOM_TIME]?.let {
            runCatching { Instant.fromEpochSeconds(it.toString().toLong()) }.getOrNull()
        }

        ExpirationTimeUiModel(selectedOption, customTime)
    }
)

private const val KEY_SELECTED_OPTION = "selectedOptionKey"
private const val KEY_SELECTED_CUSTOM_TIME = "selectedCustomTimeKey"
