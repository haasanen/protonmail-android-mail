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
import ch.protonmail.android.legacymigration.domain.model.LegacyUserInfo
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyUserDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val dbReader: LegacyDbReader = mockk()

    private val dataSource = LegacyUserDataSourceImpl(
        dbReader = dbReader,
        dbCoroutineScope = testCoroutineScope
    )

    private val userId = UserId("user123")

    @Test
    fun `getUser returns LegacyUserInfo when present`() = runTest {
        // Given
        val userInfo = LegacyUserInfo(
            userId = userId,
            passPhrase = "decrypted-pass",
            email = "user@example.com",
            name = "User Name",
            displayName = "User Display"
        )
        coEvery { dbReader.readLegacyUserInfo(userId) } returns userInfo

        // When
        val result = dataSource.getUser(userId)

        // Then
        assertEquals(userInfo, result)
    }

    @Test
    fun `getUser returns null when not found`() = runTest {
        // Given
        coEvery { dbReader.readLegacyUserInfo(userId) } returns null

        // When
        val result = dataSource.getUser(userId)

        // Then
        assertNull(result)
    }
}
