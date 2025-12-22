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

package ch.protonmail.android.mailpadlocks.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.mailpadlocks.presentation.R

sealed class EncryptionInfoUiModel(
    @DrawableRes val icon: Int,
    @StringRes val summary: Int,
    @StringRes val title: Int,
    @StringRes val description: Int
) {

    data object StoredWithZeroAccessEncryption : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_filled,
        summary = R.string.padlocks_stored_with_zero_access_encryption_summary,
        title = R.string.padlocks_stored_with_zero_access_encryption_summary,
        description = R.string.padlocks_stored_with_zero_access_encryption_description
    )

}
