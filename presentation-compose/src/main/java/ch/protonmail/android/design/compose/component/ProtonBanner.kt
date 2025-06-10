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

package ch.protonmail.android.design.compose.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.R
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumInverted
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak

@Composable
fun ProtonBanner(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    iconTint: Color,
    iconSize: Dp?,
    text: String,
    textStyle: TextStyle,
    backgroundColor: Color,
    contentPadding: PaddingValues = PaddingValues(ProtonDimens.Spacing.ModeratelyLarge),
    borderColorIsBackgroundColor: Boolean = false,
    contentAlignedWithText: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    ProtonBanner(
        modifier,
        icon,
        iconTint,
        iconSize,
        AnnotatedString(text),
        textStyle,
        backgroundColor,
        contentPadding,
        borderColorIsBackgroundColor,
        contentAlignedWithText,
        content
    )
}

@Composable
fun ProtonBanner(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    iconTint: Color,
    iconSize: Dp?,
    text: AnnotatedString,
    textStyle: TextStyle,
    backgroundColor: Color,
    contentPadding: PaddingValues = PaddingValues(ProtonDimens.Spacing.ModeratelyLarge),
    borderColorIsBackgroundColor: Boolean = false,
    contentAlignedWithText: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier
            .testTag(ProtonBannerTestTags.ProtonBannerRoot)
            .padding(
                start = contentPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                bottom = contentPadding.calculateBottomPadding()
            )
            .border(
                width = ProtonDimens.BorderSize.Default,
                color = if (borderColorIsBackgroundColor) backgroundColor else ProtonTheme.colors.borderNorm,
                shape = ProtonTheme.shapes.extraLarge
            )
            .background(color = backgroundColor, shape = ProtonTheme.shapes.extraLarge)
            .padding(contentPadding)
    ) {
        Row {
            Icon(
                modifier = Modifier
                    .testTag(ProtonBannerTestTags.ProtonBannerIcon)
                    .size(iconSize ?: Dp.Unspecified),
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Standard))
            if (contentAlignedWithText) {
                Column {
                    Text(
                        modifier = Modifier.testTag(ProtonBannerTestTags.ProtonBannerText),
                        text = text,
                        style = textStyle
                    )
                    content()
                }
            } else {
                Text(
                    modifier = Modifier.testTag(ProtonBannerTestTags.ProtonBannerText),
                    text = text,
                    style = textStyle
                )
            }

        }

        if (!contentAlignedWithText) {
            content()
        }
    }
}


@Composable
fun ProtonBanner(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle,
    backgroundColor: Color,
    borderColorIsBackgroundColor: Boolean = false
) {
    Column(
        modifier = modifier
            .testTag(ProtonBannerTestTags.ProtonBannerRoot)
            .padding(
                start = ProtonDimens.Spacing.ModeratelyLarge,
                end = ProtonDimens.Spacing.ModeratelyLarge,
                bottom = ProtonDimens.Spacing.ModeratelyLarge
            )
            .border(
                width = ProtonDimens.BorderSize.Default,
                color = if (borderColorIsBackgroundColor) backgroundColor else ProtonTheme.colors.borderNorm,
                shape = ProtonTheme.shapes.extraLarge
            )
            .background(color = backgroundColor, shape = ProtonTheme.shapes.extraLarge)
            .padding(ProtonDimens.Spacing.ModeratelyLarge)
    ) {
        Text(
            modifier = Modifier.testTag(ProtonBannerTestTags.ProtonBannerText),
            text = text,
            style = textStyle
        )

    }
}

@Composable
fun ProtonBannerWithButton(
    bannerText: String,
    buttonText: String,
    @DrawableRes icon: Int,
    onButtonClicked: () -> Unit
) {
    ProtonBannerWithButton(
        AnnotatedString(bannerText),
        buttonText,
        icon,
        onButtonClicked
    )
}

@Composable
fun ProtonBannerWithButton(
    bannerText: AnnotatedString,
    buttonText: String,
    @DrawableRes icon: Int,
    onButtonClicked: () -> Unit
) {
    ProtonBanner(
        icon = icon,
        iconTint = ProtonTheme.colors.iconWeak,
        iconSize = ProtonDimens.IconSize.Medium,
        text = bannerText,
        textStyle = ProtonTheme.typography.bodyMediumWeak,
        backgroundColor = ProtonTheme.colors.backgroundNorm
    ) {
        Column {
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
            ProtonButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onButtonClicked() },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = ProtonTheme.colors.interactionWeakNorm,
                    contentColor = ProtonTheme.colors.textNorm
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                shape = ProtonTheme.shapes.huge,
                border = BorderStroke(ProtonDimens.OutlinedBorderSize, ProtonTheme.colors.interactionWeakNorm),
                contentPadding = PaddingValues(
                    horizontal = ProtonDimens.Spacing.Standard,
                    vertical = ProtonDimens.Spacing.Medium
                )
            ) {
                Text(
                    text = buttonText,
                    style = ProtonTheme.typography.bodyMediumNorm
                )
            }
        }
    }
}


