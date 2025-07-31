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

import ch.protonmail.android.mailsnooze.domain.model.NoSnooze
import ch.protonmail.android.mailsnooze.domain.model.SnoozeReminder
import ch.protonmail.android.mailsnooze.domain.model.Snoozed
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class ConversationSnoozeStatusMapperTest {

    @Test
    fun `map snooze reminder`() {
        val mapped = LocalConversationTestData.spamConversation.copy(displaySnoozeReminder = true)
            .toSnoozeInformation()

        assertEquals(SnoozeReminder, mapped)
    }

    @Test
    fun `map snoozed Until`() {
        val seventhOfAugust = 1_754_722_800L
        val mapped = LocalConversationTestData.spamConversation.copy(
            displaySnoozeReminder = false,
            snoozedUntil = seventhOfAugust.toULong()
        ) // 07 Aug 2025
            .toSnoozeInformation()

        assertEquals(Snoozed(Instant.fromEpochSeconds(seventhOfAugust)), mapped)
    }

    @Test
    fun `map no snooze`() {
        val mapped = LocalConversationTestData.spamConversation.copy(
            displaySnoozeReminder = false,
            snoozedUntil = null
        )
            .toSnoozeInformation()

        assertEquals(NoSnooze, mapped)
    }
}
