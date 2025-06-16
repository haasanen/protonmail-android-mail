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

package ch.protonmail.android.maillabel.domain.usecase

import app.cash.turbine.test
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.model.ViewMode
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
@Ignore
internal class ObserveCurrentViewModeTest(
    @Suppress("UNUSED_PARAMETER") testName: String,
    private val input: Params.Input,
    private val expected: ViewMode
) {

//    private val observeMailSettings: ObserveMailSettings = mockk {
//        every { this@mockk(UserIdTestData.userId) } returns
//            flowOf(buildMailSettings(isConversationSettingEnabled = input.isConversationSettingEnabled))
//    }
    private val observeMessageOnlyLabelIds = mockk<ObserveMessageOnlyLabelIds> {
        every { this@mockk.invoke(UserIdTestData.userId) } returns flowOf(
            listOf(
                SystemLabelId.Drafts,
                SystemLabelId.AllDrafts,
                SystemLabelId.Sent,
                SystemLabelId.AllSent
            ).map { it.labelId }
        )
    }
    private val observeCurrentViewMode = ObserveCurrentViewMode(observeMessageOnlyLabelIds)

    @Test
    fun test() = runTest {
        observeCurrentViewMode(UserIdTestData.userId, input.selectedMailLabelId.labelId).test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    data class Params(
        val testName: String,
        val input: Input,
        val expected: ViewMode
    ) {

        data class Input(
            val isConversationSettingEnabled: Boolean,
            val selectedMailLabelId: MailLabelId
        )
    }

    private companion object TestData {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = listOf(

            Params(
                testName = "conversation enabled in Inbox",
                input = Params.Input(
                    isConversationSettingEnabled = true,
                    MailLabelTestData.inboxSystemLabel.id
                ),
                expected = ViewMode.ConversationGrouping
            ),

            Params(
                testName = "conversation enabled in Drafts",
                input = Params.Input(
                    isConversationSettingEnabled = true,
                    MailLabelTestData.draftsSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation enabled in Sent",
                input = Params.Input(
                    isConversationSettingEnabled = true,
                    MailLabelTestData.sentSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation enabled in Starred",
                input = Params.Input(
                    isConversationSettingEnabled = true,
                    MailLabelTestData.starredSystemLabel.id
                ),
                expected = ViewMode.ConversationGrouping
            ),

            Params(
                testName = "conversation enabled in Archive",
                input = Params.Input(
                    isConversationSettingEnabled = true,
                    MailLabelTestData.archiveSystemLabel.id
                ),
                expected = ViewMode.ConversationGrouping
            ),

            Params(
                testName = "conversation enabled in Spam",
                input = Params.Input(
                    isConversationSettingEnabled = true,
                    MailLabelTestData.spamSystemLabel.id
                ),
                expected = ViewMode.ConversationGrouping
            ),

            Params(
                testName = "conversation enabled in Trash",
                input = Params.Input(
                    isConversationSettingEnabled = true,
                    MailLabelTestData.trashSystemLabel.id
                ),
                expected = ViewMode.ConversationGrouping
            ),

            Params(
                testName = "conversation enabled in AllMail",
                input = Params.Input(
                    isConversationSettingEnabled = true,
                    MailLabelTestData.allMailSystemLabel.id
                ),
                expected = ViewMode.ConversationGrouping
            ),

            Params(
                testName = "conversation enabled in CustomLabel",
                input = Params.Input(isConversationSettingEnabled = true, MailLabelId.Custom.Label(LabelId("0"))),
                expected = ViewMode.ConversationGrouping
            ),

            Params(
                testName = "conversation enabled in CustomFolder",
                input = Params.Input(isConversationSettingEnabled = true, MailLabelId.Custom.Folder(LabelId("0"))),
                expected = ViewMode.ConversationGrouping
            ),

            Params(
                testName = "conversation disabled in Inbox",
                input = Params.Input(
                    isConversationSettingEnabled = false,
                    MailLabelTestData.inboxSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation disabled in Drafts",
                input = Params.Input(
                    isConversationSettingEnabled = false,
                    MailLabelTestData.draftsSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation disabled in Sent",
                input = Params.Input(
                    isConversationSettingEnabled = false,
                    MailLabelTestData.sentSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation disabled in Starred",
                input = Params.Input(
                    isConversationSettingEnabled = false,
                    MailLabelTestData.starredSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation disabled in Archive",
                input = Params.Input(
                    isConversationSettingEnabled = false,
                    MailLabelTestData.archiveSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation disabled in Spam",
                input = Params.Input(
                    isConversationSettingEnabled = false,
                    MailLabelTestData.spamSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation disabled in Trash",
                input = Params.Input(
                    isConversationSettingEnabled = false,
                    MailLabelTestData.trashSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation disabled in AllMail",
                input = Params.Input(
                    isConversationSettingEnabled = false,
                    MailLabelTestData.allMailSystemLabel.id
                ),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation disabled in CustomLabel",
                input = Params.Input(isConversationSettingEnabled = false, MailLabelId.Custom.Label(LabelId("0"))),
                expected = ViewMode.NoConversationGrouping
            ),

            Params(
                testName = "conversation disabled in CustomFolder",
                input = Params.Input(isConversationSettingEnabled = false, MailLabelId.Custom.Folder(LabelId("0"))),
                expected = ViewMode.NoConversationGrouping
            )

        ).map { arrayOf(it.testName, it.input, it.expected) }
    }
}
