/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailpadlocks.presentation

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import ch.protonmail.android.mailpadlocks.domain.PrivacyLock
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockColor
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockIcon
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockTooltip
import ch.protonmail.android.mailpadlocks.presentation.mapper.EncryptionInfoUiModelMapper
import ch.protonmail.android.mailpadlocks.presentation.model.EncryptionInfoUiModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class EncryptionInfoBottomSheetSnapshotTest(
    private val tooltip: PrivacyLockTooltip
) {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
        theme = "ProtonTheme"
    )

    @Test
    fun encryptionInfoBottomSheet() {
        val privacyLock = PrivacyLock.Value(
            icon = PrivacyLockIcon.ClosedLock,
            color = PrivacyLockColor.Blue,
            tooltip = tooltip
        )
        val uiModel = EncryptionInfoUiModelMapper.fromPrivacyLock(privacyLock)
            as EncryptionInfoUiModel.WithLock

        paparazzi.snapshot {
            EncryptionInfoBottomSheetContent(
                state = EncryptionInfoSheetState.Requested(uiModel),
                onDismissed = {}
            )
        }
    }

    companion object {

        @Parameters(name = "{0}")
        @JvmStatic
        fun tooltips() = PrivacyLockTooltip.entries
            .filter { it != PrivacyLockTooltip.None }
    }
}
