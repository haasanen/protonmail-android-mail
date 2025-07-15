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
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailcommon.presentation.mapper.ColorMapper
import ch.protonmail.android.maillabel.domain.model.ExclusiveLocation
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.GetSelectedMailLabelId
import ch.protonmail.android.mailmailbox.domain.usecase.ShouldShowLocationIndicator
import ch.protonmail.android.mailsettings.domain.model.FolderColorSettings
import ch.protonmail.android.testdata.mailbox.MailboxTestData.buildMailboxItem
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlinx.coroutines.test.runTest
import ch.protonmail.android.maillabel.presentation.R

class GetMailboxItemLocationIconTest {

    private val userId = UserIdSample.Primary
    private val getSelectedMailLabelId = mockk<GetSelectedMailLabelId>()
    private val shouldShowLocationIndicator = mockk<ShouldShowLocationIndicator>()
    private val colorMapper = mockk<ColorMapper>()

    private val getMailboxItemLocationIcon = GetMailboxItemLocationIcon(
        getSelectedMailLabelId = getSelectedMailLabelId,
        shouldShowLocationIndicator = shouldShowLocationIndicator,
        colorMapper = colorMapper
    )

    @Test
    fun `should return None when current location shouldn't show icons and not showing search results`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem()
        val folderColorSettings = FolderColorSettings(useFolderColor = true, inheritParentFolderColor = false)

        coEvery { getSelectedMailLabelId() } returns MailLabelId.System(SystemLabelId.Inbox.labelId)
        coEvery { shouldShowLocationIndicator(userId, any(), any()) } returns false

        // When
        val result = getMailboxItemLocationIcon(
            userId,
            mailboxItem, folderColorSettings, isShowingSearchResults = false
        )

        // Then
        assertTrue(result is GetMailboxItemLocationIcon.Result.None)
    }

    @Test
    fun `should return Icon with system label icon`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(
            exclusiveLocation = ExclusiveLocation.System(
                SystemLabelId.Inbox,
                LabelId("1")
            )
        )
        val folderColorSettings = FolderColorSettings(useFolderColor = false, inheritParentFolderColor = false)

        coEvery { getSelectedMailLabelId() } returns MailLabelId.System(SystemLabelId.AllMail.labelId)
        coEvery { shouldShowLocationIndicator(userId, any(), any()) } returns true

        // When
        val result = getMailboxItemLocationIcon(userId, mailboxItem, folderColorSettings, isShowingSearchResults = true)

        // Then
        assertTrue(result is GetMailboxItemLocationIcon.Result.Icon)
    }

    @Test
    fun `should return Icon with custom label and folder color`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(
            exclusiveLocation = ExclusiveLocation.Folder("Custom folder", LabelId("1"), "#FF5733")
        )
        val folderColorSettings = FolderColorSettings(useFolderColor = true, inheritParentFolderColor = false)

        every { colorMapper.toColor(any()) } returns Color(0xFFFF5733).right()
        coEvery { shouldShowLocationIndicator(userId, any(), any()) } returns true
        coEvery { getSelectedMailLabelId() } returns MailLabelId.System(SystemLabelId.AllMail.labelId)

        // When
        val result = getMailboxItemLocationIcon(userId, mailboxItem, folderColorSettings, isShowingSearchResults = true)

        // Then
        assertTrue(result is GetMailboxItemLocationIcon.Result.Icon)
        assertEquals(R.drawable.ic_proton_folder_filled, (result as GetMailboxItemLocationIcon.Result.Icon).icon.icon)
        assertEquals(Color(0xFFFF5733), result.icon.color)
    }

    @Test
    fun `should return Icon with custom label without folder color`() = runTest {
        // Given
        val mailboxItem = buildMailboxItem(
            exclusiveLocation = ExclusiveLocation.Folder("Custom folder", LabelId("1"), "#FF5733")
        )
        val folderColorSettings = FolderColorSettings(useFolderColor = false, inheritParentFolderColor = false)
        coEvery { shouldShowLocationIndicator(userId, any(), any()) } returns true
        coEvery { getSelectedMailLabelId() } returns MailLabelId.System(SystemLabelId.AllMail.labelId)

        // When
        val result = getMailboxItemLocationIcon(userId, mailboxItem, folderColorSettings, isShowingSearchResults = true)

        // Then
        assertTrue(result is GetMailboxItemLocationIcon.Result.Icon)
        assertEquals(R.drawable.ic_proton_folder, (result as GetMailboxItemLocationIcon.Result.Icon).icon.icon)
    }
}
