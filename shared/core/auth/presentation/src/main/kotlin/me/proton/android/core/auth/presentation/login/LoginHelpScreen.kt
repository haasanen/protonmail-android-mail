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

@file:Suppress("UseComposableActions")

package me.proton.android.core.auth.presentation.login

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.widget.ProtonCloseButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R as CoreR

@Composable
public fun LoginHelpScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onCustomerSupportClicked: () -> Unit = {},
    onForgotPasswordClicked: () -> Unit = {},
    onForgotUsernameClicked: () -> Unit = {},
    onOtherLoginIssuesClicked: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = { ProtonCloseButton(onCloseClicked = onCloseClicked) },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxWidth()) {
            HelpColumn(
                onCustomerSupportClicked = onCustomerSupportClicked,
                onForgotPasswordClicked = onForgotPasswordClicked,
                onForgotUsernameClicked = onForgotUsernameClicked,
                onOtherLoginIssuesClicked = onOtherLoginIssuesClicked,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun HelpColumn(
    modifier: Modifier = Modifier,
    onCustomerSupportClicked: () -> Unit = {},
    onForgotPasswordClicked: () -> Unit = {},
    onForgotUsernameClicked: () -> Unit = {},
    onOtherLoginIssuesClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.login_help_title),
            style = LocalTypography.current.headline,
            modifier = Modifier.padding(
                horizontal = ProtonDimens.DefaultSpacing,
                vertical = ProtonDimens.MediumSpacing
            )
        )
        HelpItem(
            icon = CoreR.drawable.ic_proton_user_circle,
            text = R.string.login_help_forgot_username,
            onClick = onForgotUsernameClicked
        )
        HelpItem(
            icon = CoreR.drawable.ic_proton_key,
            text = R.string.login_help_forgot_password,
            onClick = onForgotPasswordClicked
        )
        HelpItem(
            icon = CoreR.drawable.ic_proton_question_circle,
            text = R.string.login_help_other_issues,
            onClick = onOtherLoginIssuesClicked
        )
        Text(
            text = stringResource(id = R.string.login_help_contact_needed),
            style = LocalTypography.current.body2Regular,
            modifier = Modifier.padding(
                start = ProtonDimens.DefaultSpacing,
                end = ProtonDimens.DefaultSpacing,
                top = ProtonDimens.LargerSpacing
            )
        )
        HelpItem(
            icon = painterResource(id = CoreR.drawable.ic_proton_speech_bubble),
            text = stringResource(id = R.string.login_help_customer_support),
            onClick = onCustomerSupportClicked,
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
        )
        Spacer(
            modifier = Modifier
                .height(ProtonDimens.MediumSpacing)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun HelpItem(
    @DrawableRes icon: Int,
    @StringRes text: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HelpItem(
        icon = painterResource(id = icon),
        text = stringResource(id = text),
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun HelpItem(
    icon: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .padding(ProtonDimens.DefaultSpacing)
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(ProtonDimens.DefaultIconSize),
                colorFilter = ColorFilter.tint(LocalColors.current.iconNorm)
            )
            Text(
                text = text,
                style = LocalTypography.current.body1Regular,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = ProtonDimens.DefaultSpacing)
            )
        }
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun LoginHelpScreenPreview() {
    ProtonTheme {
        LoginHelpScreen()
    }
}
