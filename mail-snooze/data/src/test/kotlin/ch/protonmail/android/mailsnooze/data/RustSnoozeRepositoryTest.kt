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

package ch.protonmail.android.mailsnooze.data

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsnooze.data.mapper.SnoozeMapperTest.Companion.expectedInstant
import ch.protonmail.android.mailsnooze.domain.model.SnoozeOption
import ch.protonmail.android.mailsnooze.domain.model.SnoozeWeekStart
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uniffi.proton_mail_uniffi.Id
import uniffi.proton_mail_uniffi.NonDefaultWeekStart
import uniffi.proton_mail_uniffi.SnoozeActions
import uniffi.proton_mail_uniffi.SnoozeTime

class RustSnoozeRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dataSource = mockk<RustSnoozeDataSource> {
        coEvery {
            this@mockk.getAvailableSnoozeActionsForConversation(
                SampleData.userId,
                SampleData.weekStart,
                SampleData.conversationIds
            )
        } returns SampleData.mockSnoozeActions.right()
    }
    private val sut = RustSnoozeRepositoryImpl(dataSource)

    @Test
    fun `when getAvailableSnoozeActionsForConversation then returns Result right`() = runTest {

        val result = sut.getAvailableSnoozeActions(
            SampleData.userId, SampleData.inputWeekStart,
            SampleData.inputConversationIds
        )

        assertEquals(
            listOf(
                SnoozeOption.Tomorrow(expectedInstant),
                SnoozeOption.NextWeek(expectedInstant),
                SnoozeOption.LaterThisWeek(expectedInstant),
                SnoozeOption.ThisWeekend(expectedInstant),
                SnoozeOption.UpgradeRequired
            ).right(),
            result
        )
    }

    @Test
    fun `when getAvailableSnoozeActionsForConversation then returns Result left`() = runTest {
        val error = DataError.Local.Unknown.left()
        coEvery {
            dataSource.getAvailableSnoozeActionsForConversation(
                SampleData.userId,
                SampleData.weekStart,
                SampleData.conversationIds
            )
        } returns error

        val result = sut.getAvailableSnoozeActions(
            SampleData.userId, SampleData.inputWeekStart,
            SampleData.inputConversationIds
        )

        assertEquals(error, result)
    }

    object SampleData {

        val userId = UserId("UserId")
        val inputWeekStart = SnoozeWeekStart.MONDAY
        val weekStart = NonDefaultWeekStart.MONDAY
        private const val conversationIdFirst = 12
        private const val conversationIdSecond = 22
        val inputConversationIds =
            listOf(ConversationId(conversationIdFirst.toString()), ConversationId(conversationIdSecond.toString()))
        val conversationIds = listOf(Id(conversationIdFirst.toULong()), Id(conversationIdSecond.toULong()))
        const val inputMs = 1_754_394_159_278L
        val mockSnoozeActions =
            SnoozeActions(
                options = listOf(
                    SnoozeTime.Tomorrow(inputMs.toULong()),
                    SnoozeTime.NextWeek(inputMs.toULong()),
                    SnoozeTime.LaterThisWeek(inputMs.toULong()),
                    SnoozeTime.ThisWeekend(inputMs.toULong())
                ),
                showUnsnooze = false
            )
    }
}
