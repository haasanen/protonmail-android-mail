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

package ch.protonmail.android.legacymigration.data.mapper

import ch.protonmail.android.legacymigration.domain.model.AccountPasswordMode
import ch.protonmail.android.legacymigration.domain.model.LegacySessionInfo
import ch.protonmail.android.legacymigration.domain.model.LegacyUserAddressInfo
import ch.protonmail.android.legacymigration.domain.model.LegacyUserInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.domain.entity.AddressId
import kotlin.test.Test
import kotlin.test.assertEquals

class MigrationInfoMapperTest {

    private val mapper = MigrationInfoMapper()

    @Test
    fun `maps necessary migration data to AccountMigrationInfo`() {
        // Given
        val userId = UserId("user-123")
        val sessionId = SessionId("session-abc")
        val sessionInfo = LegacySessionInfo(
            userId = userId,
            sessionId = sessionId,
            refreshToken = "refresh-token",
            twoPassModeEnabled = true
        )

        val user = LegacyUserInfo(
            userId = userId,
            passPhrase = "secret-key",
            email = "user@proton.me",
            name = "John Doe",
            displayName = null
        )

        val userAddress = LegacyUserAddressInfo(
            userId = userId,
            addressId = AddressId("address-123"),
            email = "addr@proton.me",
            displayName = "John A."
        )

        // When
        val result = mapper.mapToAccountMigrationInfo(
            sessionInfo = sessionInfo,
            user = user,
            userAddress = userAddress,
            isPrimaryUser = true
        )

        // Then
        assertEquals(userId, result.userId)
        assertEquals("John Doe", result.username)
        assertEquals("John Doe", result.displayName)
        assertEquals("addr@proton.me", result.primaryAddr)
        assertEquals("refresh-token", result.refreshToken)
        assertEquals("secret-key", result.keySecret)
        assertEquals(sessionId, result.sessionId)
        assertEquals(true, result.isPrimaryUser)
        assertEquals(AccountPasswordMode.TWO, result.passwordMode)
    }
}

