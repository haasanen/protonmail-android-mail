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

package ch.protonmail.android.mailsnooze.presentation.model.mapper

import java.util.Locale
import java.util.TimeZone
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsnooze.domain.model.Tomorrow
import ch.protonmail.android.mailsnooze.presentation.R
import ch.protonmail.android.mailsnooze.presentation.model.mapper.SnoozeSuccessMapper.toSuccessMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.time.Instant

class SnoozeSuccessMapperTest {

    val mockAppLocale = mockk<GetAppLocale> {
        every { this@mockk.invoke() } returns Locale.UK
    }

    val dayTimeMapper = DayTimeMapper(mockAppLocale)

    @Before
    fun init() {
        mockkStatic(TimeZone::class)
        every { TimeZone.getDefault() } returns TimeZone.getTimeZone("Europe/Zurich")
    }


    @Test
    fun `when map SnoozeTime to Success`() {
        val tuesday142 = 1_754_394_159_278
        assertEquals(
            TextUiModel(R.string.snooze_sheet_success, "Tue, 1:42"),
            Tomorrow(
                Instant.fromEpochMilliseconds(tuesday142)
            ).toSuccessMessage(dayTimeMapper)
        )
    }
}
