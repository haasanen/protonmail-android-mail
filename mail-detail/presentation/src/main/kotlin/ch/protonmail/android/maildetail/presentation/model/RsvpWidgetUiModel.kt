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

package ch.protonmail.android.maildetail.presentation.model

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel

data class RsvpWidgetUiModel(
    val title: TextUiModel,
    val date: TextUiModel,
    val time: TextUiModel,
    val isAttendanceOptional: Boolean,
    val buttons: RsvpButtonsUiModel,
    val calendar: RsvpCalendarUiModel,
    val recurrence: TextUiModel?,
    val location: TextUiModel,
    val organizer: RsvpOrganizerUiModel,
    val attendees: List<RsvpAttendeeUiModel>,
    val status: RsvpStatusUiModel?
)

data class RsvpStatusUiModel(
    val message: TextUiModel,
    val textColor: Color,
    val backgroundColor: Color
)

data class RsvpAttendeeUiModel(
    val answer: RsvpAnswer,
    val name: TextUiModel?,
    val email: TextUiModel
)

data class RsvpOrganizerUiModel(
    val name: TextUiModel?,
    val email: TextUiModel
)

data class RsvpCalendarUiModel(
    val color: Color,
    val name: TextUiModel
)

sealed interface RsvpButtonsUiModel {
    data object Hidden : RsvpButtonsUiModel
    data class Shown(val answer: RsvpAnswer) : RsvpButtonsUiModel
}

enum class RsvpAnswer { Yes, No, Maybe, Unanswered }
