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

package ch.protonmail.android.mailsnooze.presentation

import java.util.Locale
import java.util.TimeZone
import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.mailsnooze.domain.SnoozeRepository
import ch.protonmail.android.mailsnooze.domain.model.SnoozeError
import ch.protonmail.android.mailsnooze.domain.model.SnoozeWeekStart
import ch.protonmail.android.mailsnooze.domain.model.Tomorrow
import ch.protonmail.android.mailsnooze.domain.model.UpgradeRequired
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeConversationId
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOperationViewAction
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOptionsEffects
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeOptionsState
import ch.protonmail.android.mailsnooze.presentation.model.SnoozeUntilUiModel
import ch.protonmail.android.mailsnooze.presentation.model.UpgradeToSnoozeUiModel
import ch.protonmail.android.mailsnooze.presentation.model.mapper.DayTimeMapper
import ch.protonmail.android.mailsnooze.presentation.usecase.GetFirstDayOfWeekStart
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class SnoozeOptionsBottomSheetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(TestDispatcherProvider().Main)
    private lateinit var sut: SnoozeOptionsBottomSheetViewModel
    val mockAppLocale = mockk<GetAppLocale> {
        every { this@mockk.invoke() } returns Locale.UK
    }
    val getFirstDayOFWeek = GetFirstDayOfWeekStart(mockAppLocale)
    val dayTimeMapper = DayTimeMapper(mockAppLocale)
    val mockSnoozeRepository = mockk<SnoozeRepository> {
        coEvery {
            this@mockk.getAvailableSnoozeActions(
                userId,
                SnoozeWeekStart.MONDAY,
                inputItems
            )
        } returns outputSnoozeOptions.right()

        coEvery {
            this@mockk.snoozeConversation(
                userId,
                labelId,
                inputItems,
                inputSnoozeTime
            )
        } returns Unit.right()
    }

    val initialData = SnoozeBottomSheet.InitialData(
        userId = userId,
        labelId = labelId,
        items
    )

    @Before
    fun setUp() {
        mockkStatic(TimeZone::class)
        every { TimeZone.getDefault() } returns TimeZone.getTimeZone("Europe/Zurich")

        sut = SnoozeOptionsBottomSheetViewModel(
            initialData = initialData,
            dayTimeMapper = dayTimeMapper,
            getFirstDayOfWeekStart = getFirstDayOFWeek,
            snoozeRepository = mockSnoozeRepository
        )
    }

    @Test
    fun `emits Loading state when initialised`() = runTest {
        assertEquals(SnoozeOptionsState.Loading, sut.state.first())
    }

    @Test
    fun `emits loaded Data state when loaded`() = runTest {
        val mappedTime = "11:19"
        val uiOptions = listOf(
            SnoozeUntilUiModel(
                SnoozeOperationViewAction.SnoozeUntil(Tomorrow(outputInstant)),
                R.drawable.ic_proton_sun,
                TextUiModel(R.string.snooze_sheet_option_tomorrow),
                TextUiModel(mappedTime)
            ),
            UpgradeToSnoozeUiModel(SnoozeOperationViewAction.Upgrade)
        )

        sut.state.test {
            assertEquals(SnoozeOptionsState.Loading, awaitItem())
            assertEquals(SnoozeOptionsState.Data(uiOptions), awaitItem())
        }
    }

    @Test
    fun `when Operation SnoozeOperationViewAction SnoozeUntil AND success THEN emit Success effect`() = runTest {
        val mappedTime = "Wed, 11:19"
        // when
        sut.onAction(inputSnoozeOperation)

        sut.effects.test {
            val expected =
                TextUiModel(
                    R.string.snooze_sheet_success,
                    mappedTime
                )
            assertEquals(SnoozeOptionsEffects(), awaitItem())
            assertEquals(expected, awaitItem().success.consume())
        }
    }

    @Test
    fun `when Operation SnoozeOperationViewAction SnoozeUntil AND error THEN emit Error effect`() = runTest {
        coEvery {
            mockSnoozeRepository.snoozeConversation(
                userId,
                labelId,
                inputItems,
                inputSnoozeTime
            )
        } returns SnoozeError.Unknown().left()

        // when
        sut.onAction(inputSnoozeOperation)

        // then
        sut.effects.test {
            val expected =
                TextUiModel(
                    R.string.snooze_sheet_error_unable_to_snooze
                )

            assertEquals(SnoozeOptionsEffects(), awaitItem())
            assertEquals(expected, awaitItem().error.consume())
        }
    }

    companion object {

        val userId = UserId("UserId")
        val labelId = LabelId("labelId")
        val items = listOf<SnoozeConversationId>(SnoozeConversationId("1"), SnoozeConversationId("11"))
        val inputItems = listOf<ConversationId>(ConversationId("1"), ConversationId("11"))

        val outputInstant = Instant.fromEpochSeconds(1_754_643_935_975L)

        val inputSnoozeTime = Tomorrow(outputInstant)
        val inputSnoozeOperation =
            SnoozeOperationViewAction.SnoozeUntil(inputSnoozeTime)
        val outputSnoozeOptions = listOf(
            Tomorrow(outputInstant),
            UpgradeRequired
        )
    }
}
