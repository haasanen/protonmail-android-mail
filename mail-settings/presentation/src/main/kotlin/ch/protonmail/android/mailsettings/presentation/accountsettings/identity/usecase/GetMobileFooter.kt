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

package ch.protonmail.android.mailsettings.presentation.accountsettings.identity.usecase

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsettings.domain.model.MobileFooter
import ch.protonmail.android.mailsettings.domain.repository.MobileFooterRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@MissingRustApi(
    """
    Implementation kept for migration purposes. 
    To re-implement, we need to wire it properly with the corresponding Rust SDK API.
"""
)
class GetMobileFooter @Inject constructor(
    private val mobileFooterRepository: MobileFooterRepository
) {

    suspend operator fun invoke(userId: UserId): Either<DataError, MobileFooter> =
        mobileFooterRepository.getMobileFooter(userId)
}
