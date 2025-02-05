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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeWeak
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.model.ActionUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import kotlinx.collections.immutable.ImmutableList


@Composable
fun ActionGroup(
    modifier: Modifier = Modifier,
    actionUiModels: ImmutableList<ActionUiModel>,
    onActionClicked: (ActionUiModel) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            actionUiModels.forEachIndexed { index, action ->
                ActionGroupItem(
                    action = action,
                    onClick = { onActionClicked(action) }
                )

                if (index < actionUiModels.lastIndex) {
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
internal fun ActionGroupItem(
    modifier: Modifier = Modifier,
    action: ActionUiModel,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                vertical = ProtonDimens.Spacing.Large,
                horizontal = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .testTag(MoreActionsBottomSheetTestTags.ActionItem)
                .padding(end = ProtonDimens.Spacing.Large),
            painter = painterResource(id = action.icon),
            contentDescription = NO_CONTENT_DESCRIPTION
        )
        Text(
            modifier = Modifier
                .testTag(MoreActionsBottomSheetTestTags.LabelIcon)
                .weight(1f),
            text = action.description.string(),
            style = ProtonTheme.typography.bodyLargeWeak,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
