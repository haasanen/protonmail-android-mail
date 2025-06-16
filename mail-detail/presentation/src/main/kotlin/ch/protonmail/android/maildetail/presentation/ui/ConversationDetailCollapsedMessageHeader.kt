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

package ch.protonmail.android.maildetail.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import androidx.constraintlayout.compose.atLeast
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.design.compose.theme.bodySmallNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcommon.presentation.compose.OfficialBadge
import ch.protonmail.android.mailcommon.presentation.compose.SmallNonClickableIcon
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMessageUiModel
import ch.protonmail.android.maildetail.presentation.previewdata.ConversationDetailCollapsedMessageHeaderPreviewData
import ch.protonmail.android.maildetail.presentation.ui.common.SingleLineRecipientNames
import ch.protonmail.android.mailmessage.presentation.ui.ParticipantAvatar
import me.proton.core.util.kotlin.EMPTY_STRING
import me.proton.core.util.kotlin.exhaustive

@Composable
internal fun ConversationDetailCollapsedMessageHeader(
    uiModel: ConversationDetailMessageUiModel.Collapsed,
    avatarActions: ParticipantAvatar.Actions,
    modifier: Modifier = Modifier
) {
    val fontWeight = if (uiModel.isUnread) FontWeight.Medium else FontWeight.Normal
    val labelFontWeight = if (uiModel.isUnread) FontWeight.Bold else FontWeight.Normal
    val fontColor = if (uiModel.isUnread) ProtonTheme.colors.textNorm else ProtonTheme.colors.textWeak

    ConstraintLayout(
        modifier = modifier
            .testTag(ConversationDetailCollapsedMessageHeaderTestTags.RootItem)
            .padding(ProtonDimens.Spacing.Large)
            .fillMaxWidth()
    ) {
        val (
            avatarRef,
            forwardedIconRef,
            repliedIconRef,
            senderRef,
            expirationRef,
            labelsRef,
            starIconRef,
            attachmentIconRef,
            timeRef
        ) = createRefs()

        createHorizontalChain(
            avatarRef,
            forwardedIconRef,
            repliedIconRef,
            senderRef,
            expirationRef,
            labelsRef,
            starIconRef,
            attachmentIconRef,
            timeRef,
            chainStyle = ChainStyle.Packed
        )

        ParticipantAvatar(
            modifier = Modifier
                .padding(end = ProtonDimens.Spacing.Large)
                .constrainAs(avatarRef) {
                    centerVerticallyTo(parent)
                },
            avatarUiModel = uiModel.avatar,
            avatarImageUiModel = uiModel.avatarImage,
            actions = avatarActions
        )

        ForwardedIcon(
            modifier = Modifier.constrainAs(forwardedIconRef) {
                centerVerticallyTo(parent)
            },
            uiModel = uiModel,
            fontColor = fontColor
        )

        RepliedIcon(
            modifier = Modifier.constrainAs(repliedIconRef) {
                centerVerticallyTo(parent)
            },
            uiModel = uiModel,
            fontColor = fontColor
        )

        Column(
            modifier = Modifier
                .padding(end = ProtonDimens.Spacing.Small)
                .constrainAs(senderRef) {
                    width = Dimension.preferredWrapContent
                    centerVerticallyTo(parent)
                }
        ) {
            Sender(
                uiModel = uiModel,
                fontWeight = fontWeight,
                fontColor = fontColor
            )

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToRecipientsTitle(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(end = ProtonDimens.Spacing.Small),
                    fontWeight = fontWeight,
                    fontColor = fontColor
                )

                SingleLineRecipientNames(
                    modifier = Modifier
                        .wrapContentHeight(),
                    recipients = uiModel.recipients,
                    hasUndisclosedRecipients = uiModel.shouldShowUndisclosedRecipients,
                    textStyle = ProtonTheme.typography.bodyMediumNorm,
                    fontColor = fontColor,
                    fontWeight = fontWeight
                )
            }
        }

        Expiration(
            modifier = Modifier.constrainAs(expirationRef) {
                visibility = visibleWhen(uiModel.expiration != null)
                centerVerticallyTo(parent)
            },
            uiModel = uiModel
        )

        Text(
            modifier = Modifier.constrainAs(labelsRef) {
                visibility = Visibility.Invisible
                width = Dimension.fillToConstraints.atLeast(1.dp)
                centerVerticallyTo(parent)
            },
            text = "Labels",
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        StarIcon(
            modifier = Modifier.constrainAs(starIconRef) {
                visibility = visibleWhen(uiModel.isStarred)
                centerVerticallyTo(parent)
            }
        )

        AttachmentIcon(
            fontColor = fontColor,
            modifier = Modifier.constrainAs(attachmentIconRef) {
                visibility = visibleWhen(uiModel.hasAttachments)
                centerVerticallyTo(parent)
            }
        )

        Time(
            modifier = Modifier.constrainAs(timeRef) {
                centerVerticallyTo(parent)
            },
            uiModel = uiModel,
            fontWeight = labelFontWeight,
            fontColor = fontColor
        )
    }
}

@Composable
private fun ToRecipientsTitle(
    modifier: Modifier = Modifier,
    fontWeight: FontWeight,
    fontColor: Color
) {
    Text(
        modifier = modifier,
        text = stringResource(id = R.string.to),
        style = ProtonTheme.typography.bodyMedium,
        fontWeight = fontWeight,
        color = fontColor
    )
}

@Composable
private fun AttachmentIcon(fontColor: Color, modifier: Modifier) {
    Icon(
        modifier = modifier
            .testTag(ConversationDetailCollapsedMessageHeaderTestTags.AttachmentIcon)
            .size(ProtonDimens.IconSize.Small),
        painter = painterResource(id = R.drawable.ic_proton_paper_clip),
        tint = fontColor,
        contentDescription = NO_CONTENT_DESCRIPTION
    )
}

