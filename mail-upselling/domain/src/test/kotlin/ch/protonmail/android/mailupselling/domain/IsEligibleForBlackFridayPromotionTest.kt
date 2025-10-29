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

package ch.protonmail.android.mailupselling.domain

import arrow.core.right
import ch.protonmail.android.mailsession.domain.usecase.ObserveUser
import ch.protonmail.android.mailupselling.domain.usecase.HasInAppNotificationsEnabled
import ch.protonmail.android.mailupselling.domain.usecase.IsEligibleForBlackFridayPromotion
import ch.protonmail.android.testdata.user.UserTestData
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IsEligibleForBlackFridayPromotionTest {

    private val hasInAppNotificationsEnabled = mockk<HasInAppNotificationsEnabled>()
    private val observeUser = mockk<ObserveUser>()

    private lateinit var isEligibleForBlackFridayPromotion: IsEligibleForBlackFridayPromotion

    @BeforeTest
    fun setup() {
        isEligibleForBlackFridayPromotion = IsEligibleForBlackFridayPromotion(
            hasInAppNotificationsEnabled,
            observeUser
        )
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `should return false when user id can't be determined`() = runTest {
        // Given
        every { observeUser(userId) } returns flowOf()

        // When
        val actual = isEligibleForBlackFridayPromotion(userId)

        // Then
        assertFalse(actual)
    }

    @Test
    fun `should return false when user is delinquent`() = runTest {
        // Given
        every { observeUser(userId) } returns flowOf(UserTestData.freeUser.copy(delinquent = 1).right())

        // When
        val actual = isEligibleForBlackFridayPromotion(userId)

        // Then
        assertFalse(actual)
    }

    @Test
    fun `should return false when user has IAN disabled`() = runTest {
        // Given
        every { observeUser(userId) } returns flowOf(UserTestData.freeUser.right())
        coEvery { hasInAppNotificationsEnabled(userId) } returns false.right()

        // When
        val actual = isEligibleForBlackFridayPromotion(userId)

        // Then
        assertFalse(actual)
    }

    @Test
    fun `should return true when user exists, is not delinquent and has IAN enabled`() = runTest {
        // Given
        every { observeUser(userId) } returns flowOf(UserTestData.freeUser.right())
        coEvery { hasInAppNotificationsEnabled(userId) } returns true.right()

        // When
        val actual = isEligibleForBlackFridayPromotion(userId)

        // Then
        assertTrue(actual)
    }

    private companion object {

        val userId = UserTestData.freeUser.userId
    }
}
