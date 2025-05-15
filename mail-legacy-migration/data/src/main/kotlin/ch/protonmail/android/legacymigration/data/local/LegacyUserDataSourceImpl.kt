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

import ch.protonmail.android.legacymigration.domain.model.LegacyUserInfo
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.entity.UserEntity
import javax.inject.Inject

class LegacyUserDataSourceImpl @Inject constructor(
    db: UserDatabase,
    private val keyStoreCrypto: KeyStoreCrypto
) : LegacyUserDataSource {

    private val userDao = db.userDao()

    override suspend fun getUser(userId: UserId): LegacyUserInfo? = userDao.getByUserId(userId)?.toLegacyUserInfo()

    private fun UserEntity.toLegacyUserInfo() = LegacyUserInfo(
        userId = userId,
        passPhrase = passphrase?.decrypt(keyStoreCrypto)?.let { String(it.array) } ?: "",
        email = email,
        name = name,
        displayName = displayName
    )
}
