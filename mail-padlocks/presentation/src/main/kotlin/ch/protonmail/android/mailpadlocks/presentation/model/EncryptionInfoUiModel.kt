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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.mailpadlocks.presentation.R

sealed class EncryptionInfoUiModel(
    @DrawableRes val icon: Int,
    @ColorRes val color: Int,
    @StringRes val link: Int,
    @StringRes val title: Int,
    @StringRes val description: Int
) {

    data object StoredWithZeroAccessEncryption : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_filled,
        color = R.color.charade,
        link = R.string.padlocks_stored_with_zero_access_encryption_link,
        title = R.string.padlocks_stored_with_zero_access_encryption_summary,
        description = R.string.padlocks_stored_with_zero_access_encryption_description
    )

    data object ProtonE2ee : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_filled,
        color = R.color.padlock_blue,
        link = R.string.padlocks_e2ee_link,
        title = R.string.padlocks_proton_e2ee,
        description = R.string.padlocks_proton_e2ee_description
    )

    data object ProtonE2eeVerifiedContact : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_check_filled,
        color = R.color.padlock_blue,
        link = R.string.padlocks_e2ee_link,
        title = R.string.padlocks_proton_e2ee_verified_sender,
        description = R.string.padlocks_proton_e2ee_verified_sender_description
    )

    data object ProtonE2eeWithFailedVerification : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_exclamation_filled,
        color = R.color.padlock_blue,
        link = R.string.padlocks_e2ee_link,
        title = R.string.padlocks_proton_e2ee_failed_verification,
        description = R.string.padlocks_proton_e2ee_failed_verification_description
    )

    data object PgpE2ee : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_filled,
        color = R.color.padlock_green,
        link = R.string.padlocks_e2ee_link,
        title = R.string.padlocks_pgp_e2ee,
        description = R.string.padlocks_pgp_e2ee_description
    )

    data object PgpE2eeSigned : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_pen_filled,
        color = R.color.padlock_green,
        link = R.string.padlocks_e2ee_link,
        title = R.string.padlocks_pgp_e2ee_signed,
        description = R.string.padlocks_pgp_e2ee_signed_description
    )

    data object PgpE2eeVerifiedContact : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_check_filled,
        color = R.color.padlock_green,
        link = R.string.padlocks_e2ee_link,
        title = R.string.padlocks_pgp_e2ee_verified_contact,
        description = R.string.padlocks_pgp_e2ee_verified_contact_description
    )

    data object PgpE2eeWithFailedVerification : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_exclamation_filled,
        color = R.color.padlock_green,
        link = R.string.padlocks_e2ee_link,
        title = R.string.padlocks_pgp_e2ee_failed_verification,
        description = R.string.padlocks_pgp_e2ee_failed_verification_description
    )

    data object PgpSigned : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_open_pen_filled,
        color = R.color.padlock_green,
        link = R.string.padlocks_signed_link,
        title = R.string.padlocks_pgp_signed,
        description = R.string.padlocks_pgp_signed_description
    )

    data object PgpSignedVerifiedContact : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_open_check_filled,
        color = R.color.padlock_green,
        link = R.string.padlocks_signed_link,
        title = R.string.padlocks_pgp_signed_verified_contact,
        description = R.string.padlocks_pgp_signed_verified_contact_description
    )

    data object PgpSignedVerificationFailed : EncryptionInfoUiModel(
        icon = R.drawable.ic_proton_lock_open_check_filled,
        color = R.color.padlock_green,
        link = R.string.padlocks_signed_link,
        title = R.string.padlocks_pgp_signed_failed_verification,
        description = R.string.padlocks_pgp_signed_failed_verification_description
    )

}
