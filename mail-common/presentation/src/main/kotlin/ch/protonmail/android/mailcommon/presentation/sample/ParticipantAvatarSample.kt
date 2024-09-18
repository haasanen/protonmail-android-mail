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

package ch.protonmail.android.mailcommon.presentation.sample

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel

object ParticipantAvatarSample {

    val ebay = AvatarUiModel.ParticipantAvatar(
        initial = "E",
        address = "ebay@ebay.com",
        bimiSelector = "BIMI",
        color = Color.Red
    )

    val amazon = AvatarUiModel.ParticipantAvatar(
        initial = "A",
        address = "amazon@amazon.com",
        bimiSelector = "BIMI",
        color = Color.Blue
    )
}
