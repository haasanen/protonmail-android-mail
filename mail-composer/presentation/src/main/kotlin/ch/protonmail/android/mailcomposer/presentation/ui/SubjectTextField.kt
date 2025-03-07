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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens

@Composable
internal fun SubjectTextField(
    initialValue: String,
    onSubjectChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initialValue))
    }

    var userUpdated by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = initialValue) {
        if (!userUpdated) {
            text = TextFieldValue(initialValue)
        }
    }

    Row(
        modifier = modifier
            .height(MailDimens.Composer.FormFieldsRowHeight)
            .padding(start = ProtonDimens.Spacing.Large)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = stringResource(R.string.subject_prefix),
            modifier = Modifier.wrapContentWidth(),
            color = ProtonTheme.colors.textHint,
            style = ProtonTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Standard))
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onSubjectChange(it.text)
                userUpdated = true
            },
            modifier = Modifier
                .padding(horizontal = 0.dp)
                .weight(1f),
            textStyle = ProtonTheme.typography.bodyMediumNorm,
            maxLines = 3,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            )
        )
    }
}

@Preview
@Composable
private fun SubjectTextFieldPreview() {
    ProtonTheme {
        SubjectTextField(
            initialValue = "Test subject",
            onSubjectChange = {}
        )
    }
}

