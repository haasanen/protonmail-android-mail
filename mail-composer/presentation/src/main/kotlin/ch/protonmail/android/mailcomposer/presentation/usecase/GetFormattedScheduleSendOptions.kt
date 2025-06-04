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

package ch.protonmail.android.mailcomposer.presentation.usecase

import java.text.SimpleDateFormat
import java.util.Date
import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.usecase.GetAppLocale
import ch.protonmail.android.mailcomposer.domain.model.ScheduleSendOptions
import ch.protonmail.android.mailcomposer.domain.usecase.GetScheduleSendOptions
import ch.protonmail.android.mailcomposer.presentation.model.ScheduleSendOptionsUiModel
import javax.inject.Inject
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class GetFormattedScheduleSendOptions @Inject constructor(
    private val getScheduleSendOptions: GetScheduleSendOptions,
    private val getAppLocale: GetAppLocale
) {

    suspend operator fun invoke(): Either<DataError, ScheduleSendOptionsUiModel> =
        getScheduleSendOptions().map { it.toUiModel() }

    private fun Instant.format(): String {
        val date = Date.from(this.toJavaInstant())
        val formatter = SimpleDateFormat("dd MMM 'at' HH:mm", getAppLocale())
        return formatter.format(date)
    }

    private fun ScheduleSendOptions.toUiModel() = ScheduleSendOptionsUiModel(
        tomorrowTimeFormatted = this.tomorrowTime.format(),
        mondayTimeFormatted = this.mondayTime.format(),
        isCustomTimeOptionAvailable = this.isCustomTimeOptionAvailable
    )
}
