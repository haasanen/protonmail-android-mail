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

package ch.protonmail.android.mailmailbox.presentation.mailbox.usecase

import ch.protonmail.android.mailmailbox.domain.repository.SenderAddressRepository
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class ObserveValidSenderAddress @Inject constructor(val senderAddressRepository: SenderAddressRepository) {

    /**
     * Will gracefully fail, that is is there is an error we will assume that we have a valid sender address,
     * this will fail in the composer so the observation exists only for UI purposes.  Since sender address being
     * invalid is a bit of an edge case it's better to gracefully fail here and assume true and let the composer
     * catch the outliers
     */
    suspend operator fun invoke(userId: UserId) =
        senderAddressRepository.observeUserHasValidSenderAddress(userId = userId)
            .map {
                it.fold({
                    true
                }, {
                    it
                })
            }
}
