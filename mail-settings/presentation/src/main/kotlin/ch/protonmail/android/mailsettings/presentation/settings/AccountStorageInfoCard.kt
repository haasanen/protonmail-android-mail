/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsettings.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import ch.protonmail.android.design.compose.component.ProtonMainSettingsItem
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailsession.presentation.model.StorageQuotaUiModel
import ch.protonmail.android.mailsettings.presentation.R
import ch.protonmail.android.mailsettings.presentation.R.string
import ch.protonmail.android.mailsettings.presentation.settings.previewprovider.AccountStorageInfoCardPreviewParameterProvider

@Composable
fun AccountStorageInfoCard(
    modifier: Modifier = Modifier,
    storageQuotaUiModel: StorageQuotaUiModel,
    onClick: () -> Unit
) {
    val storageIndicatorColor = if (storageQuotaUiModel.isAboveAlertThreshold) {
        ProtonTheme.colors.notificationError
    } else {
        ProtonTheme.colors.notificationSuccess
    }

    val nameColor = if (storageQuotaUiModel.isAboveAlertThreshold) {
        ProtonTheme.colors.notificationError
    } else {
        ProtonTheme.colors.textWeak
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ProtonTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {

        Column {
            ProtonMainSettingsItem(
                name = stringResource(
                    string.mail_settings_storage_quota,
                    storageQuotaUiModel.usagePercent.roundToInt(),
                    storageQuotaUiModel.maxStorage
                ),
                nameColor = nameColor,
                iconRes = R.drawable.ic_proton_storage,
                iconColor = storageIndicatorColor,
                onClick = onClick
            )
        }
    }
}

@Composable
@Preview
fun AccountStorageInfoCardPreview(
    @PreviewParameter(AccountStorageInfoCardPreviewParameterProvider::class) uiModel: StorageQuotaUiModel
) {
    ProtonTheme {
        AccountStorageInfoCard(
            storageQuotaUiModel = uiModel,
            onClick = {}
        )
    }
}
