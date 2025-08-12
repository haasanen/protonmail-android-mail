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

package ch.protonmail.android.mailsession.presentation.mapper

import androidx.compose.ui.graphics.Color
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.presentation.model.AccountInformationUiModel
import javax.inject.Inject

class AccountInformationMapper @Inject constructor(
    private val colorMapper: ColorMapper
) {

    fun toUiModel(account: Account): AccountInformationUiModel {
        val address = account.primaryAddress ?: account.name
        return AccountInformationUiModel(
            userId = account.userId,
            name = account.name,
            email = address,
            avatarUiModel = account.avatarInfo?.let {
                AvatarUiModel.ParticipantAvatar(
                    initial = it.initials,
                    address = address,
                    bimiSelector = null,
                    color = colorMapper.toColor(it.color).getOrElse { Color.Unspecified }
                )
            }
        )
    }
}
