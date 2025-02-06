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

package me.proton.android.core.accountmanager.presentation.switcher.v1

sealed class AccountListItem(internal open val accountItem: AccountItem) {
    open class Ready(override val accountItem: AccountItem) : AccountListItem(accountItem) {
        data class Primary(override val accountItem: AccountItem) : Ready(accountItem)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Ready
            return accountItem == other.accountItem
        }

        override fun hashCode(): Int = accountItem.hashCode()
    }

    data class Disabled(override val accountItem: AccountItem) : AccountListItem(accountItem)
}
