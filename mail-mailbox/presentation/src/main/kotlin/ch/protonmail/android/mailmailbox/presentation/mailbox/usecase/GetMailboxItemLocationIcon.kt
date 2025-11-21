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

import androidx.compose.ui.graphics.Color
import arrow.core.getOrElse
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.maillabel.domain.model.ExclusiveLocation
import ch.protonmail.android.maillabel.domain.usecase.GetSelectedMailLabelId
import ch.protonmail.android.maillabel.presentation.iconRes
import ch.protonmail.android.mailmailbox.domain.model.MailboxItem
import ch.protonmail.android.mailmailbox.domain.usecase.ShouldShowLocationIndicator
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.MailboxItemLocationUiModel
import ch.protonmail.android.mailsettings.domain.model.FolderColorSettings
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetMailboxItemLocationIcon @Inject constructor(
    private val getSelectedMailLabelId: GetSelectedMailLabelId,
    private val shouldShowLocationIndicator: ShouldShowLocationIndicator,
    private val colorMapper: ColorMapper
) {

    suspend operator fun invoke(
        userId: UserId,
        mailboxItem: MailboxItem,
        folderColorSettings: FolderColorSettings,
        isShowingSearchResults: Boolean
    ): Result {
        if (!shouldShowIcons(userId, mailboxItem) && !isShowingSearchResults) {
            return Result.None
        }

        val icon = getLocationIcons(mailboxItem, folderColorSettings).takeIf { it.isNotEmpty() } ?: return Result.None
        return Result.Icon(icon)
    }

    private fun getLocationIcons(
        mailboxItem: MailboxItem,
        folderColorSettings: FolderColorSettings
    ): List<MailboxItemLocationUiModel> {
        return mailboxItem.exclusiveLocations.take(ICON_COUNT_THRESHOLD).mapNotNull { location ->
            when (location) {
                is ExclusiveLocation.System -> {
                    val iconDrawable = location.systemLabelId.iconRes()
                    MailboxItemLocationUiModel(iconDrawable)
                }

                is ExclusiveLocation.Folder -> {
                    when (folderColorSettings.useFolderColor) {
                        true -> MailboxItemLocationUiModel(
                            icon = R.drawable.ic_proton_folder_filled,
                            color = colorMapper.toColor(location.color).getOrElse { Color.Transparent }
                        )

                        false -> MailboxItemLocationUiModel(R.drawable.ic_proton_folder)
                    }
                }

                ExclusiveLocation.NoLocation -> null
            }
        }
    }

    private suspend fun shouldShowIcons(userId: UserId, mailboxItem: MailboxItem): Boolean {
        val currentLocation = getSelectedMailLabelId()
        val itemLocation = mailboxItem.exclusiveLocations

        // Should show when starred, all mail, almost all mail or custom label
        return shouldShowLocationIndicator(userId, currentLocation, itemLocation)
    }

    sealed interface Result {
        data object None : Result
        data class Icon(
            val icons: List<MailboxItemLocationUiModel>
        ) : Result
    }

    private companion object {

        const val ICON_COUNT_THRESHOLD = 3
    }
}
