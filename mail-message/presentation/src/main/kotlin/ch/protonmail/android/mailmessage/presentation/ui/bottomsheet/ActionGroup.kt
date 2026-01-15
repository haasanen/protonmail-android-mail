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

package ch.protonmail.android.mailmessage.presentation.ui.bottomsheet

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T> ActionGroup(
    modifier: Modifier = Modifier,
    items: ImmutableList<T>,
    onItemClicked: (T) -> Unit,
    content: @Composable (item: T, onClick: () -> Unit) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                content(item) { onItemClicked(item) }

                if (index < items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(0.dp),
                        thickness = 1.dp,
                        color = ProtonTheme.colors.separatorNorm
                    )
                }
            }
        }
    }
}

@Composable
fun ActionGroupItem(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    description: String,
    contentDescription: String,
    onClick: () -> Unit,
    secondaryContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                vertical = ProtonDimens.Spacing.Large,
                horizontal = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .testTag(MoreActionsBottomSheetTestTags.ActionItem)
                    .padding(end = ProtonDimens.Spacing.Large),
                painter = painterResource(id = icon),
                contentDescription = contentDescription
            )
            Column(
                modifier = Modifier
                    .testTag(MoreActionsBottomSheetTestTags.LabelIcon)
                    .weight(1f)
            ) {
                Text(
                    text = description,
                    style = ProtonTheme.typography.bodyLargeNorm,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (secondaryContent != null) {
                    Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Small))
                    secondaryContent()
                }
            }
        }
    }
}
