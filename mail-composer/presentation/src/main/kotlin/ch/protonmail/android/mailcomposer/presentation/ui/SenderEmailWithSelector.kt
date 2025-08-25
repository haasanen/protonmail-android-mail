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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.uicomponents.chips.ChipsTestTags

@Composable
internal fun SenderEmailWithSelector(
    modifier: Modifier = Modifier,
    selectedEmail: String,
    onChangeSender: () -> Unit
) {
    Row(
        modifier = modifier
            .height(MailDimens.Composer.FormFieldsRowHeight)
            .padding(start = ProtonDimens.Spacing.Large)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = stringResource(R.string.from_prefix),
            modifier = Modifier
                .testTag(ChipsTestTags.FieldPrefix)
                .align(Alignment.CenterVertically),
            color = ProtonTheme.colors.textWeak,
            style = ProtonTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))

        BasicTextField(
            value = TextFieldValue(selectedEmail),
            onValueChange = { },
            modifier = Modifier
                .testTag(SenderEmailWithSelectorTestTags.TextField)
                .align(Alignment.CenterVertically)
                .weight(1f),
            readOnly = true,
            textStyle = ProtonTheme.typography.bodyMediumNorm,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            )
        )

        ChangeSenderButton(Modifier.align(Alignment.CenterVertically), onChangeSender)
    }
}

@Composable
private fun ChangeSenderButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(
        modifier = modifier
            .testTag(ComposerTestTags.ChangeSenderButton),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_proton_chevron_tiny_down),
            tint = ProtonTheme.colors.iconHint,
            contentDescription = NO_CONTENT_DESCRIPTION
        )
    }
}

object SenderEmailWithSelectorTestTags {

    const val TextField = "TextField"
}
