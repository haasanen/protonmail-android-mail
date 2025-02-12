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

package ch.protonmail.android.testdata.contact

import ch.protonmail.android.mailcommon.domain.sample.AvatarInformationSample
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.testdata.contact.ContactTestData.contactGroupSuggestionEmail1
import ch.protonmail.android.testdata.contact.ContactTestData.contactGroupSuggestionEmail2

object ContactUiModelTestData {

    val contactSuggestion1 = ContactSuggestionUiModel.Contact(
        name = "contact being suggested",
        initial = AvatarInformationSample.contactSuggestion.initials,
        email = ContactEmailSample.contactSuggestionEmail.email
    )

    val contactGroupSuggestion = ContactSuggestionUiModel.ContactGroup(
        name = "contact group here",
        color = AvatarInformationSample.contactSuggestion.color,
        emails = listOf(
            contactGroupSuggestionEmail1.emails[0].email,
            contactGroupSuggestionEmail2.emails[0].email
        )
    )

}

