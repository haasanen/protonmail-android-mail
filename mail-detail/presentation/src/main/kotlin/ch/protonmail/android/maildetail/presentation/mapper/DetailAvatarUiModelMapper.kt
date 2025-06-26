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

package ch.protonmail.android.maildetail.presentation.mapper

import ch.protonmail.android.mailcommon.domain.model.AvatarInformation
import ch.protonmail.android.mailcommon.presentation.mapper.AvatarInformationMapper
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailmessage.domain.model.Sender
import javax.inject.Inject

class DetailAvatarUiModelMapper @Inject constructor(
    private val avatarInformationMapper: AvatarInformationMapper
) {

    operator fun invoke(
        isDraft: Boolean,
        avatarInformation: AvatarInformation,
        sender: Sender
    ): AvatarUiModel {
        return if (isDraft) {
            AvatarUiModel.DraftIcon
        } else {
            avatarInformationMapper.toUiModel(
                avatarInformation,
                sender.address,
                sender.bimiSelector
            )
        }
    }
}
