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

package ch.protonmail.android.mailcontact.presentation.contactdetails.mapper

import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import ch.protonmail.android.mailcontact.domain.model.ContactDetailAddress
import ch.protonmail.android.mailcontact.domain.model.ContactField
import ch.protonmail.android.mailcontact.domain.model.PartialDate

fun List<ContactField>.hasEmailAddresses() =
    filterIsInstance<ContactField.Emails>().firstOrNull()?.list?.isNotEmpty() ?: false

fun List<ContactField>.hasTelephoneNumbers() =
    filterIsInstance<ContactField.Telephones>().firstOrNull()?.list?.isNotEmpty() ?: false

fun ContactDetailAddress.toFormattedAddress() =
    listOfNotNull(street, postalCode, city, region, country).joinToString(", ")

fun PartialDate.toFormattedPartialDate(): String {
    val monthName = this.month?.let {
        Month.of(it.toInt()).getDisplayName(TextStyle.FULL, Locale.getDefault())
    }
    return when {
        monthName != null && this.day != null && this.year != null -> "$monthName ${this.day}, ${this.year}"
        monthName != null && this.day != null -> "$monthName ${this.day}"
        monthName != null && this.year != null -> "$monthName ${this.year}"
        this.day != null && this.year != null -> "${this.day}, ${this.year}"
        monthName != null -> monthName
        this.day != null -> this.day.toString()
        this.year != null -> this.year.toString()
        else -> ""
    }
}
