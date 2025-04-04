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

package ch.protonmail.android.maillabel.presentation.bottomsheet

import ch.protonmail.android.mailmessage.domain.model.MessageId
import me.proton.core.mailsettings.domain.entity.ViewMode

sealed interface LabelAsBottomSheetEntryPoint {
    data object Conversation : LabelAsBottomSheetEntryPoint
    data class Message(val messageId: MessageId) : LabelAsBottomSheetEntryPoint

    sealed class Mailbox(val viewMode: ViewMode) : LabelAsBottomSheetEntryPoint {
        class LabelAsSwipeAction(viewMode: ViewMode, val itemId: LabelAsItemId) : Mailbox(viewMode)
        class SelectionMode(viewMode: ViewMode) : Mailbox(viewMode)
    }
}
