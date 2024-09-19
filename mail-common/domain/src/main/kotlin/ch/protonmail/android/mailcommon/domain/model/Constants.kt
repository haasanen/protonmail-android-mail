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

package ch.protonmail.android.mailcommon.domain.model

import me.proton.core.domain.entity.UserId

const val TRANSPARENT_COLOR_HEX = "#00000000"

object DeprecatedId {

    @Deprecated(
        """
       This user id exists for the sole purpose of supporting deprecated models from core lib
       (eg. MailSettings). It's not valid and its value should not be used.
    """
    )
    val UserId = UserId("DEPRECATED_USER_ID")
}

