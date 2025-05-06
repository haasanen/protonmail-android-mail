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

package ch.protonmail.android.mailmessage.presentation.model.attachment

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import ch.protonmail.android.mailmessage.domain.model.AttachmentState

@Immutable
data class AttachmentMetadataUiModel(
    val id: AttachmentIdUiModel,
    val name: String,
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int,
    val size: Long,
    val status: AttachmentState? = null,
    val deletable: Boolean = false,
    val isCalendar: Boolean = false
)
