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

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsnooze.domain.model.SnoozeError
import ch.protonmail.android.mailsnooze.presentation.R
import ch.protonmail.android.mailsnooze.presentation.model.mapper.SnoozeErrorMapper.toUIModel
import org.junit.Assert.assertEquals
import org.junit.Test

class SnoozeErrorMapperTest {

    @Test
    fun `when map SnoozeIsInThePast to UIError`() {
        assertEquals(
            TextUiModel(R.string.snooze_sheet_error_in_the_past), SnoozeError.SnoozeIsInThePast.toUIModel()
        )
    }

    @Test
    fun `when map general SnoozeError to UIError`() {
        assertEquals(
            TextUiModel(R.string.snooze_sheet_error_unable_to_snooze), SnoozeError.Other().toUIModel()
        )
        assertEquals(
            TextUiModel(R.string.snooze_sheet_error_unable_to_snooze), SnoozeError.InvalidSnoozeLocation.toUIModel()
        )
    }
}
