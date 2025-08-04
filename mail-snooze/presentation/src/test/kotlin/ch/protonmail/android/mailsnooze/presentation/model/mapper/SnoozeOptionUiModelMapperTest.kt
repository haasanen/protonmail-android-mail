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
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsnooze.domain.model.SnoozeOption
import ch.protonmail.android.mailsnooze.presentation.R
import ch.protonmail.android.mailsnooze.presentation.model.CustomSnoozeUiModel
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOperationViewAction
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeUntilUiModel
import ch.protonmail.android.mailsnooze.presentation.model.UnSnooze
import ch.protonmail.android.mailsnooze.presentation.model.UpgradeToSnoozeUiModel
import ch.protonmail.android.mailsnooze.presentation.model.mapper.SnoozeOptionUiModelMapper.toSnoozeOptionUiModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import kotlin.time.Instant

class SnoozeOptionUiModelMapperTest {

    val time = Instant.fromEpochMilliseconds(1_754_394_159_278)
    val mappedDayTime = "Tue, 1:42"
    val mappedTime = "1:42"
    val mockAppLocale = mockk<GetAppLocale> {
        every { this@mockk.invoke() } returns Locale.UK
    }

    val dayTimeMapper = DayTimeMapper(mockAppLocale)

    @Test
    fun `when map snoozeOption NextWeek`() {
        val mapped = SnoozeOption.NextWeek(time).toSnoozeOptionUiModel(dayTimeMapper)
        Assert.assertEquals(
            SnoozeUntilUiModel(
                SnoozeOperationViewAction.SnoozeUntil,
                R.drawable.ic_proton_briefcase,
                TextUiModel(R.string.snooze_sheet_option_next_week),
                TextUiModel(mappedDayTime)
            ),
            mapped
        )
    }

    @Test
    fun `when map snoozeOption This Weekend`() {
        val mapped = SnoozeOption.ThisWeekend(time).toSnoozeOptionUiModel(dayTimeMapper)
        Assert.assertEquals(
            SnoozeUntilUiModel(
                SnoozeOperationViewAction.SnoozeUntil,
                R.drawable.ic_proton_chair,
                TextUiModel(R.string.snooze_sheet_option_this_weekend),
                TextUiModel(mappedDayTime)
            ),
            mapped
        )
    }

    @Test
    fun `when map snoozeOption Later`() {
        val mapped = SnoozeOption.LaterThisWeek(time).toSnoozeOptionUiModel(dayTimeMapper)
        Assert.assertEquals(
            SnoozeUntilUiModel(
                SnoozeOperationViewAction.SnoozeUntil,
                R.drawable.ic_proton_sun_half,
                TextUiModel(R.string.snooze_sheet_option_later_this_week),
                TextUiModel(mappedDayTime)
            ),
            mapped
        )
    }


    @Test
    fun `when map snoozeOption Tomorrow`() {
        val mapped = SnoozeOption.Tomorrow(time).toSnoozeOptionUiModel(dayTimeMapper)
        Assert.assertEquals(
            SnoozeUntilUiModel(
                SnoozeOperationViewAction.SnoozeUntil,
                R.drawable.ic_proton_sun,
                TextUiModel(R.string.snooze_sheet_option_tomorrow),
                TextUiModel(mappedTime)
            ),
            mapped
        )
    }

    @Test
    fun `when map snoozeOption pick custom time`() {
        val mapped = SnoozeOption.Allowed.toSnoozeOptionUiModel(dayTimeMapper)
        Assert.assertEquals(
            CustomSnoozeUiModel(SnoozeOperationViewAction.PickSnooze),
            mapped
        )
    }

    @Test
    fun `when map snoozeOption upgrade`() {
        val mapped = SnoozeOption.UpgradeRequired.toSnoozeOptionUiModel(dayTimeMapper)
        Assert.assertEquals(
            UpgradeToSnoozeUiModel(SnoozeOperationViewAction.Upgrade),
            mapped
        )
    }

    @Test
    fun `when map snoozeOption unsnooze`() {
        val mapped = SnoozeOption.UnSnooze.toSnoozeOptionUiModel(dayTimeMapper)
        Assert.assertEquals(
            UnSnooze,
            mapped
        )
    }
}
