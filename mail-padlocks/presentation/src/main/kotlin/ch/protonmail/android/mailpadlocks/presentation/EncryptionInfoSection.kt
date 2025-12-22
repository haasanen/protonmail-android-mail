/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailpadlocks.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallNorm
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens.MessageDetailsHeader.DetailsTitleWidth
import ch.protonmail.android.mailcommon.presentation.compose.SmallNonClickableIcon
import ch.protonmail.android.mailpadlocks.presentation.model.EncryptionInfoState
import ch.protonmail.android.mailpadlocks.presentation.model.EncryptionInfoUiModel

@Composable
fun EncryptionInfoSection(
    uiModel: EncryptionInfoUiModel,
    onMoreInfoClick: (EncryptionInfoUiModel) -> Unit,
    viewModel: EncryptionInfoViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {

    val state = viewModel.state.collectAsStateWithLifecycle()

    when (state.value) {
        EncryptionInfoState.Enabled -> EncryptionInfo(
            uiModel = uiModel,
            onMoreInfoClick = { onMoreInfoClick(uiModel) },
            modifier = modifier
        )
        is EncryptionInfoState.Disabled -> {}
    }
}

@Composable
private fun EncryptionInfo(
    uiModel: EncryptionInfoUiModel,
    onMoreInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.ModeratelyLarge)
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier.width(DetailsTitleWidth),
                contentAlignment = Alignment.Center
            ) {
                SmallNonClickableIcon(
                    iconId = uiModel.icon,
                    iconColor = colorResource(uiModel.color)
                )
            }
            Column {

                Text(
                    modifier = Modifier
                        .wrapContentWidth(),
                    text = stringResource(uiModel.summary),
                    style = ProtonTheme.typography.bodySmallNorm
                )

                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))

                Text(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clickable(onClick = { onMoreInfoClick() }),
                    text = stringResource(R.string.padlocks_show_details),
                    style = ProtonTheme.typography.bodySmallNorm.copy(
                        color = ProtonTheme.colors.iconAccent
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEncryptionInfo() {
    EncryptionInfo(
        uiModel = EncryptionInfoUiModel.StoredWithZeroAccessEncryption,
        onMoreInfoClick = { }
    )
}
