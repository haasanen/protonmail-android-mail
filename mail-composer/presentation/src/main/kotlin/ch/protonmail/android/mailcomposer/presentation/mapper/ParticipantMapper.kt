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

package ch.protonmail.android.mailcomposer.presentation.mapper

import ch.protonmail.android.mailcomposer.presentation.model.RecipientUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailmessage.domain.model.Participant
import me.proton.core.util.kotlin.equalsNoCase
import me.proton.core.util.kotlin.takeIfNotBlank
import javax.inject.Inject

@Deprecated("To be dropped once contact suggestions through rust are in place")
class ParticipantMapper @Inject constructor() {

    fun recipientUiModelToParticipant(
        recipient: RecipientUiModel.Valid,
        contacts: List<ContactMetadata.Contact>
    ): Participant {
        val contact = contacts.firstOrNull { contact ->
            contact.emails.any {
                recipient.address.equalsNoCase(it.email)
            }
        }

        val contactEmail = contact?.emails?.find {
            recipient.address.equalsNoCase(it.email)
        }

        return Participant(
            recipient.address,
            contact?.name?.takeIfNotBlank() ?: recipient.address,
            contactEmail?.isProton ?: false,
            null
        )
    }
}
