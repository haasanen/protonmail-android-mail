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

package ch.protonmail.android.testdata.action

import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailcommon.domain.model.AvailableActions

object AvailableActionsTestData {

    val replyActionsOnly = AvailableActions(
        listOf(Action.ReplyAll),
        emptyList(),
        emptyList(),
        emptyList()
    )

    val replyReportPhishing = AvailableActions(
        listOf(Action.Reply),
        emptyList(),
        emptyList(),
        listOf(Action.ReportPhishing)
    )

    val forwardReportPhishingActions = AvailableActions(
        listOf(Action.Reply),
        emptyList(),
        emptyList(),
        listOf(Action.ReportPhishing)
    )

    val fullAvailableActions = AvailableActions(
        listOf(Action.Reply),
        listOf(Action.MarkRead, Action.Star),
        listOf(Action.Archive, Action.Trash),
        listOf(Action.ReportPhishing, Action.SavePdf)
    )
}
