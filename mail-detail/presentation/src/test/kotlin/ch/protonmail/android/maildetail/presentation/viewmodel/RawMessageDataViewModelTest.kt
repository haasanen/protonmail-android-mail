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

package ch.protonmail.android.maildetail.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.maildetail.domain.usecase.DownloadRawMessageData
import ch.protonmail.android.maildetail.presentation.model.RawMessageDataState
import ch.protonmail.android.maildetail.presentation.model.RawMessageDataType
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.ui.RawMessageDataScreen
import ch.protonmail.android.maildetail.presentation.viewmodel.RawMessageDataViewModelTest.TestData.MESSAGE_ID
import ch.protonmail.android.maildetail.presentation.viewmodel.RawMessageDataViewModelTest.TestData.RAW_DATA_TYPE_HEADERS
import ch.protonmail.android.maildetail.presentation.viewmodel.RawMessageDataViewModelTest.TestData.RAW_DATA_TYPE_HTML
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.usecase.GetRawMessageBody
import ch.protonmail.android.mailmessage.domain.usecase.GetRawMessageHeaders
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.proton.core.util.kotlin.serialize
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RawMessageDataViewModelTest {

    private val downloadRawMessageData = mockk<DownloadRawMessageData>()
    private val getRawMessageBody = mockk<GetRawMessageBody>()
    private val getRawMessageHeaders = mockk<GetRawMessageHeaders>()
    private val savedStateHandle = mockk<SavedStateHandle> {
        every { this@mockk.get<String>(RawMessageDataScreen.MESSAGE_ID_KEY) } returns MESSAGE_ID
    }
    private val observePrimaryUserId = mockk<ObservePrimaryUserId> {
        every { this@mockk.invoke() } returns flowOf(UserIdTestData.userId)
    }

    private val rawMessageDataViewModel by lazy {
        RawMessageDataViewModel(
            downloadRawMessageData = downloadRawMessageData,
            getRawMessageBody = getRawMessageBody,
            getRawMessageHeaders = getRawMessageHeaders,
            savedStateHandle = savedStateHandle,
            observePrimaryUserId = observePrimaryUserId
        )
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `should emit correct state after successfully getting the message headers in the initialization`() = runTest {
        // Given
        val messageId = MessageId(MESSAGE_ID)
        val rawHeaders = "raw headers"
        every { savedStateHandle.get<String>(RawMessageDataScreen.RAW_DATA_TYPE_KEY) } returns RAW_DATA_TYPE_HEADERS
        coEvery { getRawMessageHeaders(UserIdTestData.userId, messageId) } returns rawHeaders.right()

        // When
        rawMessageDataViewModel.state.test {

            // Then
            val expected = RawMessageDataState.Data(
                type = RawMessageDataType.Headers,
                data = rawHeaders,
                toast = Effect.empty()
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should emit correct state after an error getting the message headers in the initialization`() = runTest {
        // Given
        val messageId = MessageId(MESSAGE_ID)
        every { savedStateHandle.get<String>(RawMessageDataScreen.RAW_DATA_TYPE_KEY) } returns RAW_DATA_TYPE_HEADERS
        coEvery { getRawMessageHeaders(UserIdTestData.userId, messageId) } returns DataError.Local.Unknown.left()

        // When
        rawMessageDataViewModel.state.test {

            // Then
            val expected = RawMessageDataState.Error(RawMessageDataType.Headers)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should emit correct state after successfully getting the message html in the initialization`() = runTest {
        // Given
        val messageId = MessageId(MESSAGE_ID)
        val rawBody = "raw body"
        every { savedStateHandle.get<String>(RawMessageDataScreen.RAW_DATA_TYPE_KEY) } returns RAW_DATA_TYPE_HTML
        coEvery { getRawMessageBody(UserIdTestData.userId, messageId) } returns rawBody.right()

        // When
        rawMessageDataViewModel.state.test {

            // Then
            val expected = RawMessageDataState.Data(
                type = RawMessageDataType.HTML,
                data = rawBody,
                toast = Effect.empty()
            )
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should emit correct state after an error getting the message html in the initialization`() = runTest {
        // Given
        val messageId = MessageId(MESSAGE_ID)
        every { savedStateHandle.get<String>(RawMessageDataScreen.RAW_DATA_TYPE_KEY) } returns RAW_DATA_TYPE_HTML
        coEvery { getRawMessageBody(UserIdTestData.userId, messageId) } returns DataError.Local.Unknown.left()

        // When
        rawMessageDataViewModel.state.test {

            // Then
            val expected = RawMessageDataState.Error(RawMessageDataType.HTML)
            assertEquals(expected, awaitItem())
        }
    }

    @Test
    fun `should emit state with success toast when downloading data is successful`() = runTest {
        // Given
        val messageId = MessageId(MESSAGE_ID)
        val rawBody = "raw body"
        every { savedStateHandle.get<String>(RawMessageDataScreen.RAW_DATA_TYPE_KEY) } returns RAW_DATA_TYPE_HTML
        coEvery { getRawMessageBody(UserIdTestData.userId, messageId) } returns rawBody.right()
        coEvery { downloadRawMessageData("html", rawBody) } returns Unit.right()

        rawMessageDataViewModel.state.test {
            awaitItem()

            // When
            rawMessageDataViewModel.downloadData(RawMessageDataType.HTML, rawBody)

            // Then
            assertEquals(
                R.string.raw_message_data_successful_download,
                (awaitItem() as RawMessageDataState.Data).toast.consume()
            )
        }
    }

    @Test
    fun `should emit state with failure toast when downloading data failed`() = runTest {
        // Given
        val messageId = MessageId(MESSAGE_ID)
        val rawHeaders = "raw headers"
        every { savedStateHandle.get<String>(RawMessageDataScreen.RAW_DATA_TYPE_KEY) } returns RAW_DATA_TYPE_HEADERS
        coEvery { getRawMessageHeaders(UserIdTestData.userId, messageId) } returns rawHeaders.right()
        coEvery { downloadRawMessageData("headers", rawHeaders) } returns DataError.Local.Unknown.left()

        rawMessageDataViewModel.state.test {
            awaitItem()

            // When
            rawMessageDataViewModel.downloadData(RawMessageDataType.Headers, rawHeaders)

            // Then
            assertEquals(
                R.string.raw_message_data_failed_download,
                (awaitItem() as RawMessageDataState.Data).toast.consume()
            )
        }
    }

    object TestData {

        const val MESSAGE_ID = "message_id"

        val RAW_DATA_TYPE_HEADERS = RawMessageDataType.Headers.serialize()
        val RAW_DATA_TYPE_HTML = RawMessageDataType.HTML.serialize()
    }
}
