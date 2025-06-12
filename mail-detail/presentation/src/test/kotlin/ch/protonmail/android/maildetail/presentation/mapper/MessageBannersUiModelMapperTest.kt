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

package ch.protonmail.android.maildetail.presentation.mapper

import android.content.Context
import android.content.res.Resources
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.model.AutoDeleteBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.ExpirationBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.ScheduleSendBannerUiModel
import ch.protonmail.android.maildetail.presentation.usecase.FormatScheduleSendTime
import ch.protonmail.android.mailmessage.domain.model.MessageBanner
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class MessageBannersUiModelMapperTest {

    private val resourcesMock = mockk<Resources>()
    private val contextMock = mockk<Context> {
        every { resources } returns resourcesMock
    }
    private val formatScheduleSendTime = mockk<FormatScheduleSendTime>()

    private val messageBannersUiModelMapper = MessageBannersUiModelMapper(contextMock, formatScheduleSendTime)

    @Test
    fun `should map to ui model with a phishing banner when banners list contains it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            listOf(MessageBanner.PhishingAttempt)
        )

        // Then
        assertTrue(result.shouldShowPhishingBanner)
    }

    @Test
    fun `should map to ui model without a phishing banner when banners list does not contain it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            emptyList()
        )

        // Then
        assertFalse(result.shouldShowPhishingBanner)
    }

    @Test
    fun `should map to ui model with a spam banner when banners list contains it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            listOf(MessageBanner.Spam)
        )

        // Then
        assertTrue(result.shouldShowSpamBanner)
    }

    @Test
    fun `should map to ui model without a spam banner when banners list does not contain it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            emptyList()
        )

        // Then
        assertFalse(result.shouldShowSpamBanner)
    }

    @Test
    fun `should map to ui model with a blocked sender banner when banners list contains it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            listOf(MessageBanner.BlockedSender)
        )

        // Then
        assertTrue(result.shouldShowBlockedSenderBanner)
    }

    @Test
    fun `should map to ui model without a blocked sender banner when banners list does not contain it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            emptyList()
        )

        // Then
        assertFalse(result.shouldShowBlockedSenderBanner)
    }

    @Test
    fun `should map to ui model with expiration banner when banners list contains it`() {
        // Given
        every { resourcesMock.getQuantityString(any(), any(), any()) } returns "formatted duration"
        every { resourcesMock.getString(any(), any()) } returns "message expires in"

        // When
        val result = messageBannersUiModelMapper.toUiModel(
            listOf(MessageBanner.Expiry(Instant.DISTANT_FUTURE))
        )

        // Then
        assertEquals(ExpirationBannerUiModel.Expiration(Instant.DISTANT_FUTURE), result.expirationBannerUiModel)
    }

    @Test
    fun `should map to ui model with no expiration banner when banners list does not contain it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            emptyList()
        )

        // Then
        assertEquals(ExpirationBannerUiModel.NoExpiration, result.expirationBannerUiModel)
    }

    @Test
    fun `should map to ui model with auto-delete banner when banners list contains it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            listOf(MessageBanner.AutoDelete(Instant.DISTANT_FUTURE))
        )

        // Then
        assertEquals(
            AutoDeleteBannerUiModel.AutoDelete(Instant.DISTANT_FUTURE),
            result.autoDeleteBannerUiModel
        )
    }

    @Test
    fun `should map to ui model with no auto-delete banner when banners list does not contain it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            emptyList()
        )

        // Then
        assertEquals(AutoDeleteBannerUiModel.NoAutoDelete, result.autoDeleteBannerUiModel)
    }

    @Test
    fun `should map to ui model with schedule-send banner when banners list contains it`() {
        // Given
        val expected = TextUiModel.Text("in the far future")
        every { formatScheduleSendTime(Instant.DISTANT_FUTURE) } returns expected

        // When
        val result = messageBannersUiModelMapper.toUiModel(
            listOf(MessageBanner.ScheduledSend(Instant.DISTANT_FUTURE))
        )

        // Then
        assertEquals(
            ScheduleSendBannerUiModel.SendScheduled(expected, false),
            result.scheduleSendBannerUiModel
        )
    }

    @Test
    fun `should map to ui model with no schedule-send banner when banners list does not contain it`() {
        // When
        val result = messageBannersUiModelMapper.toUiModel(
            emptyList()
        )

        // Then
        assertEquals(ScheduleSendBannerUiModel.NoScheduleSend, result.scheduleSendBannerUiModel)
    }
}
