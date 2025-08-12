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

package ch.protonmail.android.mailsession.domain.model

import me.proton.core.domain.entity.UserId

data class Account(
    val userId: UserId,
    val name: String,
    val state: AccountState,
    val primaryAddress: String,
    val avatarInfo: AccountAvatarInfo? = null
)

enum class AccountState {
    /**
     * The account is not yet ready to be used.
     */
    NotReady,

    /**
     * The account has at least one fully logged-in session.
     */
    Ready,

    /**
     * The account has authenticated sessions but they are missing the key secret.
     */
    TwoPasswordNeeded,

    /**
     * The account has partially authenticated sessions that require a second factor.
     */
    TwoFactorNeeded,

    /**
     * The account has authenticated but they should set a new password (it is probably a private sub-account).
     */
    NewPassNeeded,

    /**
     * The account has no active sessions.
     */
    Disabled,

    /**
     * The account requires a new password.
     */
    NewPassNeeded
}
