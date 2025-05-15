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

package ch.protonmail.android.legacymigration.domain.model

import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId

data class AccountMigrationInfo(

    /**
     * The user's ID.
     */
    val userId: UserId,

    /**
     * The name of the user.
     */
    val username: String,

    /**
     * The user's primary email address.
     */
    val primaryAddr: String,

    /**
     * The user's display name.
     */
    val displayName: String,

    /**
     * The user's unique session ID.
     */
    val sessionId: SessionId,

    /**
     * The refresh token. This token must be refreshed before use;
     * once refreshed, it becomes an access token.
     */
    val refreshToken: String,

    /**
     *  The user's **unecrypted** key secret.
     */
    val keySecret: String,

    /**
     * The passwords mode.
     */
    val passwordMode: AccountPasswordMode,

    val isPrimaryUser: Boolean = false
)
