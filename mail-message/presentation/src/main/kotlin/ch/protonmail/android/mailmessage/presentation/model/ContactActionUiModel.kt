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

package ch.protonmail.android.mailmessage.presentation.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.mailmessage.presentation.R

sealed class ContactActionUiModel(
    @DrawableRes val iconRes: Int,
    @StringRes val textRes: Int,
    @StringRes val descriptionRes: Int
) {

    data class CopyAddress(val address: String) : ContactActionUiModel(
        iconRes = R.drawable.ic_proton_squares,
        textRes = R.string.contact_actions_copy_address,
        descriptionRes = R.string.contact_actions_copy_address_description
    )

    data class CopyName(val name: String) : ContactActionUiModel(
        iconRes = R.drawable.ic_proton_squares,
        textRes = R.string.contact_actions_copy_name,
        descriptionRes = R.string.contact_actions_copy_name_description
    )

    data class NewMessage(val participant: Participant) : ContactActionUiModel(
        iconRes = R.drawable.ic_proton_pen_square,
        textRes = R.string.contact_actions_new_message,
        descriptionRes = R.string.contact_actions_new_message_description
    )

    data class AddContactUiModel(val participant: Participant) : ContactActionUiModel(
        iconRes = R.drawable.ic_proton_user_plus,
        textRes = R.string.contact_actions_add_contact,
        descriptionRes = R.string.contact_actions_add_contact_description
    )

    data class BlockContact(val participant: Participant) : ContactActionUiModel(
        iconRes = R.drawable.ic_proton_circle_slash,
        textRes = R.string.contact_actions_block_contact,
        descriptionRes = R.string.contact_actions_block_contact_description
    )

    data class UnblockContact(val participant: Participant) : ContactActionUiModel(
        iconRes = R.drawable.ic_proton_circle_slash,
        textRes = R.string.contact_actions_unblock_contact,
        descriptionRes = R.string.contact_actions_unblock_contact_description
    )


    data class BlockAddress(val participant: Participant) : ContactActionUiModel(
        iconRes = R.drawable.ic_proton_circle_slash,
        textRes = R.string.contact_actions_block_address,
        descriptionRes = R.string.contact_actions_block_address_description
    )

    data class UnblockAddress(val participant: Participant) : ContactActionUiModel(
        iconRes = R.drawable.ic_proton_circle_slash,
        textRes = R.string.contact_actions_unblock_address,
        descriptionRes = R.string.contact_actions_unblock_address_description
    )
}
