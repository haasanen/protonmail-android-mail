/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.composer.data.mapper

import ch.protonmail.android.composer.data.local.LocalSenderAddresses
import ch.protonmail.android.mailcommon.data.mapper.LocalComposerRecipient
import ch.protonmail.android.mailcomposer.domain.model.SenderAddresses
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailmessage.domain.model.Recipient
import uniffi.proton_mail_uniffi.ComposerRecipient
import uniffi.proton_mail_uniffi.DraftSenderAddressList
import uniffi.proton_mail_uniffi.SingleRecipientEntry

fun DraftSenderAddressList.toLocalSenderAddresses() = LocalSenderAddresses(this.available, this.active)
fun LocalSenderAddresses.toSenderAddresses() = SenderAddresses(
    this.addresses.map { SenderEmail(it) },
    SenderEmail(this.selected)
)

fun List<LocalComposerRecipient>.toSingleRecipients(): List<Recipient> = this
    .filterIsInstance<ComposerRecipient.Single>()
    .map {
        val localRecipient = it.v1
        Recipient(
            address = localRecipient.address,
            name = localRecipient.displayName ?: localRecipient.address,
            isProton = false
        )
    }

fun List<LocalComposerRecipient>.toComposerRecipients(): List<String> = this.map { localRecipient ->
    when (localRecipient) {
        is ComposerRecipient.Group -> localRecipient.v1.displayName
        is ComposerRecipient.Single -> localRecipient.v1.address
    }
}

fun Recipient.toSingleRecipientEntry() = SingleRecipientEntry(
    this.name,
    this.address
)
