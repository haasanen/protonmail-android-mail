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

import ch.protonmail.android.legacymigration.data.local.rawSql.LegacyDbReader
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ch.protonmail.android.legacymigration.domain.model.LegacyUserAddressInfo
import kotlinx.coroutines.CoroutineScope
import me.proton.core.user.domain.entity.AddressId

class LegacyUserAddressDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val dbReader: LegacyDbReader = mockk()

    private val dataSource = LegacyUserAddressDataSourceImpl(
        dbReader = dbReader,
        dbCoroutineScope = testCoroutineScope
    )

    private val userId = UserId("user123")

    @Test
    fun `getPrimaryUserAddress returns null when dbReader returns null`() = runTest {
        // Given
        coEvery { dbReader.readPrimaryAddress(userId) } returns null

        // When
        val result = dataSource.getPrimaryUserAddress(userId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getPrimaryUserAddress returns mapped LegacyUserAddressInfo`() = runTest {
        // Given
        val expected = LegacyUserAddressInfo(
            userId = userId,
            addressId = AddressId("addr123"),
            email = "a@example.com",
            displayName = "My Address"
        )
        coEvery { dbReader.readPrimaryAddress(userId) } returns expected

        // When
        val result = dataSource.getPrimaryUserAddress(userId)

        // Then
        assertEquals(expected, result)
    }
}

