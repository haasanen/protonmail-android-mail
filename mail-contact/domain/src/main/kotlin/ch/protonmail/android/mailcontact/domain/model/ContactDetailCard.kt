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

data class ContactDetailCard(
    val id: ContactId,
    val fields: List<ContactField>
)

sealed interface ContactField {

    data class Anniversary(val date: ContactDate) : ContactField
    data class Birthday(val date: ContactDate) : ContactField
    data class Gender(val type: GenderKind) : ContactField
    data class Addresses(val list: List<ContactDetailAddress>) : ContactField
    data class Emails(val list: List<ContactDetailEmail>) : ContactField
    data class Languages(val list: List<String>) : ContactField
    data class Logos(val list: List<String>) : ContactField
    data class Members(val list: List<String>) : ContactField
    data class Notes(val list: List<String>) : ContactField
    data class Organizations(val list: List<String>) : ContactField
    data class Telephones(val list: List<ContactDetailTelephone>) : ContactField
    data class Photos(val list: List<String>) : ContactField
    data class Roles(val list: List<String>) : ContactField
    data class TimeZones(val list: List<String>) : ContactField
    data class Titles(val list: List<String>) : ContactField
    data class Urls(val list: List<VCardUrl>) : ContactField
}

sealed interface ContactDate {

    data class Text(val text: String) : ContactDate
    data class Date(val partialDate: PartialDate) : ContactDate
}

data class PartialDate(val year: Short?, val month: Byte?, val day: Byte?)

sealed interface GenderKind {

    data object Male : GenderKind
    data object Female : GenderKind
    data object Other : GenderKind
    data object NotApplicable : GenderKind
    data object Unknown : GenderKind
    data object None : GenderKind
    data class Custom(val value: String) : GenderKind
}

data class ContactDetailAddress(
    val street: String?,
    val city: String?,
    val region: String?,
    val postalCode: String?,
    val country: String?,
    val addressTypes: List<VCardPropType>
)

data class ContactDetailEmail(
    val email: String,
    val emailType: List<VCardPropType>
)

data class ContactDetailTelephone(
    val number: String,
    val telephoneTypes: List<VCardPropType>
)

data class VCardUrl(
    val url: String,
    val urlTypes: List<VCardPropType>
)

sealed interface VCardPropType {

    data object Home : VCardPropType
    data object Work : VCardPropType
    data object Text : VCardPropType
    data object Voice : VCardPropType
    data object Fax : VCardPropType
    data object Cell : VCardPropType
    data object Video : VCardPropType
    data object Pager : VCardPropType
    data object TextPhone : VCardPropType
    data class Custom(val value: String) : VCardPropType
}
