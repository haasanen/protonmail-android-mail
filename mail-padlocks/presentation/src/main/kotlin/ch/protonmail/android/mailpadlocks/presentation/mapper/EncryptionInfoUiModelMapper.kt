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

package ch.protonmail.android.mailpadlocks.presentation.mapper

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.mailpadlocks.domain.PrivacyLock
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockColor
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockIcon
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockTooltip
import ch.protonmail.android.mailpadlocks.presentation.R
import ch.protonmail.android.mailpadlocks.presentation.model.EncryptionInfoUiModel

internal object EncryptionInfoUiModelMapper {

    fun fromPrivacyLock(privacyLock: PrivacyLock): EncryptionInfoUiModel = when (privacyLock) {
        is PrivacyLock.None -> EncryptionInfoUiModel.NoLock
        is PrivacyLock.Value -> {
            val icon = resolveIcon(privacyLock.icon)
            val color = resolveColor(privacyLock.color)
            val tooltipValues = resolveTooltip(privacyLock.tooltip)

            EncryptionInfoUiModel.WithLock(
                icon = icon,
                color = color,
                link = tooltipValues.link,
                title = tooltipValues.title,
                description = tooltipValues.description
            )
        }
    }

    @DrawableRes
    private fun resolveIcon(privacyLockIcon: PrivacyLockIcon): Int {
        return when (privacyLockIcon) {
            PrivacyLockIcon.ClosedLock -> R.drawable.ic_lock_filled
            PrivacyLockIcon.ClosedLockWithTick -> R.drawable.ic_lock_check_filled
            PrivacyLockIcon.ClosedLockWithPen -> R.drawable.ic_lock_pen_filled
            PrivacyLockIcon.ClosedLockWarning -> R.drawable.ic_lock_exclamation_filled
            PrivacyLockIcon.OpenLockWithPen -> R.drawable.ic_lock_open_pen_filled
            PrivacyLockIcon.OpenLockWithTick -> R.drawable.ic_lock_open_check_filled
            PrivacyLockIcon.OpenLockWarning -> R.drawable.ic_lock_open_exclamation_filled
        }
    }

    @ColorRes
    private fun resolveColor(privacyLockColor: PrivacyLockColor): Int {
        return when (privacyLockColor) {
            PrivacyLockColor.Black -> R.color.padlock_black
            PrivacyLockColor.Green -> R.color.padlock_green
            PrivacyLockColor.Blue -> R.color.padlock_blue
        }
    }

    private fun resolveTooltip(privacyLockTooltip: PrivacyLockTooltip): TooltipValues {
        // Copy values still being finalized, the following is a placeholder.
        return when (privacyLockTooltip) {
            else -> TooltipValues(
                link = R.string.padlocks_stored_with_zero_access_encryption_link,
                title = R.string.padlocks_stored_with_zero_access_encryption_summary,
                description = R.string.padlocks_stored_with_zero_access_encryption_description
            )
        }
    }

    private data class TooltipValues(
        @StringRes val link: Int,
        @StringRes val title: Int,
        @StringRes val description: Int
    )
}
