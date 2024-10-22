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

package ch.protonmail.android.mailcontact.presentation.model

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.presentation.R
import me.proton.core.util.kotlin.takeIfNotBlank
import javax.inject.Inject

class ContactEmailListMapper @Inject constructor() {

    fun toEmailUiModel(contactEmails: List<ContactEmail>): TextUiModel {
        return if (contactEmails.isNotEmpty()) {
            val emails = contactEmails.mapNotNull { contactEmail ->
                contactEmail.email.takeIfNotBlank()
            }

            if (emails.isEmpty()) {
                TextUiModel(R.string.no_contact_email)
            } else if (emails.size > 1) {
                TextUiModel(
                    R.string.multiple_contact_emails,
                    emails.first(),
                    emails.size.minus(1)
                )
            } else {
                TextUiModel(emails.first())
            }
        } else {
            TextUiModel(R.string.no_contact_email)
        }
    }
}
