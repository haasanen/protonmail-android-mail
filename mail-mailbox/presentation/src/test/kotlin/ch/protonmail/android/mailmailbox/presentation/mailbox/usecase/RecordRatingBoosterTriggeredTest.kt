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

package ch.protonmail.android.mailmailbox.presentation.mailbox.usecase

import ch.protonmail.android.mailfeatureflags.domain.model.ShowRatingBoosterEnabled
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import kotlin.test.Test

class RecordRatingBoosterTriggeredTest {

    private val observePrimaryUserId: ObservePrimaryUserId = mockk()
    private val userSessionRepository: UserSessionRepository = mockk(relaxed = true)
    private lateinit var trigger: RecordRatingBoosterTriggered

    private val testUserId = UserId("user-123")
    private val expectedKey = ShowRatingBoosterEnabled.key
    private val expectedValue = false

    @Before
    fun setUp() {
        trigger = RecordRatingBoosterTriggered(
            observePrimaryUserId,
            userSessionRepository
        )
    }

    @Test
    fun `when primary user ID is available then feature flag is overridden to false`() = runTest {
        // Given
        coEvery { observePrimaryUserId.invoke() } returns flowOf(testUserId)

        // When
        trigger()

        // Then
        coVerify(exactly = 1) {
            userSessionRepository.overrideFeatureFlag(
                testUserId,
                expectedKey,
                expectedValue
            )
        }
    }

    @Test
    fun `when primary user ID is null then feature flag is not overridden`() = runTest {
        // Given
        coEvery { observePrimaryUserId.invoke() } returns flowOf(null)

        // When
        trigger()

        // Then
        coVerify(exactly = 0) { userSessionRepository.overrideFeatureFlag(any(), any(), any()) }
    }
}
