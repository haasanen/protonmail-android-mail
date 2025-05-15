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

import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.db.AddressDatabase
import me.proton.core.user.data.db.dao.AddressDao
import me.proton.core.user.data.entity.AddressEntity
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ch.protonmail.android.legacymigration.domain.model.LegacyUserAddressInfo
import me.proton.core.user.domain.entity.AddressId

class LegacyUserAddressDataSourceImplTest {

    @get:Rule
    val coroutineRule = MainDispatcherRule()

    private val addressDao: AddressDao = mockk()
    private val addressDatabase: AddressDatabase = mockk {
        every { addressDao() } returns addressDao
    }

    private val dataSource = LegacyUserAddressDataSourceImpl(addressDatabase)

    @Test
    fun `getPrimaryUserAddress returns null when list is empty`() = runTest {
        // Given
        val userId = UserId("user123")
        coEvery { addressDao.getByUserId(userId) } returns emptyList()

        // When
        val result = dataSource.getPrimaryUserAddress(userId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getPrimaryUserAddress filters and maps lowest ordered enabled address`() = runTest {
        // Given
        val userId = UserId("user123")
        val addr1 = mockk<AddressEntity> {
            every { this@mockk.userId } returns userId
            every { this@mockk.addressId } returns AddressId("ad1")
            every { this@mockk.displayName } returns "Address 1"
            every { this@mockk.email } returns "a1@example.com"
            every { this@mockk.order } returns 2
            every { this@mockk.enabled } returns true
        }
        val addr2 = mockk<AddressEntity> {
            every { this@mockk.userId } returns userId
            every { this@mockk.addressId } returns AddressId("ad2")
            every { this@mockk.displayName } returns "Address 2"
            every { this@mockk.email } returns "a2@example.com"
            every { this@mockk.order } returns 1
            every { this@mockk.enabled } returns true
        }
        val addr3 = mockk<AddressEntity> {
            every { this@mockk.userId } returns userId
            every { this@mockk.addressId } returns AddressId("ad3")
            every { this@mockk.displayName } returns "Address 3"
            every { this@mockk.email } returns "a3@example.com"
            every { this@mockk.order } returns 3
            every { this@mockk.enabled } returns false
        }

        coEvery { addressDao.getByUserId(userId) } returns listOf(addr1, addr2, addr3)

        // When
        val result = dataSource.getPrimaryUserAddress(userId)

        // Then
        val expected = LegacyUserAddressInfo(
            addressId = addr2.addressId,
            email = addr2.email,
            order = addr2.order,
            userId = addr2.userId,
            displayName = addr2.displayName
        )
        assertEquals(expected, result)
    }
}
