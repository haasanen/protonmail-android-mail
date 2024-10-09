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

package ch.protonmail.android.mailcontact.domain.model

import me.proton.core.domain.entity.UserId

data class ContactEmail(
    val userId: UserId,
    val id: ContactEmailId,
    val name: String,
    val email: String,
    /**
     * 0 if contact contains custom sending preferences or keys, 1 otherwise
     */
    val defaults: Int,
    val order: Int,
    val contactId: ContactId,
    val canonicalEmail: String?,
    val labelIds: List<String>,
    val isProton: Boolean?,
    val lastUsedTime: Long
)
