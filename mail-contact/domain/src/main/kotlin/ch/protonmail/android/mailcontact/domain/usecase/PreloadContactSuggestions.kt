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

package ch.protonmail.android.mailcontact.domain.usecase

import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.mailcontact.domain.repository.DeviceContactsRepository
import javax.inject.Inject
import arrow.core.Either
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcontact.domain.model.DeviceContactsWithSignature
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import timber.log.Timber

class PreloadContactSuggestions @Inject constructor(
    private val contactsRepository: ContactRepository,
    private val deviceContactsRepository: DeviceContactsRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(userId: UserId): Either<DataError, Unit> = withContext(ioDispatcher) {
        val deviceContacts = deviceContactsRepository
            .getAllContacts(useCacheIfAvailable = false)
            .onLeft { Timber.w("contact-suggestions: Failed to get device contacts: $it") }
            .getOrElse { DeviceContactsWithSignature.Empty }

        contactsRepository.preloadContactSuggestions(userId, deviceContacts)
    }
}
