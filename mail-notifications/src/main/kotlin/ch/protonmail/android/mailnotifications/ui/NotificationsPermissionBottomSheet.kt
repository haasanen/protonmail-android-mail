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

package ch.protonmail.android.mailnotifications.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ch.protonmail.android.design.compose.component.ProtonTextButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.AdaptivePreviews
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailnotifications.R
import ch.protonmail.android.mailnotifications.presentation.model.NotificationsPermissionRequestUiModel
import ch.protonmail.android.mailnotifications.presentation.model.NotificationsPermissionStateType

@Composable
fun NotificationsPermissionBottomSheet(
    onRequest: () -> Unit,
    onDismiss: () -> Unit,
    uiModel: NotificationsPermissionRequestUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(ProtonTheme.colors.backgroundInvertedNorm)
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .padding(horizontal = ProtonDimens.Spacing.Standard)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(id = R.drawable.ic_proton_notification_bell),
            contentDescription = NO_CONTENT_DESCRIPTION
        )

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = uiModel.title.string(),
            style = ProtonTheme.typography.titleLarge,
            color = ProtonTheme.colors.textNorm,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = uiModel.description.string(),
            style = ProtonTheme.typography.bodyLarge,
            color = ProtonTheme.colors.textNorm,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Jumbo))

        Buttons(
            onEnable = onRequest,
            onDismiss = onDismiss
        )

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))
    }
}

@Composable
private fun Buttons(onEnable: () -> Unit, onDismiss: () -> Unit) {
    ProtonTextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.Spacing.Standard)
            .background(
                color = ProtonTheme.colors.brandNorm,
                shape = ProtonTheme.shapes.massive
            ),
        onClick = {
            onEnable()
            onDismiss()
        }
    ) {
        Text(
            text = stringResource(R.string.notification_permissions_bottomsheet_enable),
            style = ProtonTheme.typography.titleMedium,
            color = ProtonTheme.colors.textInverted,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Medium))

    ProtonTextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.Spacing.Standard),
        onClick = { onDismiss() }
    ) {
        Text(
            text = stringResource(R.string.notification_permissions_bottomsheet_dismiss),
            style = ProtonTheme.typography.titleMedium,
            color = ProtonTheme.colors.brandNorm,
            textAlign = TextAlign.Center
        )
    }
}

@AdaptivePreviews
@Composable
private fun PreviewBottomSheetFirstTime() {
    ProtonTheme {
        NotificationsPermissionBottomSheet(
            modifier = Modifier,
            uiModel = NotificationsPermissionStateType.FirstTime.uiModel,
            onRequest = {},
            onDismiss = {}
        )
    }
}

@AdaptivePreviews
@Composable
private fun PreviewBottomSheetSecondTime() {
    ProtonTheme {
        NotificationsPermissionBottomSheet(
            modifier = Modifier,
            uiModel = NotificationsPermissionStateType.SecondTime.uiModel,
            onRequest = {},
            onDismiss = {}
        )
    }
}
