/*
 * Copyright (C) 2024 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.auth.presentation.ui

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import me.proton.core.auth.presentation.R
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.presentation.R as CoreR

private const val SMALL_SCREEN_HEIGHT = 680

@Composable
public fun WelcomeScreenMail(
    actions: WelcomeScreen.Actions,
    modifier: Modifier = Modifier,
) {
    WelcomeScreen(
        welcomeImage = R.drawable.welcome_header_mail,
        appLogoWithName = CoreR.drawable.logo_mail_with_text,
        appNameContentDescription = R.string.app_name_mail,
        subtitleText = R.string.welcome_subtitle_mail,
        actions = actions,
        modifier = modifier,
    )
}

@Composable
public fun WelcomeScreen(
    @DrawableRes welcomeImage: Int,
    @DrawableRes appLogoWithName: Int,
    @StringRes appNameContentDescription: Int,
    @StringRes subtitleText: Int,
    actions: WelcomeScreen.Actions,
    modifier: Modifier = Modifier,
) {
    WelcomeScreen(
        welcomePainter = painterResource(id = welcomeImage),
        appLogoWithNamePainter = painterResource(id = appLogoWithName),
        appNameContentDescription = stringResource(id = appNameContentDescription),
        subtitleText = stringResource(id = subtitleText),
        onSignUpClicked = actions.onSignUpClicked,
        onSignInClicked = actions.onSignInClicked,
        modifier = modifier,
    )
}

@Composable
public fun WelcomeScreen(
    welcomePainter: Painter,
    appLogoWithNamePainter: Painter,
    appNameContentDescription: String,
    subtitleText: String,
    onSignUpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.background(LocalColors.current.backgroundNorm)) {
        val isSmallHeight = maxHeight <= SMALL_SCREEN_HEIGHT.dp

        ConstraintLayout(Modifier.fillMaxSize()) {
            val (welcomeImage, content, footer) = createRefs()
            val (bottomImageSpacer, middleSpacer) = createRefs()
            val contentTopBarrier = createTopBarrier(bottomImageSpacer, middleSpacer)
            val middleGuideline = createGuidelineFromTop(0.5f)
            val bottomGuideline = createGuidelineFromBottom(ProtonDimens.MediumSpacing)

            Image(
                painter = welcomePainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(welcomeImage) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        if (this@BoxWithConstraints.maxWidth > this@BoxWithConstraints.maxHeight) {
                            top.linkTo(parent.top)
                            bottom.linkTo(content.top)
                        } else {
                            top.linkTo(parent.top)
                        }
                    },
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.BottomCenter
            )

            Spacer(modifier = Modifier.constrainAs(middleSpacer) {
                top.linkTo(middleGuideline)
            })
            Spacer(modifier = Modifier.constrainAs(bottomImageSpacer) {
                top.linkTo(welcomeImage.bottom)
            })

            WelcomeScreenContent(
                appLogoWithNamePainter = appLogoWithNamePainter,
                appNameContentDescription = appNameContentDescription,
                subtitleText = subtitleText,
                onSignUpClicked = onSignUpClicked,
                onSignInClicked = onSignInClicked,
                modifier = Modifier
                    .padding(horizontal = ProtonDimens.LargeSpacing)
                    .widthIn(max = 360.dp)
                    .constrainAs(content) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        if (isSmallHeight) {
                            bottom.linkTo(parent.bottom, margin = ProtonDimens.MediumSpacing)
                        } else {
                            top.linkTo(contentTopBarrier, margin = ProtonDimens.LargeSpacing)
                        }
                    }
            )

            if (!isSmallHeight) {
                Image(
                    modifier = Modifier
                        .constrainAs(footer) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(bottomGuideline)
                        },
                    painter = painterResource(id = R.drawable.logo_proton_footer),
                    colorFilter = ColorFilter.tint(LocalColors.current.textHint),
                    contentDescription = stringResource(R.string.welcome_proton_privacy_by_default),
                    alignment = Alignment.Center
                )
            }
        }
    }
}

@Composable
private fun WelcomeScreenContent(
    appLogoWithNamePainter: Painter,
    appNameContentDescription: String,
    subtitleText: String,
    onSignUpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = appLogoWithNamePainter,
            contentDescription = appNameContentDescription,
            alignment = Alignment.Center
        )

        Text(
            text = subtitleText,
            style = ProtonTypography.Default.defaultSmallWeak,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
        )

        ProtonSolidButton(
            contained = false,
            onClick = onSignUpClicked,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.welcome_sign_up))
        }

        ProtonTextButton(
            contained = false,
            onClick = onSignInClicked,
            modifier = Modifier
                .padding(vertical = ProtonDimens.DefaultSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
        ) {
            Text(text = stringResource(R.string.welcome_sign_in))
        }
    }
}

public data object WelcomeScreen {
    public data class Actions(
        val onSignInClicked: () -> Unit,
        val onSignUpClicked: () -> Unit,
    ) {
        public companion object {
            public fun empty(): Actions = Actions(
                onSignInClicked = {},
                onSignUpClicked = {},
            )
        }
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun WelcomeScreenMailPreview() {
    ProtonTheme {
        WelcomeScreenMail(actions = WelcomeScreen.Actions.empty())
    }
}
