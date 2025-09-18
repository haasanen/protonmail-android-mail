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

package me.proton.android.core.humanverification.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import me.proton.core.compose.component.ProtonCloseButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headlineSmallNorm

import me.proton.core.presentation.R as CoreR

@Composable
@Suppress("UseComposableActions")
fun HumanVerificationHelpScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onRequestInviteClicked: () -> Unit = {},
    onVisitHelpCenterClicked: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ProtonTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.human_verification_help),
                        style = ch.protonmail.android.design.compose.theme.ProtonTheme.typography.titleMediumNorm
                    )
                },
                navigationIcon = { ProtonCloseButton(onCloseClicked = onCloseClicked) },
                backgroundColor = LocalColors.current.backgroundNorm,
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxWidth()) {
            HelpColumn(
                onRequestInviteClicked = onRequestInviteClicked,
                onVisitHelpCenterClicked = onVisitHelpCenterClicked,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun HelpColumn(
    modifier: Modifier = Modifier,
    onRequestInviteClicked: () -> Unit = {},
    onVisitHelpCenterClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.human_verification_help_title),
            style = LocalTypography.current.headlineSmallNorm,
            modifier = Modifier.padding(
                horizontal = ProtonDimens.DefaultSpacing,
                vertical = ProtonDimens.MediumSpacing
            )
        )
        HumanVerificationHelpItem(
            icon = CoreR.drawable.ic_proton_user_circle,
            title = R.string.human_verification_request_invite_title,
            description = R.string.human_verification_request_invite_description,
            onClick = onRequestInviteClicked
        )
        HumanVerificationHelpItem(
            icon = CoreR.drawable.ic_proton_key,
            title = R.string.human_verification_visit_help_center_title,
            description = R.string.human_verification_visit_help_center_description,
            onClick = onVisitHelpCenterClicked
        )
    }
}

@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
internal fun HumanVerificationHelpScreenPreview() {
    ProtonTheme {
        HumanVerificationHelpScreen()
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun HumanVerificationHelpScreenDarkPreview() {
    ProtonTheme {
        HumanVerificationHelpScreen()
    }
}
