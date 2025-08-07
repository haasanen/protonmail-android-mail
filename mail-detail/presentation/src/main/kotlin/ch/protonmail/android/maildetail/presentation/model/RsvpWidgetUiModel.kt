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

interface RsvpWidgetUiModel {

    data class Shown(val event: RsvpEventUiModel) : RsvpWidgetUiModel
    data object Hidden : RsvpWidgetUiModel
    data object Loading : RsvpWidgetUiModel
    data object Error : RsvpWidgetUiModel
}

data class RsvpEventUiModel(
    val title: TextUiModel,
    val dateTime: TextUiModel,
    val isAttendanceOptional: Boolean,
    val buttons: RsvpButtonsUiModel,
    val calendar: RsvpCalendarUiModel?,
    val recurrence: TextUiModel?,
    val location: TextUiModel?,
    val organizer: RsvpOrganizerUiModel,
    val attendees: List<RsvpAttendeeUiModel>,
    val status: RsvpStatusUiModel?
)

sealed interface RsvpStatusUiModel {
    data object HappeningNow : RsvpStatusUiModel
    data object EventEnded : RsvpStatusUiModel
    data object EventCancelled : RsvpStatusUiModel
    data object InviteOutdated : RsvpStatusUiModel
    data object EventCancelledInviteOutdated : RsvpStatusUiModel
    data object NetworkFailure : RsvpStatusUiModel
    data object AddressIsIncorrect : RsvpStatusUiModel
    data object UserIsOrganizer : RsvpStatusUiModel
    data object EventDoesNotExist : RsvpStatusUiModel
}

data class RsvpAttendeeUiModel(
    val answer: RsvpAttendeeAnswer,
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
    data class Shown(
        val answer: RsvpAttendeeAnswer,
        val isAnsweringInProgress: Boolean
    ) : RsvpButtonsUiModel
}

enum class RsvpAttendeeAnswer { Yes, No, Maybe, Unanswered }
