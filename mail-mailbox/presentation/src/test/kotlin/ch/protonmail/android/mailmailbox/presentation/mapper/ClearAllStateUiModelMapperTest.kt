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

package ch.protonmail.android.mailmailbox.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailmailbox.domain.model.SpamOrTrash
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.mapper.ClearAllStateUiModelMapper
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllStateUiModel
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class ClearAllStateUiModelMapperTest(
    @Suppress("unused") private val testName: String,
    private val state: ClearAllState,
    private val expectedState: ClearAllStateUiModel
) {

    @Test
    fun `maps to the correct state`() {
        val actual = ClearAllStateUiModelMapper.toUiModel(state)
        assertEquals(expectedState, actual)
    }

    companion object {

        private val upsellWithLinkModel = ClearAllStateUiModel.Visible.UpsellBannerWithLink(
            bannerText = TextUiModel(R.string.mailbox_action_clear_trash_spam_upsell_banner_text),
            linkText = TextUiModel(R.string.mailbox_action_clear_trash_spam_upsell_banner_link_title),
            icon = R.drawable.ic_upsell_mail_plus
        )

        private val actionWithTrashAndAutoDeleteOn = ClearAllStateUiModel.Visible.ClearAllBannerWithButton(
            bannerText = TextUiModel(R.string.mailbox_action_auto_delete_on_clear_trash_spam_banner_text),
            buttonText = TextUiModel(R.string.mailbox_action_button_clear_trash),
            icon = R.drawable.ic_proton_trash_clock
        )

        private val actionWithSpamAndAutoDeleteOn = ClearAllStateUiModel.Visible.ClearAllBannerWithButton(
            bannerText = TextUiModel(R.string.mailbox_action_auto_delete_on_clear_trash_spam_banner_text),
            buttonText = TextUiModel(R.string.mailbox_action_button_clear_spam),
            icon = R.drawable.ic_proton_trash_clock
        )

        private val actionWithTrashAndAutoDeleteOff = ClearAllStateUiModel.Visible.ClearAllBannerWithButton(
            bannerText = TextUiModel(R.string.mailbox_action_auto_delete_off_clear_trash_spam_banner_text),
            buttonText = TextUiModel(R.string.mailbox_action_button_clear_trash),
            icon = R.drawable.ic_proton_trash_clock
        )

        private val actionWithSpamAndAutoDeleteOff = ClearAllStateUiModel.Visible.ClearAllBannerWithButton(
            bannerText = TextUiModel(R.string.mailbox_action_auto_delete_off_clear_trash_spam_banner_text),
            buttonText = TextUiModel(R.string.mailbox_action_button_clear_spam),
            icon = R.drawable.ic_proton_trash_clock
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "hidden state maps to hidden ui model",
                ClearAllState.Hidden,
                ClearAllStateUiModel.Hidden
            ),
            arrayOf(
                "upsell state maps to upsell ui model",
                ClearAllState.UpsellBanner,
                upsellWithLinkModel
            ),
            arrayOf(
                "clearable state maps to clear button ui model (trash)",
                ClearAllState.ClearAllActionBanner(isAutoDeleteEnabled = true, spamOrTrash = SpamOrTrash.Trash),
                actionWithTrashAndAutoDeleteOn
            ),
            arrayOf(
                "clearable state maps to clear button ui model (spam)",
                ClearAllState.ClearAllActionBanner(isAutoDeleteEnabled = true, spamOrTrash = SpamOrTrash.Spam),
                actionWithSpamAndAutoDeleteOn
            ),
            arrayOf(
                "clearable state maps to clear button ui model (trash)",
                ClearAllState.ClearAllActionBanner(isAutoDeleteEnabled = false, spamOrTrash = SpamOrTrash.Trash),
                actionWithTrashAndAutoDeleteOff
            ),
            arrayOf(
                "clearable state maps to clear button ui model (spam)",
                ClearAllState.ClearAllActionBanner(isAutoDeleteEnabled = false, spamOrTrash = SpamOrTrash.Spam),
                actionWithSpamAndAutoDeleteOff
            )
        )
    }
}
