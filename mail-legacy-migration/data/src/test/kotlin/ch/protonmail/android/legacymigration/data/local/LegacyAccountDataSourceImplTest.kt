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

import io.mockk.mockk
import org.junit.Rule
import ch.protonmail.android.legacymigration.data.local.rawSql.LegacyDbReader
import ch.protonmail.android.legacymigration.domain.model.LegacySessionInfo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import kotlinx.coroutines.CoroutineScope
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId

class LegacyAccountDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val testCoroutineScope = CoroutineScope(mainDispatcherRule.testDispatcher)

    private val dbReader: LegacyDbReader = mockk()

    private val dataSource = LegacyAccountDataSourceImpl(
        dbReader = dbReader,
        dbCoroutineScope = testCoroutineScope
    )

    private val userId1 = UserId("user1")
    private val userId2 = UserId("user2")
    private val sessionId1 = SessionId("session1")
    private val sessionId2 = SessionId("session2")

    @Test
    fun `getPrimaryUserId returns primary userId`() = runTest {
        // Given
        coEvery { dbReader.readLatestPrimaryUserId() } returns userId1

        // When
        val result = dataSource.getPrimaryUserId()

        // Then
        assertEquals(userId1, result)
    }

    @Test
    fun `getSession returns legacy session`() = runTest {
        // Given
        val sessionInfo = LegacySessionInfo(
            userId = userId1,
            sessionId = sessionId1,
            refreshToken = "refresh-token",
            twoPassModeEnabled = true
        )
        coEvery { dbReader.readLegacySessionInfo(sessionId1) } returns sessionInfo

        // When
        val result = dataSource.getSession(sessionId1)

        // Then
        assertEquals(sessionInfo, result)
    }

    @Test
    fun `getSessions returns list of legacy sessions`() = runTest {
        // Given
        val sessionList = listOf(
            LegacySessionInfo(userId1, sessionId1, "r1", true),
            LegacySessionInfo(userId2, sessionId2, "r2", false)
        )
        coEvery { dbReader.readAuthenticatedSessions() } returns sessionList

        // When
        val result = dataSource.getSessions()

        // Then
        assertEquals(sessionList, result)
    }
}

