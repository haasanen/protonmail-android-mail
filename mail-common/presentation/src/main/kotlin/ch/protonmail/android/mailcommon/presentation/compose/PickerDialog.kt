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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ch.protonmail.android.mailcommon.presentation.R
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.titleLargeNorm

@Composable
fun PickerDialog(
    modifier: Modifier = Modifier,
    title: String,
    selectedValue: TextUiModel,
    values: List<TextUiModel>,
    onDismissRequest: () -> Unit,
    onValueSelected: (TextUiModel) -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            shape = RoundedCornerShape(MailDimens.DialogCardRadius),
            modifier = modifier
                .padding(ProtonDimens.Spacing.Large)
                .fillMaxWidth()
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .background(ProtonTheme.colors.backgroundNorm)
            ) {
                val (header, cancelButton, typeList) = createRefs()

                Text(
                    text = title,
                    style = ProtonTheme.typography.titleLargeNorm,
                    modifier = Modifier
                        .constrainAs(header) {
                            top.linkTo(parent.top, margin = ProtonDimens.Spacing.ExtraLarge)
                            start.linkTo(parent.start, margin = ProtonDimens.Spacing.ExtraLarge)
                        }
                )
                TextButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier
                        .constrainAs(cancelButton) {
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    Text(
                        text = stringResource(R.string.picker_dialog_cancel),
                        style = ProtonTheme.typography.bodyLargeNorm,
                        color = ProtonTheme.colors.brandNorm
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .constrainAs(typeList) {
                            top.linkTo(header.bottom, margin = ProtonDimens.Spacing.Large)
                            bottom.linkTo(cancelButton.top, margin = ProtonDimens.Spacing.Standard)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.preferredWrapContent
                        }
                ) {
                    items(values) { value ->
                        val isSelected = value == selectedValue
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = isSelected,
                                    role = Role.RadioButton
                                ) { onValueSelected(value) }
                                .padding(
                                    horizontal = ProtonDimens.Spacing.Large,
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
        }
    }
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
