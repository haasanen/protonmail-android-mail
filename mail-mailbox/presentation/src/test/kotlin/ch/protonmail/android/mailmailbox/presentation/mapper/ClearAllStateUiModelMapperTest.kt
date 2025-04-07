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
import ch.protonmail.android.maillabel.domain.model.MailLabel
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailmailbox.presentation.R
import ch.protonmail.android.mailmailbox.presentation.mailbox.mapper.ClearAllStateUiModelMapper
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllStateUiModel
import io.mockk.mockk
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class ClearAllStateUiModelMapperTest(
    @Suppress("unused") private val testName: String,
    private val state: ClearAllState,
    private val mailLabel: MailLabel,
    private val expectedState: ClearAllStateUiModel
) {

    @Test
    fun `maps to the correct state`() {
        val actual = ClearAllStateUiModelMapper.toUiModel(state, mailLabel)
        assertEquals(expectedState, actual)
    }

    companion object {

        private val upsellWithLinkModel = ClearAllStateUiModel.Visible.UpsellBannerWithLink(
            bannerText = TextUiModel(R.string.mailbox_action_clear_trash_spam_upsell_banner_text),
            linkText = TextUiModel(R.string.mailbox_action_clear_trash_spam_upsell_banner_link_title),
            icon = R.drawable.ic_upsell_mail_plus
        )

        private val inProgressModel = ClearAllStateUiModel.Visible.InfoBanner(
            TextUiModel(R.string.mailbox_action_clear_operation_scheduled)
        )

        private val actionWithTrash = ClearAllStateUiModel.Visible.ClearAllBannerWithButton(
            bannerText = TextUiModel(R.string.mailbox_action_clear_trash_spam_banner_text),
            buttonText = TextUiModel(R.string.mailbox_action_button_clear_trash),
            icon = R.drawable.ic_proton_trash_clock
        )

        private val actionWithSpam = ClearAllStateUiModel.Visible.ClearAllBannerWithButton(
            bannerText = TextUiModel(R.string.mailbox_action_clear_trash_spam_banner_text),
            buttonText = TextUiModel(R.string.mailbox_action_button_clear_spam),
            icon = R.drawable.ic_proton_trash_clock
        )
        private val actionWithUnsupported = ClearAllStateUiModel.Visible.ClearAllBannerWithButton(
            bannerText = TextUiModel(R.string.mailbox_action_clear_trash_spam_banner_text),
            buttonText = TextUiModel.Text(""),
            icon = R.drawable.ic_proton_trash_clock
        )

        private val trashMailLabel = MailLabel.System(
            id = MailLabelId.System(SystemLabelId.Trash.labelId),
            systemLabelId = SystemLabelId.Trash,
            order = 1
        )

        private val spamMailLabel = MailLabel.System(
            id = MailLabelId.System(SystemLabelId.Spam.labelId),
            systemLabelId = SystemLabelId.Spam,
            order = 1
        )
        private val inboxMailLabel = MailLabel.System(
            id = MailLabelId.System(SystemLabelId.Inbox.labelId),
            systemLabelId = SystemLabelId.Inbox,
            order = 1
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "hidden state maps to hidden ui model",
                ClearAllState.Hidden,
                mockk<MailLabel>(),
                ClearAllStateUiModel.Hidden
            ),
            arrayOf(
                "pending state maps to pending ui model",
                ClearAllState.ClearAllInProgress,
                mockk<MailLabel>(),
                inProgressModel
            ),
            arrayOf(
                "upsell state maps to upsell ui model",
                ClearAllState.UpsellBanner,
                mockk<MailLabel>(),
                upsellWithLinkModel
            ),
            arrayOf(
                "clearable state maps to clear button ui model (trash)",
                ClearAllState.ClearAllActionBanner,
                trashMailLabel,
                actionWithTrash
            ),
            arrayOf(
                "clearable state maps to clear button ui model (spam)",
                ClearAllState.ClearAllActionBanner,
                spamMailLabel,
                actionWithSpam
            ),
            arrayOf(
                "clearable state maps to clear button ui model (inbox)",
                ClearAllState.ClearAllActionBanner,
                inboxMailLabel,
                actionWithUnsupported
            ),
            arrayOf(
                "clearable state maps to clear button ui model (non system label)",
                ClearAllState.ClearAllActionBanner,
                mockk<MailLabel.Custom>(),
                actionWithUnsupported
            )
        )
    }
}
