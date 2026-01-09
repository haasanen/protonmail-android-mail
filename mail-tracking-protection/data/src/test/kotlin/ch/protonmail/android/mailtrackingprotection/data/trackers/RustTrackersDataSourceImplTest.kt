/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailtrackingprotection.data.trackers

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.data.mapper.LocalMessageId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailtrackingprotection.data.wrapper.RustTrackersWrapper
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uniffi.proton_mail_uniffi.Id
import uniffi.proton_mail_uniffi.TrackerDomain
import uniffi.proton_mail_uniffi.WatchTrackerInfoStream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RustTrackersDataSourceImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dataSource: RustTrackersDataSource

    private val mockWrapper = mockk<RustTrackersWrapper>()
    private val mockStream = mockk<WatchTrackerInfoStream>(relaxed = true)

    private val testMessageId: LocalMessageId = Id(123UL)
    private val testTrackerDomain = TrackerDomain("tracker.example.com", emptyList())

    @BeforeTest
    fun setup() {
        dataSource = RustTrackersDataSourceImpl()
    }

    @Test
    fun `observe trackers returns flow of trackers successfully with initial info`() = runTest {
        // Given
        coEvery { mockWrapper.watchTrackerInfoStream(testMessageId) } returns mockStream.right()
        every { mockStream.initialInfo() } returns mockk {
            every { trackers } returns listOf(testTrackerDomain)
        }

        // When
        dataSource.observeTrackers(mockWrapper, testMessageId).test {
            // Then
            val trackerInfo = awaitItem()
            assertTrue(trackerInfo.isRight())
            assertEquals(listOf(testTrackerDomain), trackerInfo.getOrNull())
            awaitComplete()
        }
    }

    @Test
    fun `observe trackers returns flow of error when watch stream fails`() = runTest {
        // Given
        val expected = DataError.Local.NoUserSession
        coEvery { mockWrapper.watchTrackerInfoStream(testMessageId) } returns expected.left()

        // When
        dataSource.observeTrackers(mockWrapper, testMessageId).test {
            // Then
            val trackerInfo = awaitItem()
            assertTrue(trackerInfo.isLeft())
            assertEquals(expected, trackerInfo.swap().getOrNull())
            awaitComplete()
        }
    }
}
