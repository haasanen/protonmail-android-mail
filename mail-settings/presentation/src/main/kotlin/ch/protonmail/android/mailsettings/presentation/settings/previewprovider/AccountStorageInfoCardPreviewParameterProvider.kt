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

package ch.protonmail.android.mailsettings.presentation.settings.previewprovider

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import ch.protonmail.android.mailsession.domain.model.Percent
import ch.protonmail.android.mailsession.presentation.model.StorageQuotaUiModel

class AccountStorageInfoCardPreviewParameterProvider : PreviewParameterProvider<StorageQuotaUiModel> {

    override val values: Sequence<StorageQuotaUiModel> = State.entries.map { it.uiModel }.asSequence()

    enum class State(val uiModel: StorageQuotaUiModel) {
        BELOW_THRESHOLD(
            uiModel = StorageQuotaUiModel(
                usagePercent = Percent(50.0),
                maxStorage = "100 MB",
                isAboveAlertThreshold = false
            )
        ),
        ABOVE_THRESHOLD(
            uiModel = StorageQuotaUiModel(
                usagePercent = Percent(90.0),
                maxStorage = "15 GB",
                isAboveAlertThreshold = true
            )
        )
    }
}
