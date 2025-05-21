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

package ch.protonmail.android.mailcommon.presentation.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcommon.presentation.R
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogButton as ProtonAlertDialogButton1

@Composable
fun PickerDialog(
    modifier: Modifier = Modifier,
    title: String,
    selectedValue: TextUiModel,
    values: List<TextUiModel>,
    onDismissRequest: () -> Unit,
    onValueSelected: (TextUiModel) -> Unit
) {
    ProtonAlertDialog(
        modifier = modifier,
        backgroundColor = ProtonTheme.colors.backgroundNorm,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = ProtonTheme.typography.titleLargeNorm,
                modifier = Modifier
            )
        },
        buttons = {
            ProtonAlertDialogButton1(
                title = stringResource(id = R.string.picker_dialog_cancel),
                onClick = onDismissRequest
            )
        },
        text = {
            LazyColumn {
                items(values) { value ->
                    val isSelected = value == selectedValue
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton
                            ) { onValueSelected(value) }
                            .padding(
                                vertical = MailDimens.PickerDialogItemVerticalPadding
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Large))
                        Text(
                            modifier = Modifier.weight(1f, fill = true),
                            text = value.string(),
                            style = ProtonTheme.typography.bodyLargeNorm
                        )
                    }
                }
            }
        }
    )
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
private fun TypePickerDialogPreview() {
    PickerDialog(
        title = "Title",
        selectedValue = TextUiModel("Value 2"),
        values = listOf(TextUiModel("Value 1"), TextUiModel("Value 2"), TextUiModel("Value 3")),
        onDismissRequest = { },
        onValueSelected = { }
    )
}
