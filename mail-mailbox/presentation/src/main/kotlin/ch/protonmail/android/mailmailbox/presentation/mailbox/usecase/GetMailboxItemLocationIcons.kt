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

package ch.protonmail.android.mailmailbox.presentation.mailbox.usecase

import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemLocationUiModel
import ch.protonmail.android.mailsettings.domain.model.FolderColorSettings
import javax.inject.Inject

/**
 * Defines the list of locations for which a mailbox item should show a
 * location icon, based on the currently selected location (mailbox user is looking at)
 */
@MissingRustApi
class GetMailboxItemLocationIcons @Inject constructor(
    private val selectedMailLabelId: SelectedMailLabelId
) {

    suspend operator fun invoke(
        mailboxItem: MailboxItem,
        folderColorSettings: FolderColorSettings,
        isShowingSearchResults: Boolean
    ): Result {
        if (!currentLocationShouldShowIcons() && !isShowingSearchResults) {
            return Result.None
        }

        val icons = getLocationIcons(mailboxItem)
        if (icons.isEmpty()) {
            // Having no icons can happen when an item was in a custom folder which got
            // deleted. Such item is now only in all mail and no other location.
            // This is handled by showing no location icons, product discussion on alternatives ongoing
            return Result.None
        }
        return Result.Icons(icons.first(), icons.getOrNull(1), icons.getOrNull(2))
    }

    @MissingRustApi
    // Need rust to expose exclusiveLocation, which will allow mailboxItem to define its own location
    private suspend fun getLocationIcons(mailboxItem: MailboxItem): MutableList<MailboxItemLocationUiModel> {
        val icons = mutableListOf<MailboxItemLocationUiModel>()

        return icons
    }

    @MissingRustApi
    private fun currentLocationShouldShowIcons(): Boolean {
        val currentLocation = selectedMailLabelId.flow.value

        // Should show when starred, all mail or custom label
        // Removed when introducing dynamic system folders.
        // Now we would need to get mail labels to check for system folders Ids.
        // which won't be done as we expect rust to expose the icons
        // for a given item directly
        return false
    }

    sealed interface Result {
        object None : Result
        data class Icons(
            val first: MailboxItemLocationUiModel,
            val second: MailboxItemLocationUiModel? = null,
            val third: MailboxItemLocationUiModel? = null
        ) : Result
    }
}
