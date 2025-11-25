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

package ch.protonmail.android.mailmailbox.data.repository.repository

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmailbox.data.local.SenderAddressDataSource
import ch.protonmail.android.mailmailbox.data.repository.SenderAddressRepositoryImpl
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class SenderAddressRepositoryImplTest {

    private val senderAddressDataSource: SenderAddressDataSource = mockk()
    private val userSessionRepository: UserSessionRepository = mockk()
    private val mockSessionWrapper: MailUserSessionWrapper = mockk()

    private lateinit var repository: SenderAddressRepositoryImpl

    private val testUserId = UserId("test_user")

    @Before
    fun setup() {
        repository = SenderAddressRepositoryImpl(
            senderAddressDataSource = senderAddressDataSource,
            userSessionRepository = userSessionRepository
        )
    }

    @Test
    fun `given userSession is null then return NoUserSession error flow`() = runTest {
        // given
        coEvery { userSessionRepository.getUserSession(testUserId) } returns null

        // when
        val flow = repository.observeUserHasValidSenderAddress(testUserId)

        // then
        flow.test {
            val expectedError = DataError.Local.NoUserSession.left()
            val actual = awaitItem()

            assertEquals(expectedError, actual)
            awaitComplete()
        }
    }

    @Test
    fun `given userSession is available when observing then delegate to datasource and returns datasource flow`() =
        runTest {
            // given
            coEvery { userSessionRepository.getUserSession(testUserId) } returns mockSessionWrapper

            // when
            val expectedResult = true.right()
            coEvery {
                senderAddressDataSource.observeUserHasValidSenderAddress(mockSessionWrapper)
            } returns flowOf(expectedResult)


            val flow = repository.observeUserHasValidSenderAddress(testUserId)

            // then
            flow.test {
                val actual = awaitItem()
                assertEquals(expectedResult, actual)
                awaitComplete()
            }
        }

    @Test
    fun `given userSession is available when observing then it delegates to datasource and returns flow error`() =
        runTest {
            // given
            coEvery { userSessionRepository.getUserSession(testUserId) } returns mockSessionWrapper

            val expectedError = DataError.Local.Unknown.left()
            coEvery {
                senderAddressDataSource.observeUserHasValidSenderAddress(mockSessionWrapper)
            } returns flowOf(expectedError)

            // when
            val flow = repository.observeUserHasValidSenderAddress(testUserId)

            // then
            flow.test {
                val actual = awaitItem()
                assertEquals(expectedError, actual)
                awaitComplete()
            }
        }

    @Test
    fun `given userSession is available when datasource emits multiple values then repository emits all values`() =
        runTest {
            // given
            coEvery { userSessionRepository.getUserSession(testUserId) } returns mockSessionWrapper

            val firstResult = false.right()
            val secondResult = true.right()

            coEvery {
                senderAddressDataSource.observeUserHasValidSenderAddress(mockSessionWrapper)
            } returns flowOf(firstResult, secondResult)

            // when
            val flow = repository.observeUserHasValidSenderAddress(testUserId)

            // then
            flow.test {
                assertEquals(firstResult, awaitItem())
                assertEquals(secondResult, awaitItem())
                awaitComplete()
            }
        }
}

