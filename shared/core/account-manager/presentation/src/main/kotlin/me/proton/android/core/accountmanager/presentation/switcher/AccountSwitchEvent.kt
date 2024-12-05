/*
 * Copyright (c) 2024 Proton AG
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.accountmanager.presentation.switcher

import me.proton.android.core.account.domain.model.CoreUserId

sealed interface AccountSwitchEvent {

    @JvmInline
    value class OnAccountSelected(val userId: CoreUserId) : AccountSwitchEvent

    @JvmInline
    value class OnManageAccount(val userId: CoreUserId) : AccountSwitchEvent

    @JvmInline
    value class OnSignOut(val userId: CoreUserId) : AccountSwitchEvent

    @JvmInline
    value class OnSignIn(val userId: CoreUserId) : AccountSwitchEvent

    @JvmInline
    value class OnRemoveAccount(val userId: CoreUserId) : AccountSwitchEvent

    data object OnAddAccount : AccountSwitchEvent

    data object OnManageAccounts : AccountSwitchEvent
}
