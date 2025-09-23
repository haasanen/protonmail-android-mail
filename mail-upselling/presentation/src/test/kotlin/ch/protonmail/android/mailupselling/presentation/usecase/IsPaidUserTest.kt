/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailupselling.presentation.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsession.domain.usecase.ObserveUser
import ch.protonmail.android.mailupselling.domain.usecase.IsPaidUser
import ch.protonmail.android.testdata.user.UserIdTestData
import ch.protonmail.android.testdata.user.UserTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

internal class IsPaidUserTest {

    private val userId = UserIdTestData.userId

    private val observeUser = mockk<ObserveUser>()

    private val isPaidUser = IsPaidUser(observeUser)

    @Test
    fun `returns true when the user is valid and has any paid plan`() = runTest {
        // Given
        every { observeUser.invoke(userId) } returns flowOf(UserTestData.paidUser.right())

        // When
        val actual = isPaidUser(userId)

        // Then
        assertEquals(true.right(), actual)
    }

    @Test
    fun `returns false when user is valid and is not paid`() = runTest {
        // Given
        every { observeUser.invoke(userId) } returns flowOf(UserTestData.freeUser.right())

        // When
        val actual = isPaidUser(userId)

        // Then
        assertEquals(false.right(), actual)
    }

    @Test
    fun `returns error when user is not valid`() = runTest {
        // Given
        every { observeUser.invoke(userId) } returns flowOf(DataError.Local.CryptoError.left())

        // When
        val actual = isPaidUser(userId)

        // Then
        assertEquals(DataError.Local.CryptoError.left(), actual)
    }

}
