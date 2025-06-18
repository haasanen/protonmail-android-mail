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

package ch.protonmail.android.mailcomposer.presentation.ui

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.mailcomposer.presentation.R
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSendTimePickerBottomSheetContent(
    onClose: () -> Unit,
    onScheduleSendConfirmed: (Instant) -> Unit,
    modifier: Modifier = Modifier
) {

    val timePickerState = rememberTimePickerState(initialHour = INITIAL_TIME_HOUR, initialMinute = INITIAL_TIME_MINUTE)
    val datePickerState = rememberDatePickerState(
        initialSelectedDate = LocalDate.now(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= Clock.System.now().toEpochMilliseconds() - MILLIS_IN_A_DAY

            override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
        }
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ProtonTheme.colors.backgroundInvertedNorm)
            .scrollable(
                rememberScrollableState(consumeScrollDelta = { 0f }),
                orientation = Orientation.Vertical
            )
    ) {
        TopBar(
            onClose = onClose,
            onSendClicked = {
                // Gather time from date and time pickers and schedule send
            },
            modifier = Modifier.padding(
                start = ProtonDimens.Spacing.Small,
                top = 0.dp,
                end = ProtonDimens.Spacing.Medium,
                bottom = ProtonDimens.Spacing.Medium
            )
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(ProtonTheme.colors.backgroundInvertedNorm)
                .padding(ProtonDimens.Spacing.Large)
        ) {

            ScheduleSendTime(timePickerState)

            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.ExtraLarge))

            ScheduleSendDatePicker(datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleSendTime(timePickerState: TimePickerState, modifier: Modifier = Modifier) {

    val pickedTime = remember { mutableStateOf("") }
    val showTimePicker = remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.current.platformLocale) }

    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
        calendar.set(Calendar.MINUTE, timePickerState.minute)
        calendar.isLenient = false
        pickedTime.value = formatter.format(calendar.time)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ProtonTheme.shapes.extraLarge)
            .background(
                color = ProtonTheme.colors.backgroundInvertedSecondary,
                shape = ProtonTheme.shapes.extraLarge
            )
            .padding(ProtonDimens.Spacing.Large),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(R.string.composer_schedule_send_custom_time_label),
            style = ProtonTheme.typography.bodyMediumNorm,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))

        Button(
            modifier = Modifier
                .background(
                    color = ProtonTheme.colors.backgroundInvertedDeep,
                    shape = ProtonTheme.shapes.medium
                )
                .height(42.dp)
                .padding(horizontal = 0.dp),
            onClick = { showTimePicker.value = true },
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = ProtonTheme.colors.backgroundInvertedDeep
            ),
            shape = ProtonTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = ProtonDimens.Spacing.Medium)
        ) {
            Text(
                modifier = Modifier,
                text = pickedTime.value,
                style = ProtonTheme.typography.bodyLargeNorm,
                maxLines = 1
            )
        }
    }

    if (showTimePicker.value) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker.value = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker.value = false }) {
                    Text(stringResource(R.string.composer_schedule_send_dialog_ok))
                }
            },
            title = {
                Text(
                    stringResource(R.string.composer_schedule_send_dialog_enter_time),
                    style = ProtonTheme.typography.bodyMediumNorm
                )
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
            }
        ) {
            TimeInput(
                state = timePickerState,
                colors = TimePickerDefaults.colors().copy(
                    containerColor = ProtonTheme.colors.backgroundInvertedNorm,
                    timeSelectorSelectedContainerColor = ProtonTheme.colors.backgroundInvertedDeep,
                    timeSelectorUnselectedContainerColor = ProtonTheme.colors.backgroundInvertedDeep,
                    periodSelectorSelectedContainerColor = ProtonTheme.colors.interactionBrandWeakPressed,
                    periodSelectorUnselectedContainerColor = ProtonTheme.colors.backgroundInvertedDeep
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleSendDatePicker(datePickerState: DatePickerState, modifier: Modifier = Modifier) {

    val pickedDate = remember { mutableStateOf("") }
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    LaunchedEffect(datePickerState.getSelectedDate()) {
        pickedDate.value = datePickerState.getSelectedDate()?.format(formatter) ?: ""
    }

    Column {
        DatePicker(
            modifier = modifier
                .clip(ProtonTheme.shapes.extraLarge)
                .background(
                    color = ProtonTheme.colors.backgroundInvertedSecondary,
                    shape = ProtonTheme.shapes.extraLarge
                ),
            state = datePickerState,
            showModeToggle = false,
            colors = DatePickerDefaults.colors().copy(
                containerColor = ProtonTheme.colors.backgroundNorm,
                dividerColor = ProtonTheme.colors.backgroundInvertedNorm
            ),
            title = null,
            headline = {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(ProtonDimens.Spacing.Large),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.composer_schedule_send_custom_date_label),
                        style = ProtonTheme.typography.bodyMediumNorm,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))

                    Text(
                        modifier = Modifier
                            .background(
                                color = ProtonTheme.colors.backgroundInvertedDeep,
                                shape = ProtonTheme.shapes.medium
                            )
                            .padding(ProtonDimens.Spacing.Medium)
                            .align(Alignment.CenterVertically),
                        text = pickedDate.value,
                        style = ProtonTheme.typography.bodyLargeNorm,
                        maxLines = 1
                    )
                }
            }
        )
    }
}

@Composable
private fun TopBar(
    onClose: () -> Unit,
    onSendClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_cross),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm

            )
        }

        Text(
            modifier = Modifier
                .background(ProtonTheme.colors.backgroundInvertedNorm),
            text = stringResource(R.string.composer_schedule_send_content_description),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = ProtonTheme.typography.titleMediumNorm,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = onSendClicked,
            modifier = Modifier,
            shape = ProtonTheme.shapes.huge,
            colors = ButtonDefaults.buttonColors(
                containerColor = ProtonTheme.colors.interactionBrandWeakNorm,
                disabledContainerColor = ProtonTheme.colors.interactionBrandWeakDisabled,
                contentColor = ProtonTheme.colors.textAccent,
                disabledContentColor = ProtonTheme.colors.brandMinus20
            ),
            contentPadding = PaddingValues(
                horizontal = ProtonDimens.Spacing.Large,
                vertical = ProtonDimens.Spacing.Standard
            )
        ) {
            Text(
                text = stringResource(R.string.send_button_title),
                style = ProtonTheme.typography.titleSmall
            )
        }
    }
}

private const val INITIAL_TIME_HOUR = 8
private const val INITIAL_TIME_MINUTE = 0

private const val MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24

@Preview(showBackground = true)
@Composable
private fun PreviewScheduleSendTimePickerBottomSheet() {
    ProtonTheme {
        ScheduleSendTimePickerBottomSheetContent(
            onClose = {},
            onScheduleSendConfirmed = {}
        )
    }
}
