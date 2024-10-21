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

package ch.protonmail.android.mailcontact.data.mapper

import ch.protonmail.android.mailcommon.datarust.mapper.LocalContactEmail
import ch.protonmail.android.mailcommon.datarust.mapper.LocalContactGroupId
import ch.protonmail.android.mailcommon.datarust.mapper.LocalContactId
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId
import ch.protonmail.android.mailcontact.domain.model.ContactGroupId
import ch.protonmail.android.mailcontact.domain.model.ContactId
import me.proton.core.domain.entity.UserId

fun LocalContactId.toContactId(): ContactId = ContactId(this.value.toString())
fun LocalContactGroupId.toContactGroupId(): ContactGroupId = ContactGroupId(this.value.toString())
fun LocalContactEmail.toContactEmail(contactId: ContactId): ContactEmail {
    return ContactEmail(
        userId = UserId("userId"),
        id = ContactEmailId(this.id.value.toString()),
        name = "",
        email = this.email,
        defaults = 0,
        order=0,
        contactId = contactId,
        canonicalEmail = "",
        labelIds = emptyList(),
        isProton = true,
        lastUsedTime = 0
    )
}