@Composable
fun ProtonBannerWithButton(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    iconTint: Color,
    iconSize: Dp?,
    text: String,
    buttonText: String,
    textStyle: TextStyle,
    backgroundColor: Color,
    buttonBackgroundColor: Color,
    onButtonClicked: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(ProtonDimens.Spacing.ModeratelyLarge),
    borderColorIsBackgroundColor: Boolean = false,
    contentAlignedWithText: Boolean = false
) {
    ProtonBanner(
        modifier = modifier,
        icon = icon,
        iconTint = iconTint,
        iconSize = iconSize,
        text = text,
        textStyle = textStyle,
        backgroundColor = backgroundColor,
        contentPadding = contentPadding,
        borderColorIsBackgroundColor = borderColorIsBackgroundColor,
        contentAlignedWithText = contentAlignedWithText
    ) {
        Column {
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
            ProtonButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onButtonClicked,
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = buttonBackgroundColor,
                    contentColor = textStyle.color
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                shape = ProtonTheme.shapes.huge,
                border = null,
                contentPadding = PaddingValues(
                    horizontal = ProtonDimens.Spacing.Standard,
                    vertical = ProtonDimens.Spacing.Medium
                )
            ) {
                Text(
                    text = buttonText,
                    style = textStyle
                )
            }
        }
    }
}

@Composable
fun ProtonBannerWithLink(
    bannerText: String,
    linkText: String,
    iconSize: Dp? = null,
    @DrawableRes icon: Int,
    contentPadding: PaddingValues = PaddingValues(ProtonDimens.Spacing.ModeratelyLarge),
    onLinkClicked: () -> Unit
) {
    ProtonBanner(
        icon = icon,
        iconTint = ProtonTheme.colors.iconWeak,
        iconSize = iconSize,
        text = bannerText,
        textStyle = ProtonTheme.typography.bodyMediumWeak,
        backgroundColor = ProtonTheme.colors.backgroundNorm,
        contentPadding = contentPadding,
        contentAlignedWithText = true
    ) {
        Column {
            ProtonTextButton(
                modifier = Modifier.wrapContentWidth(),
                contentPadding = PaddingValues(0.dp),
                onClick = { onLinkClicked() }
            ) {

                Text(
                    text = linkText,
                    textAlign = TextAlign.Start,
                    style = ProtonTheme.typography.bodyMedium,
                    color = ProtonTheme.colors.textAccent
                )
            }
        }
    }
}

@Preview
@Composable
fun ProtonBannerPreview() {
    ProtonTheme {
        ProtonBanner(
            icon = R.drawable.ic_info_circle,
            iconTint = ProtonTheme.colors.iconWeak,
            iconSize = ProtonDimens.IconSize.Medium,
            text = "Banner text",
            textStyle = ProtonTheme.typography.bodyMediumWeak,
            backgroundColor = ProtonTheme.colors.backgroundNorm
        )
    }
}

@Preview
@Composable
fun ProtonBannerNoIconPreview() {
    ProtonTheme {
        ProtonBanner(
            text = "Banner text",
            textStyle = ProtonTheme.typography.bodyMediumWeak,
            backgroundColor = ProtonTheme.colors.backgroundNorm
        )
    }
}

@Preview
@Composable
fun ProtonBannerWithButtonPreview() {
    ProtonTheme {
        ProtonBannerWithButton(
            bannerText = "Banner text",
            buttonText = "Button text",
            icon = R.drawable.ic_info_circle,
            onButtonClicked = {}
        )
    }
}

@Preview
@Composable
@Suppress("MagicNumber")
fun RedProtonBannerWithButtonPreview() {
    ProtonTheme {
        ProtonBannerWithButton(
            icon = R.drawable.ic_info_circle,
            iconTint = ProtonTheme.colors.iconInverted,
            iconSize = ProtonDimens.IconSize.Medium,
            text = "Banner text",
            buttonText = "Button text",
            textStyle = ProtonTheme.typography.bodyMediumInverted,
            backgroundColor = ProtonTheme.colors.notificationError,
            buttonBackgroundColor = Color(0x33FFFFFF),
            borderColorIsBackgroundColor = true,
            onButtonClicked = {}
        )
    }
}

@Preview
@Composable
fun ProtonBannerWithLinkPreview() {
    ProtonTheme {
        ProtonBannerWithLink(
            bannerText = "Upgrade to automatically delete messages " +
                "that have been in trash or spam for more than 30 days",
            linkText = "Learn more",
            icon = R.drawable.ic_info_circle,
            onLinkClicked = {}
        )
    }
}


object ProtonBannerTestTags {

    const val ProtonBannerRoot = "ProtonBannerRoot"
    const val ProtonBannerIcon = "ProtonBannerIcon"
    const val ProtonBannerText = "ProtonBannerText"
}
