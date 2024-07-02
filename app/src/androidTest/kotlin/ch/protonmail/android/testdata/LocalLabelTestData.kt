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

package ch.protonmail.android.testdata

import uniffi.proton_mail_common.LocalLabelWithCount
import uniffi.proton_api_mail.LabelType as RustLabelType

object LocalLabelTestData {
    val localSystemLabelWithCount = LocalLabelWithCount(
        1.toULong(),
        rid = "remoteSystemLabelId",
        parentId = null,
        name = "Inbox",
        "path",
        "color",
        RustLabelType.SYSTEM,
        1.toUInt(),
        false,
        false,
        false,
        totalCount = 2.toULong(),
        unreadCount = 0.toULong()
    )
}
