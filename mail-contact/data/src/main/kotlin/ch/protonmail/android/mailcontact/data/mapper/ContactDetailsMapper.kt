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

import ch.protonmail.android.mailcommon.data.mapper.LocalContactDate
import ch.protonmail.android.mailcommon.data.mapper.LocalContactDateDate
import ch.protonmail.android.mailcommon.data.mapper.LocalContactDateString
import ch.protonmail.android.mailcommon.data.mapper.LocalContactDetailAddress
import ch.protonmail.android.mailcommon.data.mapper.LocalContactDetailCard
import ch.protonmail.android.mailcommon.data.mapper.LocalContactDetailsEmail
import ch.protonmail.android.mailcommon.data.mapper.LocalContactDetailsTelephones
import ch.protonmail.android.mailcommon.data.mapper.LocalContactField
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldAddresses
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldAnniversary
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldBirthday
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldEmails
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldGender
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldLanguages
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldLogos
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldMembers
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldNotes
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldOrganizations
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldPhotos
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldRoles
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldTelephones
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldTimeZones
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldTitles
import ch.protonmail.android.mailcommon.data.mapper.LocalContactFieldUrls
import ch.protonmail.android.mailcommon.data.mapper.LocalExtendedName
import ch.protonmail.android.mailcommon.data.mapper.LocalGenderKind
import ch.protonmail.android.mailcommon.data.mapper.LocalGenderKindFemale
import ch.protonmail.android.mailcommon.data.mapper.LocalGenderKindMale
import ch.protonmail.android.mailcommon.data.mapper.LocalGenderKindNone
import ch.protonmail.android.mailcommon.data.mapper.LocalGenderKindNotApplicable
import ch.protonmail.android.mailcommon.data.mapper.LocalGenderKindOther
import ch.protonmail.android.mailcommon.data.mapper.LocalGenderKindString
import ch.protonmail.android.mailcommon.data.mapper.LocalGenderKindUnknown
import ch.protonmail.android.mailcommon.data.mapper.LocalPartialDate
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropType
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypeCell
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypeFax
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypeHome
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypePager
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypeString
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypeText
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypeTextPhone
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypeVideo
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypeVoice
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardPropTypeWork
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardUrl
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardUrlValue
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardUrlValueHttp
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardUrlValueNotHttp
import ch.protonmail.android.mailcommon.data.mapper.LocalVCardUrlValueText
import ch.protonmail.android.mailcontact.domain.model.ContactDate
import ch.protonmail.android.mailcontact.domain.model.ContactDetailAddress
import ch.protonmail.android.mailcontact.domain.model.ContactDetailCard
import ch.protonmail.android.mailcontact.domain.model.ContactDetailEmail
import ch.protonmail.android.mailcontact.domain.model.ContactDetailTelephone
import ch.protonmail.android.mailcontact.domain.model.ContactField
import ch.protonmail.android.mailcontact.domain.model.ExtendedName
import ch.protonmail.android.mailcontact.domain.model.GenderKind
import ch.protonmail.android.mailcontact.domain.model.PartialDate
import ch.protonmail.android.mailcontact.domain.model.VCardPropType
import ch.protonmail.android.mailcontact.domain.model.VCardUrl
import ch.protonmail.android.mailcontact.domain.model.VCardUrlValue

fun LocalContactDetailCard.toContactDetailCard() = ContactDetailCard(
    id = id.toContactId(),
    remoteId = remoteId,
    avatarInformation = avatarInformation.toAvatarInformation(),
    extendedName = extendedName.toExtendedName(),
    fields = fields.map { it.toContactField() }
)

private fun LocalExtendedName.toExtendedName() = ExtendedName(
    last = last,
    first = first
)

