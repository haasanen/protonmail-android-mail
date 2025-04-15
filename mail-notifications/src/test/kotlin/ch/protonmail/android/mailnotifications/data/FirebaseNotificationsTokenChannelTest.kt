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

package ch.protonmail.android.mailnotifications.data

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FirebaseNotificationsTokenChannelTest {

    @Test
    fun `should send and receive the sent strings in the correct order`() = runTest {
        // Given
        val channel = FirebaseNotificationsTokenChannel()
        val strings = List(10) { index -> "string$index" }

        // When
        strings.forEach { channel.sendToken(it) }

        // Then
        channel.tokenFlow.test {
            strings.forEach { expectedString ->
                assertEquals(expectedString, awaitItem())
            }

            expectNoEvents()
        }
    }
}
