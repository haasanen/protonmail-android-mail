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

package ch.protonmail.android.mailsnooze.presentation.usecase

import java.util.Locale
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailsnooze.domain.model.SnoozeWeekStart
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class GetFirstDayOfWeekStartTest {

    val mockAppLocale = mockk<GetAppLocale>()
    val sut = GetFirstDayOfWeekStart(mockAppLocale)

    @Test
    fun `GetFirstDayOfWeekStart for locale UK is Monday `() {
        every { mockAppLocale.invoke() } returns Locale.UK
        Assert.assertEquals(sut.invoke(), SnoozeWeekStart.MONDAY)
    }

    @Test
    fun `GetFirstDayOfWeekStart for locale US is Sunday `() {
        every { mockAppLocale.invoke() } returns Locale.US
        Assert.assertEquals(sut.invoke(), SnoozeWeekStart.SUNDAY)
    }

    @Test
    fun `GetFirstDayOfWeekStart for locale JAP is Sunday `() {
        every { mockAppLocale.invoke() } returns Locale.JAPAN
        Assert.assertEquals(sut.invoke(), SnoozeWeekStart.SUNDAY)
    }
}