private fun LocalContactField.toContactField() = when (this) {
    is LocalContactFieldAddresses -> ContactField.Addresses(list = this.v1.map { it.toContactDetailAddress() })
    is LocalContactFieldAnniversary -> ContactField.Anniversary(date = this.v1.toContactDate())
    is LocalContactFieldBirthday -> ContactField.Birthday(date = this.v1.toContactDate())
    is LocalContactFieldEmails -> ContactField.Emails(list = this.v1.map { it.toContactDetailEmail() })
    is LocalContactFieldGender -> ContactField.Gender(type = this.v1.toGenderKind())
    is LocalContactFieldLanguages -> ContactField.Languages(list = this.v1)
    is LocalContactFieldLogos -> ContactField.Logos(list = this.v1)
    is LocalContactFieldMembers -> ContactField.Members(list = this.v1)
    is LocalContactFieldNotes -> ContactField.Notes(list = this.v1)
    is LocalContactFieldOrganizations -> ContactField.Organizations(list = this.v1)
    is LocalContactFieldPhotos -> ContactField.Photos(list = this.v1)
    is LocalContactFieldRoles -> ContactField.Roles(list = this.v1)
    is LocalContactFieldTelephones -> ContactField.Telephones(list = this.v1.map { it.toContactDetailTelephone() })
    is LocalContactFieldTimeZones -> ContactField.TimeZones(list = this.v1)
    is LocalContactFieldTitles -> ContactField.Titles(list = this.v1)
    is LocalContactFieldUrls -> ContactField.Urls(list = this.v1.map { it.toVCardUrl() })
}

private fun LocalContactDate.toContactDate() = when (this) {
    is LocalContactDateDate -> ContactDate.Date(partialDate = this.v1.toPartialDate())
    is LocalContactDateString -> ContactDate.Text(text = this.v1)
}

private fun LocalPartialDate.toPartialDate() = PartialDate(
    year = this.year?.toShort(),
    month = this.month?.toByte(),
    day = this.day?.toByte()
)

private fun LocalContactDetailsEmail.toContactDetailEmail() = ContactDetailEmail(
    email = this.email,
    emailType = this.emailType.map { it.toVCardPropType() }
)

private fun LocalContactDetailAddress.toContactDetailAddress() = ContactDetailAddress(
    street = this.street,
    city = this.city,
    region = this.region,
    postalCode = this.postalCode,
    country = this.country,
    addressTypes = this.addrType.map { it.toVCardPropType() }
)

private fun LocalContactDetailsTelephones.toContactDetailTelephone() = ContactDetailTelephone(
    number = this.number,
    telephoneTypes = this.telTypes.map { it.toVCardPropType() }
)

private fun LocalVCardUrl.toVCardUrl() = VCardUrl(
    url = this.url.toVCardUrlValue(),
    urlTypes = this.urlType.map { it.toVCardPropType() }
)

private fun LocalVCardUrlValue.toVCardUrlValue() = when (this) {
    is LocalVCardUrlValueHttp -> VCardUrlValue.Http(this.v1)
    is LocalVCardUrlValueNotHttp -> VCardUrlValue.NotHttp(this.v1)
    is LocalVCardUrlValueText -> VCardUrlValue.Text(this.v1)
}

private fun LocalVCardPropType.toVCardPropType() = when (this) {
    is LocalVCardPropTypeCell -> VCardPropType.Cell
    is LocalVCardPropTypeFax -> VCardPropType.Fax
    is LocalVCardPropTypeHome -> VCardPropType.Home
    is LocalVCardPropTypePager -> VCardPropType.Pager
    is LocalVCardPropTypeString -> VCardPropType.Custom(this.v1)
    is LocalVCardPropTypeText -> VCardPropType.Text
    is LocalVCardPropTypeTextPhone -> VCardPropType.TextPhone
    is LocalVCardPropTypeVideo -> VCardPropType.Video
    is LocalVCardPropTypeVoice -> VCardPropType.Voice
    is LocalVCardPropTypeWork -> VCardPropType.Work
}

private fun LocalGenderKind.toGenderKind() = when (this) {
    is LocalGenderKindFemale -> GenderKind.Female
    is LocalGenderKindMale -> GenderKind.Male
    is LocalGenderKindNone -> GenderKind.None
    is LocalGenderKindNotApplicable -> GenderKind.NotApplicable
    is LocalGenderKindOther -> GenderKind.Other
    is LocalGenderKindString -> GenderKind.Custom(value = this.v1)
    is LocalGenderKindUnknown -> GenderKind.Unknown
}