@Composable
private fun Expiration(uiModel: ConversationDetailMessageUiModel.Collapsed, modifier: Modifier) {
    Row(
        modifier = modifier
            .padding(horizontal = ProtonDimens.Spacing.Small)
            .background(
                color = ProtonTheme.colors.interactionWeakNorm, shape = ProtonTheme.shapes.large
            )
            .padding(ProtonDimens.Spacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .testTag(ConversationDetailCollapsedMessageHeaderTestTags.ExpirationIcon)
                .size(MailDimens.TinyIcon),
            painter = painterResource(id = R.drawable.ic_proton_hourglass),
            tint = ProtonTheme.colors.iconNorm,
            contentDescription = NO_CONTENT_DESCRIPTION
        )
        Text(
            modifier = Modifier.testTag(ConversationDetailCollapsedMessageHeaderTestTags.ExpirationText),
            text = uiModel.expiration?.string() ?: EMPTY_STRING,
            style = ProtonTheme.typography.bodySmallNorm
        )
    }
}

@Composable
private fun ForwardedIcon(
    uiModel: ConversationDetailMessageUiModel.Collapsed,
    fontColor: Color,
    modifier: Modifier
) {
    when (uiModel.forwardedIcon) {
        ConversationDetailMessageUiModel.ForwardedIcon.None -> Box(modifier)
        ConversationDetailMessageUiModel.ForwardedIcon.Forwarded -> SmallNonClickableIcon(
            modifier = modifier.testTag(ConversationDetailCollapsedMessageHeaderTestTags.ForwardedIcon),
            iconId = R.drawable.ic_proton_arrow_right,
            iconColor = fontColor
        )
    }.exhaustive
}

@Composable
private fun RepliedIcon(
    uiModel: ConversationDetailMessageUiModel.Collapsed,
    fontColor: Color,
    modifier: Modifier
) {
    when (uiModel.repliedIcon) {
        ConversationDetailMessageUiModel.RepliedIcon.None -> Box(modifier)
        ConversationDetailMessageUiModel.RepliedIcon.Replied -> SmallNonClickableIcon(
            modifier = modifier
                .testTag(ConversationDetailCollapsedMessageHeaderTestTags.RepliedIcon)
                .padding(horizontal = ProtonDimens.Spacing.Tiny),
            iconId = R.drawable.ic_proton_arrow_up_and_left,
            iconColor = fontColor
        )

        ConversationDetailMessageUiModel.RepliedIcon.RepliedAll -> SmallNonClickableIcon(
            modifier = modifier
                .testTag(ConversationDetailCollapsedMessageHeaderTestTags.RepliedAllIcon)
                .padding(horizontal = ProtonDimens.Spacing.Tiny),
            iconId = R.drawable.ic_proton_arrows_up_and_left,
            iconColor = fontColor
        )
    }.exhaustive
}

@Composable
private fun Sender(
    uiModel: ConversationDetailMessageUiModel.Collapsed,
    fontWeight: FontWeight,
    fontColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = modifier
                .testTag(ConversationDetailCollapsedMessageHeaderTestTags.Sender)
                .weight(1f, fill = false),
            text = uiModel.sender.participantName,
            fontWeight = fontWeight,
            color = fontColor,
            style = ProtonTheme.typography.bodyLargeNorm,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        if (uiModel.sender.shouldShowOfficialBadge) {
            OfficialBadge()
        }
    }
}

@Composable
private fun StarIcon(modifier: Modifier) {
    Icon(
        modifier = modifier
            .testTag(ConversationDetailCollapsedMessageHeaderTestTags.StarIcon)
            .size(ProtonDimens.IconSize.Small),
        painter = painterResource(id = R.drawable.ic_proton_star_filled),
        tint = ProtonTheme.colors.starSelected,
        contentDescription = NO_CONTENT_DESCRIPTION
    )
}

@Composable
private fun Time(
    uiModel: ConversationDetailMessageUiModel.Collapsed,
    fontWeight: FontWeight,
    fontColor: Color,
    modifier: Modifier
) {
    Text(
        modifier = modifier
            .testTag(ConversationDetailCollapsedMessageHeaderTestTags.Time)
            .padding(start = ProtonDimens.Spacing.Small),
        text = uiModel.shortTime.string(),
        fontWeight = fontWeight,
        color = fontColor,
        style = ProtonTheme.typography.bodySmallNorm,
        maxLines = 1
    )
}

private fun visibleWhen(isVisible: Boolean) = if (isVisible) Visibility.Visible else Visibility.Gone

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun CdCollapsedMessageHeaderPreview(
    @PreviewParameter(ConversationDetailCollapsedMessageHeaderPreviewData::class)
    uiModel: ConversationDetailMessageUiModel.Collapsed
) {
    ProtonTheme {
        ProtonTheme {
            ConversationDetailCollapsedMessageHeader(
                uiModel = uiModel,
                avatarActions = ParticipantAvatar.Actions.Empty
            )
        }
    }
}

object ConversationDetailCollapsedMessageHeaderTestTags {

    const val RootItem = "ConversationDetailCollapsedMessageHeaderRootItem"
    const val AttachmentIcon = "AttachmentIcon"
    const val ForwardedIcon = "ForwardedIcon"
    const val RepliedIcon = "RepliedIcon"
    const val RepliedAllIcon = "RepliedAllIcon"
    const val StarIcon = "StarIcon"
    const val Sender = "Sender"
    const val ExpirationIcon = "ExpirationIcon"
    const val ExpirationText = "ExpirationText"
    const val Location = "Location"
    const val Time = "Time"
}
