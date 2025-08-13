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

package me.proton.android.core.account.data.model

import me.proton.android.core.account.domain.model.CoreAccount
import me.proton.android.core.account.domain.model.CoreAccountState
import me.proton.android.core.account.domain.model.CoreUserId
import uniffi.proton_mail_uniffi.StoredAccount
import uniffi.proton_mail_uniffi.StoredAccountState

internal fun StoredAccount.toCoreAccount() = CoreAccount(
    userId = CoreUserId(userId()),
    displayName = details().name,
    nameOrAddress = details().name,
    primaryEmailAddress = details().email,
    state = state().toCoreAccountState(),
    username = details().name
)

internal fun StoredAccountState.toCoreAccountState() = when (this) {
    is StoredAccountState.LoggedIn -> CoreAccountState.Ready
    is StoredAccountState.LoggedOut -> CoreAccountState.Disabled
    is StoredAccountState.NeedMbp -> CoreAccountState.TwoPasswordNeeded
    is StoredAccountState.NeedTfa -> CoreAccountState.TwoFactorNeeded
    is StoredAccountState.NotReady -> CoreAccountState.NotReady
    is StoredAccountState.NeedNewPass -> CoreAccountState.NewPassNeeded
}
