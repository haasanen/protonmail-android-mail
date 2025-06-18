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

package ch.protonmail.android.mailcontact.presentation.contactdetails.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel

data class ContactDetailsUiModel(
    val avatarUiModel: AvatarUiModel,
    val headerUiModel: HeaderUiModel,
    val quickActionUiModels: List<QuickActionUiModel>,
    val contactDetailsItemGroupUiModels: List<ContactDetailsItemGroupUiModel>
)

sealed interface AvatarUiModel {
    data class Initials(val value: String, val color: Color) : AvatarUiModel
    data class Photo(val bitmap: ImageBitmap) : AvatarUiModel
}

data class HeaderUiModel(
    val displayName: String,
    val displayEmailAddress: String?
)

data class QuickActionUiModel(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
    val isEnabled: Boolean
)

data class ContactDetailsItemGroupUiModel(
    val contactDetailsItemUiModels: List<ContactDetailsItemUiModel>
)

data class ContactDetailsItemUiModel(
    val contactDetailsItemType: ContactDetailsItemType,
    val label: TextUiModel,
    val value: TextUiModel
)

enum class ContactDetailsItemType { Email, Phone, Other }
