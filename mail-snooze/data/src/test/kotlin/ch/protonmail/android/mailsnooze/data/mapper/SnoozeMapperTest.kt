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

package ch.protonmail.android.mailsnooze.data.mapper

import ch.protonmail.android.mailsnooze.domain.model.SnoozeOption
import org.junit.Assert
import uniffi.proton_mail_uniffi.SnoozeActions
import uniffi.proton_mail_uniffi.SnoozeTime
import kotlin.test.Test
import kotlin.time.Instant

class SnoozeMapperTest {

    @Test
    fun `map snooze actions with custom option AND unsnooze`() {
        val snoozeActions = SnoozeActions(
            options = sampleSnoozeOptions.toMutableList().apply { add(SnoozeTime.Custom) },
            showUnsnooze = true
        )
        Assert.assertEquals(
            expectedSnoozeActions.toMutableList().apply {
                add(SnoozeOption.Allowed)
                add(SnoozeOption.UnSnooze)
            },
            snoozeActions.toSnoozeActions().toMutableList()
        )
    }

    @Test
    fun `map snooze actions with custom option AND unsnooze false`() {
        val snoozeActions = SnoozeActions(
            options = sampleSnoozeOptions.toMutableList().apply { add(SnoozeTime.Custom) },
            showUnsnooze = false
        )
        Assert.assertEquals(
            expectedSnoozeActions.toMutableList().apply {
                add(SnoozeOption.Allowed)
            },
            snoozeActions.toSnoozeActions().toMutableList()
        )
    }

    @Test
    fun `map snooze actions AND upsell`() {
        val snoozeActions = SnoozeActions(
            options = sampleSnoozeOptions.toMutableList(),
            showUnsnooze = false
        )
        Assert.assertEquals(
            expectedSnoozeActions.toMutableList().apply {
                add(SnoozeOption.UpgradeRequired)
            },
            snoozeActions.toSnoozeActions().toMutableList()
        )
    }

    companion object {

        val expectedInstant = Instant.fromEpochSeconds(1_754_394_159_278L)
        const val inputMs = 1_754_394_159_278L

        val sampleSnoozeOptions = listOf(
            SnoozeTime.Tomorrow(inputMs.toULong()),
            SnoozeTime.NextWeek(inputMs.toULong()),
            SnoozeTime.LaterThisWeek(inputMs.toULong()),
            SnoozeTime.ThisWeekend(inputMs.toULong())
        )

        val expectedSnoozeActions = listOf(
            SnoozeOption.Tomorrow(expectedInstant),
            SnoozeOption.NextWeek(expectedInstant),
            SnoozeOption.LaterThisWeek(expectedInstant),
            SnoozeOption.ThisWeekend(expectedInstant)
        )
    }
}


