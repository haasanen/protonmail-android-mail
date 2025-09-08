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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonRawListItem
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.SenderUiModel
import ch.protonmail.android.uicomponents.BottomNavigationBarSpacer

@Composable
fun ChangeSenderBottomSheetContent(
    addresses: List<SenderUiModel>,
    current: SenderUiModel,
    onSenderSelected: (SenderUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(ProtonTheme.colors.backgroundInvertedNorm)
            .padding(ProtonDimens.Spacing.Large)
    ) {

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.composer_change_sender_title),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.titleLargeNorm,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

        Card(
            modifier = modifier.fillMaxWidth(),
            shape = ProtonTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors().copy(
                containerColor = ProtonTheme.colors.backgroundInvertedSecondary
            )
        ) {
            LazyColumn(
                modifier = modifier
                    .testTag(ChangeSenderBottomSheetTestTags.Root)
            ) {

                itemsIndexed(addresses) { index, item ->
                    ProtonRawListItem(
                        modifier = Modifier
                            .testTag("${ChangeSenderBottomSheetTestTags.Item}$index")
                            .clickable { onSenderSelected(item) }
                            .padding(
                                horizontal = ProtonDimens.Spacing.Large,
                                vertical = ProtonDimens.Spacing.ModeratelyLarge
                            )
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = item.email,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(
                            modifier = Modifier.size(ProtonDimens.Spacing.Standard)
                        )

                        if (item.email == current.email) {
                            Icon(
                                modifier = Modifier.padding(),
                                painter = painterResource(id = R.drawable.ic_proton_checkmark),
                                contentDescription = null,
                                tint = ProtonTheme.colors.iconAccent
                            )
                        }
                    }

                    if (index < addresses.lastIndex) {
                        HorizontalDivider(
                            thickness = ProtonDimens.Spacing.ExtraTiny,
                            color = ProtonTheme.colors.separatorNorm
                        )
                    }
                }
            }
        }

        BottomNavigationBarSpacer()
    }

}

object ChangeSenderBottomSheetTestTags {

    const val Root = "ChangeSenderBottomSheet"
    const val Item = "ChangeSenderItem"
}

@Preview
@Composable
fun PreviewChangeSenderBottomSheet() {
    ProtonTheme {
        ChangeSenderBottomSheetContent(
            addresses = listOf(SenderUiModel("test1@pm.me"), SenderUiModel("test2@pm.me")),
            current = SenderUiModel("test1@pm.me"),
            onSenderSelected = {}
        )
    }
}
