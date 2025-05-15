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
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.db.UserDatabase
import me.proton.core.user.data.db.dao.UserDao
import me.proton.core.user.data.entity.UserEntity
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyUserDataSourceImplTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val keyStoreCrypto: KeyStoreCrypto = mockk()
    private val userDao: UserDao = mockk()
    private val userDatabase: UserDatabase = mockk {
        every { userDao() } returns userDao
    }

    private val dataSource = LegacyUserDataSourceImpl(
        db = userDatabase,
        keyStoreCrypto = keyStoreCrypto
    )

    @Test
    fun `getUser maps UserEntity to LegacyUserInfo`() = runTest {
        // Given
        val userId = UserId("user123")
        val decryptedBytes = "decrypted-pass".toByteArray()
        val decryptedPassphrase = mockk<PlainByteArray> {
            every { array } returns decryptedBytes
        }

        val userEntity = mockk<UserEntity> {
            every { this@mockk.userId } returns userId
            every { email } returns "user@example.com"
            every { name } returns "User Name"
            every { displayName } returns "User Display"
            every { passphrase } returns mockk {
                every { decrypt(keyStoreCrypto) } returns decryptedPassphrase
            }
        }

        coEvery { userDao.getByUserId(userId) } returns userEntity

        // When
        val result = dataSource.getUser(userId)

        // Then
        val expected = LegacyUserInfo(
            userId = userId,
            passPhrase = "decrypted-pass",
            email = "user@example.com",
            name = "User Name",
            displayName = "User Display"
        )

        assertEquals(expected, result)
    }
}
