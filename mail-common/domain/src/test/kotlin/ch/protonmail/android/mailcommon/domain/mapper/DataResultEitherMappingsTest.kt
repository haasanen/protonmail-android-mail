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

package ch.protonmail.android.mailcommon.domain.mapper

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.test.utils.rule.LoggingTestRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DataResultEitherMappingsTest {

    @get:Rule
    val loggingTestRule = LoggingTestRule()

    @Test
    fun `emits data result value on success`() = runTest {
        // given
        val string1 = "hello"
        val string2 = "world"
        val input = flowOf(
            DataResult.Success(ResponseSource.Local, string1),
            DataResult.Success(ResponseSource.Remote, string2)
        )
        // when
        input.mapToEither().test {
            // then
            assertEquals(string1.right(), awaitItem())
            assertEquals(string2.right(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `does not emit anything on loading`() = runTest {
        // given
        val string = "hello"
        val input = flowOf(
            DataResult.Processing(ResponseSource.Remote),
            DataResult.Success(ResponseSource.Remote, string)
        )
        // when
        input.mapToEither().test {
            // then
            assertEquals(string.right(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `does log and return unknown local error for unhandled local error`() = runTest {
        // given
        val message = "an error occurred"
        val dataResult = DataResult.Error.Local(message, cause = Exception("Unknown exception"))
        val input = flowOf(dataResult)
        // when
        input.mapToEither().test {
            // then
            assertEquals(DataError.Local.Unknown.left(), awaitItem())
            loggingTestRule.assertErrorLogged("UNHANDLED LOCAL ERROR caused by result: $dataResult")
            awaitComplete()
        }
    }
}
