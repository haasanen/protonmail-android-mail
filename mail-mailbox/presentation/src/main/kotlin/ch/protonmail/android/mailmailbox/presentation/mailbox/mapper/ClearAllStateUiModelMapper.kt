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

package ch.protonmail.android.mailmailbox.presentation.mailbox.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailmailbox.domain.model.SpamOrTrash
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllStateUiModel

internal object ClearAllStateUiModelMapper {

    fun toUiModel(state: ClearAllState): ClearAllStateUiModel {
        return when (state) {
            is ClearAllState.ClearAllActionBanner -> ClearAllStateUiModel.Visible.ClearAllBannerWithButton(
                bannerText = TextUiModel(
                    value = if (state.isAutoDeleteEnabled) {
                        R.string.mailbox_action_auto_delete_on_clear_trash_spam_banner_text
                    } else {
                        R.string.mailbox_action_auto_delete_off_clear_trash_spam_banner_text
                    }
                ),
                buttonText = when (state.spamOrTrash) {
                    SpamOrTrash.Spam -> TextUiModel(R.string.mailbox_action_button_clear_spam)
                    SpamOrTrash.Trash -> TextUiModel(R.string.mailbox_action_button_clear_trash)
                },
                icon = R.drawable.ic_proton_trash_clock
            )
            is ClearAllState.Hidden -> ClearAllStateUiModel.Hidden
            is ClearAllState.UpsellBanner -> ClearAllStateUiModel.Visible.UpsellBanner(
                bannerText = TextUiModel(R.string.mailbox_action_clear_trash_spam_upsell_banner_text),
                upgradeButtonText = TextUiModel(R.string.mailbox_action_clear_trash_spam_upsell_cta),
                icon = R.drawable.ic_upsell_mail_plus
            )
        }
    }
}
