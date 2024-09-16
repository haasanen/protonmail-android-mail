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

package ch.protonmail.android.mailpagination.domain.model

import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailpagination.domain.model.ReadStatus.All
import ch.protonmail.android.mailpagination.domain.model.ReadStatus.Read
import ch.protonmail.android.mailpagination.domain.model.ReadStatus.Unread

/**
 * Page Parameters needed to query/fetch/filter/sort/order a page.
 */
data class PageKey(
    val labelId: LabelId = LabelId("0"),
    val read: ReadStatus = All,
    val pageNumber: PageNumber = PageNumber.First
)

/**
 * Filter only [Read], [Unread] or [All] items.
 *
 * @see [PageItem.read]
 */
enum class ReadStatus {
    All,
    Read,
    Unread
}

enum class PageNumber {
    First,
    Next,
    All
}
