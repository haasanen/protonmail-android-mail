/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.maildetail.presentation.mapper

import ch.protonmail.android.mailcommon.domain.model.ConversationCursorError
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.domain.model.Cursor
import ch.protonmail.android.mailcommon.domain.model.CursorId
import ch.protonmail.android.mailcommon.domain.model.End
import ch.protonmail.android.mailcommon.domain.model.Error
import ch.protonmail.android.maildetail.presentation.model.Page
import org.junit.Assert
import kotlin.test.Test

class PagedConversationMapperTest {

    @Test
    fun `when cursor result is null then return loading`() {
        Assert.assertEquals(Page.Loading, null.toPage())
    }

    @Test
    fun `when cursor result is End then return End`() {
        Assert.assertEquals(Page.End, End.toPage())
    }

    @Test
    fun `when cursor result is Cursor then return Conversation`() {
        Assert.assertEquals(
            Page.Conversation(CursorId(ConversationId("1"), "2")),
            Cursor(ConversationId("1"), "2").toPage()
        )
    }

    @Test
    fun `when cursor result is Error then return Error`() {
        Assert.assertEquals(
            Page.Error,
            Error(ConversationCursorError.Offline).toPage()
        )
    }
}
