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

package ch.protonmail.android.mailmessage.domain.sample

import ch.protonmail.android.mailmessage.domain.model.Message
import ch.protonmail.android.mailmessage.domain.model.MessageWithLabels
import me.proton.core.label.domain.entity.Label

object MessageWithLabelsSample {

    val AugWeatherForecast = build(
        message = MessageSample.AugWeatherForecast
    )

    val EmptyDraft = build(
        message = MessageSample.EmptyDraft
    )

    val Invoice = build(
        message = MessageSample.Invoice
    )

    val SepWeatherForecast = build(
        message = MessageSample.SepWeatherForecast
    )

    fun build(message: Message = MessageSample.build(), labels: List<Label> = emptyList()) = MessageWithLabels(
        message = message,
        labels = labels
    )
}
