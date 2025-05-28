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

package ch.protonmail.android.mailmailbox.data.mapper

import ch.protonmail.android.mailcommon.data.mapper.LocalAutoDeleteBanner
import ch.protonmail.android.mailcommon.data.mapper.LocalAutoDeleteState
import ch.protonmail.android.mailcommon.data.mapper.LocalSpamOrTrash
import ch.protonmail.android.mailmailbox.domain.model.AutoDeleteBanner
import ch.protonmail.android.mailmailbox.domain.model.AutoDeleteState
import ch.protonmail.android.mailmailbox.domain.model.SpamOrTrash

fun LocalAutoDeleteBanner.toAutoDeleteBanner() = AutoDeleteBanner(
    state = state.toAutoDeleteState(),
    folder = folder.toSpamOrTrash()
)

private fun LocalAutoDeleteState.toAutoDeleteState() = when (this) {
    LocalAutoDeleteState.AUTO_DELETE_UPSELL -> AutoDeleteState.AutoDeleteUpsell
    LocalAutoDeleteState.AUTO_DELETE_DISABLED -> AutoDeleteState.AutoDeleteDisabled
    LocalAutoDeleteState.AUTO_DELETE_ENABLED -> AutoDeleteState.AutoDeleteEnabled
}

private fun LocalSpamOrTrash.toSpamOrTrash() = when (this) {
    LocalSpamOrTrash.SPAM -> SpamOrTrash.Spam
    LocalSpamOrTrash.TRASH -> SpamOrTrash.Trash
}
