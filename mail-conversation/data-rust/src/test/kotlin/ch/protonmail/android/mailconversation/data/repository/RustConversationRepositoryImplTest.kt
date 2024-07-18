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

package ch.protonmail.android.mailconversation.data.repository

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailconversation.data.local.RustConversationDataSource
import ch.protonmail.android.mailconversation.data.mapper.toConversation
import ch.protonmail.android.mailconversation.data.mapper.toConversationWithContext
import ch.protonmail.android.maillabel.data.local.RustLabelDataSource
import ch.protonmail.android.maillabel.data.usecase.FindLocalLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.mailpagination.domain.model.PageFilter
import ch.protonmail.android.mailpagination.domain.model.PageKey
import ch.protonmail.android.testdata.conversation.rust.LocalConversationIdSample
import ch.protonmail.android.testdata.conversation.rust.LocalConversationTestData
import ch.protonmail.android.testdata.label.rust.LocalLabelTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import kotlin.test.assertEquals

class RustConversationRepositoryImplTest {

    private val rustConversationDataSource: RustConversationDataSource = mockk()

    private val userId = UserId("test_user")
    // We are unable to mock FindLocalLabelId because of this issue: https://github.com/mockk/mockk/issues/544
    private val systemLabelId = SystemLabelId.Archive.labelId
    private val rustLabelDataSource: RustLabelDataSource = mockk {
        every { observeSystemLabels(userId) } returns flowOf(
            listOf(
                LocalLabelTestData.localSystemLabelWithCount.copy(
                    rid = systemLabelId.id
                )
            )
        )
    }
    private val findLocalLabelId: FindLocalLabelId = FindLocalLabelId(rustLabelDataSource)
    private val rustConversationRepository = RustConversationRepositoryImpl(
        rustConversationDataSource, findLocalLabelId
    )

    @Test
    fun `getLocalConversations should return conversations`() = runTest {
        // Given
        val pageFilter = PageFilter(labelId = systemLabelId, isSystemFolder = true)
        val pageKey = PageKey(filter = pageFilter)
        val localConversations = listOf(
            LocalConversationTestData.AugConversation, LocalConversationTestData.SepConversation
        )
        val expectedConversations = localConversations.map { it.toConversationWithContext(systemLabelId) }
        coEvery { rustConversationDataSource.getConversations(userId, any()) } returns localConversations

        // When
        val result = rustConversationRepository.getLocalConversations(userId, pageKey)

        // Then
        coVerify { rustConversationDataSource.getConversations(userId, any()) }
        assertEquals(expectedConversations, result)
    }

    @Test
    fun `observeConversation should return the conversation for the given id`() = runTest {
        // Given
        val conversationId = ConversationId(LocalConversationIdSample.AugConversation.toString())
        val localConversation = LocalConversationTestData.AugConversation
        val expected = localConversation.toConversation()
        coEvery { rustConversationDataSource.getConversation(userId, any()) } returns localConversation

        // When
        rustConversationRepository.observeConversation(userId, conversationId, refreshData = false).test {
            val result = awaitItem().getOrNull()

            // Then
            assertEquals(expected, result)
            coVerify { rustConversationDataSource.getConversation(userId, any()) }

            awaitComplete()
        }

    }

    @Test
    fun `observeConversation should return error when rust provides no conversation `() = runTest {
        // Given
        val conversationId = ConversationId(LocalConversationIdSample.AugConversation.toString())

        coEvery { rustConversationDataSource.getConversation(userId, any()) } returns null

        // When
        rustConversationRepository.observeConversation(userId, conversationId, refreshData = false).test {
            val result = awaitItem().getOrNull()

            // Then
            assertEquals(null, result)
            coVerify { rustConversationDataSource.getConversation(userId, any()) }

            awaitComplete()
        }
    }


    @Test
    fun `observing cached conversations should return conversations`() = runTest {
        // Given
        val conversationIds = listOf(ConversationId(LocalConversationIdSample.AugConversation.toString()))
        val localConversations = listOf(
            LocalConversationTestData.AugConversation, LocalConversationTestData.SepConversation
        )
        val conversations = localConversations.map { it.toConversation() }

        coEvery { rustConversationDataSource.observeConversations(userId, any()) } returns flowOf(localConversations)

        // When
        rustConversationRepository.observeCachedConversations(userId, conversationIds).test {
            val result = awaitItem()

            // Then
            assertEquals(conversations, result)
            coVerify { rustConversationDataSource.observeConversations(userId, any()) }

            awaitComplete()
        }

    }
}
