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

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmailbox.domain.repository.SenderAddressRepository
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import kotlin.test.Test

class ObserveValidSenderAddressTest {

    private val senderAddressRepository: SenderAddressRepository = mockk()

    private lateinit var useCase: ObserveValidSenderAddress

    private val testUserId = UserId("user_test_id")

    private val dataError = DataError.Local.Unknown

    @Before
    fun setup() {
        useCase = ObserveValidSenderAddress(senderAddressRepository)
    }

    @Test
    fun `given repository returns error then invoke returns TRUE`() = runTest {
        // given
        val repositoryResult = Either.Left(dataError)

        coEvery { senderAddressRepository.observeUserHasValidSenderAddress(userId = testUserId) } returns flowOf(
            repositoryResult
        )

        // when
        val actual = useCase(testUserId).first()

        // then
        assertEquals(true, actual)
    }

    @Test
    fun `given repository returns right(TRUE) then invoke returns TRUE`() = runTest {
        // given
        val repositoryResult = Either.Right(true)

        coEvery { senderAddressRepository.observeUserHasValidSenderAddress(userId = testUserId) } returns flowOf(
            repositoryResult
        )

        // when
        val actual = useCase(testUserId).first()

        // then
        assertEquals(true, actual)
    }

    @Test
    fun `given repository returns right(FALSE) then invoke returns FALSE`() = runTest {
        // given
        val repositoryResult = Either.Right(false)

        coEvery { senderAddressRepository.observeUserHasValidSenderAddress(userId = testUserId) } returns flowOf(
            repositoryResult
        )

        // when
        val actual = useCase(testUserId).first()

        // then
        assertEquals(false, actual)
    }
}
