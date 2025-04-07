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
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllStateUiModel

internal object ClearAllStateUiModelMapper {

    fun toUiModel(state: ClearAllState, mailLabel: MailLabel): ClearAllStateUiModel {
        return when (state) {
            ClearAllState.ClearAllActionBanner -> ClearAllStateUiModel.Visible.ClearAllBannerWithButton(
                bannerText = TextUiModel(R.string.mailbox_action_clear_trash_spam_banner_text),
                buttonText = when (mailLabel) {
                    is MailLabel.System -> when (mailLabel.systemLabelId) {
                        SystemLabelId.Trash -> TextUiModel(R.string.mailbox_action_button_clear_trash)
                        SystemLabelId.Spam -> TextUiModel(R.string.mailbox_action_button_clear_spam)
                        else -> TextUiModel.Text("")
                    }

                    else -> TextUiModel.Text("")
                },
                icon = R.drawable.ic_proton_trash_clock
            )

            ClearAllState.ClearAllInProgress -> ClearAllStateUiModel.Visible.InfoBanner(
                TextUiModel(R.string.mailbox_action_clear_operation_scheduled)
            )

            ClearAllState.Hidden -> ClearAllStateUiModel.Hidden
            ClearAllState.UpsellBanner -> ClearAllStateUiModel.Visible.UpsellBannerWithLink(
                bannerText = TextUiModel(R.string.mailbox_action_clear_trash_spam_upsell_banner_text),
                linkText = TextUiModel(R.string.mailbox_action_clear_trash_spam_upsell_banner_link_title),
                icon = R.drawable.ic_upsell_mail_plus
            )
        }
    }
}
