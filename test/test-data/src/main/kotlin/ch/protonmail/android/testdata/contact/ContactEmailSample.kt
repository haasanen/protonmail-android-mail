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

import ch.protonmail.android.mailcontact.domain.model.ContactEmail
import ch.protonmail.android.mailcontact.domain.model.ContactEmailId

object ContactEmailSample {

    val contactEmail1 = ContactEmail(
        ContactIdTestData.contactEmailId1,
        "email (contact email 1)",
        false,
        lastUsedTime = 0
    )

    val contactEmail2 = ContactEmail(
        ContactIdTestData.contactEmailId2,
        "email (contact email 2)",
        false,
        lastUsedTime = 0
    )

    val contactEmail3 = ContactEmail(
        ContactIdTestData.contactEmailId3,
        "email (contact email 3)",
        false,
        lastUsedTime = 0
    )

    val contactEmail4 = ContactEmail(
        ContactIdTestData.contactEmailId4,
        "email (contact email 4)",
        false,
        lastUsedTime = 0
    )

    val contactEmailLastUsedRecently = ContactEmail(
        ContactIdTestData.contactEmailId5,
        "testing@last.used.time -- recently",
        false,
        lastUsedTime = 100
    )

    val contactEmailLastUsedLongTimeAgo = ContactEmail(
        ContactIdTestData.contactEmailId6,
        "testing@last.used.time -- long time ago",
        false,
        lastUsedTime = 1
    )

    val contactSuggestionEmail = ContactEmail(
        ContactIdTestData.contactSuggestionEmailId,
        "contact suggestion email",
        false,
        lastUsedTime = 0
    )

    val contactGroupSuggestionEmail1 = ContactEmail(
        ContactEmailId("contactgroup@first.email"),
        "contactgroup@first.email",
        false,
        lastUsedTime = 0
    )
    val contactGroupSuggestionEmail2 = ContactEmail(
        ContactEmailId("contactgroup@second.email"),
        "contactgroup@second.email",
        false,
        lastUsedTime = 0
    )
}
