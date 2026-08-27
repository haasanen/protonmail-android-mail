/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package ch.protonmail.android.mailsettings.presentation.settings.privacy

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.mailcommon.presentation.compose.PickerDialog
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.BackgroundSyncInterval
import ch.protonmail.android.mailsettings.presentation.R

@Composable
fun BackgroundSyncIntervalPickerDialog(
    selected: BackgroundSyncInterval,
    onDismissRequest: () -> Unit,
    onValueSelected: (BackgroundSyncInterval) -> Unit
) {
    PickerDialog(
        title = stringResource(id = R.string.mail_settings_privacy_background_sync_interval),
        selectedValue = TextUiModel(selected.labelRes()),
        values = BackgroundSyncInterval.entries.map { TextUiModel(it.labelRes()) },
        onDismissRequest = onDismissRequest,
        onValueSelected = { value ->
            onValueSelected(BackgroundSyncInterval.entries.first { TextUiModel(it.labelRes()) == value })
        }
    )
}

internal fun BackgroundSyncInterval.labelRes(): Int = when (this) {
    BackgroundSyncInterval.REAL_TIME -> R.string.mail_settings_background_sync_interval_real_time
    BackgroundSyncInterval.EVERY_15_MINUTES -> R.string.mail_settings_background_sync_interval_15_min
    BackgroundSyncInterval.EVERY_30_MINUTES -> R.string.mail_settings_background_sync_interval_30_min
    BackgroundSyncInterval.EVERY_1_HOUR -> R.string.mail_settings_background_sync_interval_1_hour
    BackgroundSyncInterval.EVERY_2_HOURS -> R.string.mail_settings_background_sync_interval_2_hours
    BackgroundSyncInterval.EVERY_3_HOURS -> R.string.mail_settings_background_sync_interval_3_hours
    BackgroundSyncInterval.EVERY_5_HOURS -> R.string.mail_settings_background_sync_interval_5_hours
    BackgroundSyncInterval.EVERY_12_HOURS -> R.string.mail_settings_background_sync_interval_12_hours
    BackgroundSyncInterval.EVERY_24_HOURS -> R.string.mail_settings_background_sync_interval_24_hours
    BackgroundSyncInterval.NEVER -> R.string.mail_settings_background_sync_interval_never
}
