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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.titleSmallNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.R
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    avatarUiModel: AvatarUiModel,
    onClick: () -> Unit = {},
    clickable: Boolean = true,
    outerContainerSize: Dp = MailDimens.DefaultTouchTargetSize,
    avatarSize: Dp = MailDimens.AvatarMinSize,
    backgroundShape: Shape = ProtonTheme.shapes.large
) {
    Box(
        modifier = modifier
            .testTag(AvatarTestTags.AvatarRootItem)
            .size(outerContainerSize)
            .clip(backgroundShape)
            .run {
                if (clickable) {
                    clickable(onClick = onClick)
                } else {
                    this // Return the current modifier unchanged if not clickable
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (avatarUiModel) {
            is AvatarUiModel.DraftIcon -> ParticipantAvatarDraftIcon(avatarSize, backgroundShape)
            is AvatarUiModel.ParticipantAvatar -> {
                if (avatarUiModel.selected) {
                    ParticipantAvatarSelected(avatarSize, backgroundShape)
                } else {
                    ParticipantAvatar(avatarUiModel)
                }
            }
        }
    }
}

@Composable
private fun ParticipantAvatarDraftIcon(avatarSize: Dp, backgroundShape: Shape) {
    Box(
        modifier = Modifier
            .testTag(AvatarTestTags.AvatarDraft)
            .sizeIn(minWidth = avatarSize, minHeight = avatarSize)
            .border(
                width = MailDimens.DefaultBorder,
                color = ProtonTheme.colors.interactionWeakNorm,
                shape = backgroundShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(ProtonDimens.IconSize.Small),
            painter = painterResource(id = R.drawable.ic_proton_pencil),
            contentDescription = NO_CONTENT_DESCRIPTION
        )
    }
}

@Composable
private fun ParticipantAvatarSelected(avatarSize: Dp, backgroundShape: Shape) {
    Box(
        modifier = Modifier
            .sizeIn(minWidth = avatarSize, minHeight = avatarSize)
            .border(
                width = MailDimens.AvatarBorderLine,
                color = ProtonTheme.colors.interactionBrandDefaultNorm,
                shape = backgroundShape
            )
            .background(
                color = ProtonTheme.colors.interactionBrandDefaultNorm,
                shape = backgroundShape
            )
            .testTag(AvatarTestTags.AvatarSelectionMode),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_proton_checkmark),
            tint = Color.White,
            contentDescription = NO_CONTENT_DESCRIPTION,
            modifier = Modifier.size(MailDimens.AvatarCheckmarkSize)
        )
    }
}

@Composable
fun ParticipantAvatar(avatarUiModel: AvatarUiModel.ParticipantAvatar) {
    Box(
        modifier = Modifier
            .sizeIn(
                minWidth = MailDimens.AvatarSize,
                minHeight = MailDimens.AvatarSize
            )
            .background(
                color = avatarUiModel.color,
                shape = ProtonTheme.shapes.large
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .testTag(AvatarTestTags.AvatarText),
            textAlign = TextAlign.Center,
            style = ProtonTheme.typography.titleSmallNorm,
            color = Color.White,
            text = avatarUiModel.initial
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewParticipantAvatar() {
    val sampleAvatar = AvatarUiModel.ParticipantAvatar(
        initial = "A",
        address = "example@example.com",
        bimiSelector = null,
        color = Color.Blue
    )

    ParticipantAvatar(avatarUiModel = sampleAvatar)
}


object AvatarTestTags {

    const val AvatarRootItem = "AvatarRootItem"
    const val AvatarText = "AvatarText"
    const val AvatarDraft = "AvatarDraft"
    const val AvatarSelectionMode = "AvatarSelectionMode"
}
