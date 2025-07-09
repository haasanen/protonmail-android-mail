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

package ch.protonmail.android.mailmessage.presentation.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.R
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcommon.presentation.compose.dpToPx
import ch.protonmail.android.mailcommon.presentation.model.AvatarImageUiModel
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun ParticipantAvatar(
    modifier: Modifier = Modifier,
    avatarUiModel: AvatarUiModel,
    avatarImageUiModel: AvatarImageUiModel,
    actions: ParticipantAvatar.Actions = ParticipantAvatar.Actions.Empty,
    clickable: Boolean = avatarUiModel !is AvatarUiModel.DraftIcon,
    outerContainerSize: Dp = MailDimens.AvatarSize,
    avatarSize: Dp = MailDimens.AvatarSize,
    backgroundShape: Shape = ProtonTheme.shapes.large
) {
    Box(
        modifier = modifier
            .testTag(AvatarTestTags.AvatarRootItem)
            .size(outerContainerSize)
            .clip(backgroundShape)
            .run {
                if (clickable) {
                    clickable(onClick = actions.onAvatarClicked)
                } else {
                    this // Return the current modifier unchanged if not clickable
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (avatarUiModel) {
            is AvatarUiModel.DraftIcon -> ParticipantAvatarDraftIcon(avatarSize)
            is AvatarUiModel.ParticipantAvatar -> {
                Crossfade(
                    targetState = avatarUiModel.selected,
                    label = avatarUiModel.address
                ) { isSelected ->
                    if (isSelected) {
                        ParticipantAvatarSelected(
                            avatarUiModel = avatarUiModel,
                            avatarSize = avatarSize,
                            backgroundShape = backgroundShape
                        )
                    } else {
                        ParticipantAvatarNotSelected(
                            avatarUiModel = avatarUiModel,
                            avatarImageUiModel = avatarImageUiModel,
                            actions = actions,
                            avatarSize = avatarSize,
                            backgroundShape = backgroundShape
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantAvatarDraftIcon(avatarSize: Dp) {
    Box(
        modifier = Modifier
            .testTag(AvatarTestTags.AvatarDraft)
            .sizeIn(
                minWidth = avatarSize,
                minHeight = avatarSize
            )
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(ProtonDimens.CornerRadius.MediumLarge)
            )
            .border(
                width = 1.dp,
                color = ProtonTheme.colors.borderNorm,
                shape = ProtonTheme.shapes.large
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.padding(ProtonDimens.Spacing.MediumLight),
            painter = painterResource(id = R.drawable.ic_proton_pen_square),
            contentDescription = NO_CONTENT_DESCRIPTION,
            tint = ProtonTheme.colors.textHint
        )
    }
}

@Composable
fun ParticipantAvatarNotSelected(
    avatarUiModel: AvatarUiModel.ParticipantAvatar,
    avatarImageUiModel: AvatarImageUiModel,
    actions: ParticipantAvatar.Actions,
    avatarSize: Dp,
    backgroundShape: Shape
) {

    LaunchedEffect(avatarImageUiModel) {
        if (avatarImageUiModel is AvatarImageUiModel.NotLoaded) {
            actions.onAvatarImageLoadRequested(avatarUiModel)
        }
    }

    when (avatarImageUiModel) {
        is AvatarImageUiModel.Data -> SenderImageAvatar(
            avatarUiModel, avatarImageUiModel, avatarSize, backgroundShape, actions
        )

        else -> SenderInitialsAvatar(avatarUiModel.initial, avatarSize, avatarUiModel.color, backgroundShape)

    }
}

@Composable
fun ParticipantAvatarSelected(
    avatarUiModel: AvatarUiModel.ParticipantAvatar,
    avatarSize: Dp,
    backgroundShape: Shape
) {
    Box(
        modifier = Modifier
            .sizeIn(
                minWidth = avatarSize,
                minHeight = avatarSize
            )
            .background(
                color = ProtonTheme.colors.brandNorm,
                shape = backgroundShape
            )
            .testTag(AvatarTestTags.AvatarSelectionMode)
            .semantics { selected = avatarUiModel.selected },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_proton_checkmark),
            tint = ProtonTheme.colors.iconInverted,
            contentDescription = NO_CONTENT_DESCRIPTION,
            modifier = Modifier.size(MailDimens.AvatarCheckmarkSize)
        )
    }
}

@Composable
private fun SenderImageAvatar(
    avatarUiModel: AvatarUiModel.ParticipantAvatar,
    avatarImageUiModel: AvatarImageUiModel.Data,
    avatarSize: Dp,
    backgroundShape: Shape,
    actions: ParticipantAvatar.Actions
) {
    val context = LocalContext.current
    val avatarSizePx = avatarSize.dpToPx()

    // If we do not provide our own cache key, Coil will make disk IO to access File to create a cache key
    val imageUri = avatarImageUiModel.imageFile.toUri()
    val imageRequest = remember(imageUri) {
        ImageRequest.Builder(context)
            .data(imageUri)
            .memoryCacheKey(imageUri.toString())
            .size(avatarSizePx)
            .listener(
                onError = { _, _ ->
                    actions.onAvatarImageLoadFailed()
                }
            )
            .build()
    }

    SubcomposeAsyncImage(
        modifier = Modifier.size(avatarSize),
        model = imageRequest,
        contentDescription = "",
        contentScale = ContentScale.Fit,
        loading = {
            SenderInitialsAvatar(avatarUiModel.initial, avatarSize, avatarUiModel.color, backgroundShape)
        },
        error = {
            SenderInitialsAvatar(avatarUiModel.initial, avatarSize, avatarUiModel.color, backgroundShape)
        }
    )
}

@Composable
private fun SenderInitialsAvatar(
    initials: String,
    avatarSize: Dp,
    color: Color,
    shape: Shape
) {
    Box(
        modifier = Modifier
            .size(avatarSize)
            .background(color = color, shape = shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .testTag(ch.protonmail.android.mailcommon.presentation.compose.AvatarTestTags.AvatarText),
            textAlign = TextAlign.Center,
            text = initials,
            style = ProtonTheme.typography.bodyLargeNorm,
            fontWeight = FontWeight.Bold,
            color = Color.White

        )
    }
}

object ParticipantAvatar {
    data class Actions(
        val onAvatarClicked: () -> Unit,
        val onAvatarImageLoadRequested: (AvatarUiModel) -> Unit,
        val onAvatarImageLoadFailed: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onAvatarClicked = {},
                onAvatarImageLoadRequested = {},
                onAvatarImageLoadFailed = {}
            )
        }
    }
}

object AvatarTestTags {

    const val AvatarRootItem = "AvatarRootItem"
    const val AvatarDraft = "AvatarDraft"
    const val AvatarSelectionMode = "AvatarSelectionMode"
}
