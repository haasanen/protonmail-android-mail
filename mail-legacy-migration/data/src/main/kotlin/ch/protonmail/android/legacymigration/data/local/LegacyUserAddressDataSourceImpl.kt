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

package ch.protonmail.android.legacymigration.data.local

import ch.protonmail.android.legacymigration.domain.model.LegacyUserAddressInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.entity.AddressEntity
import javax.inject.Inject

class LegacyUserAddressDataSourceImpl @Inject constructor(
    private val db: AddressDatabase
) : LegacyUserAddressDataSource {
    private val addressDao = db.addressDao()

    override suspend fun getPrimaryUserAddress(userId: UserId): LegacyUserAddressInfo? {
        val addressList = addressDao.getByUserId(userId)
        if (addressList.isEmpty()) {
            return null
        }

        return addressList
            .filter { it.enabled }
            .minBy { it.order }
            .toLegacyUserAddressInfo()
    }

    private fun AddressEntity.toLegacyUserAddressInfo(): LegacyUserAddressInfo {
        return LegacyUserAddressInfo(
            addressId = addressId,
            email = email,
            order = order,
            userId = userId,
            displayName = displayName
        )
    }
}
