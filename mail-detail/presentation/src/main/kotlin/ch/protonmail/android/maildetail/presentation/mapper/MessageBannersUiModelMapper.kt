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
import ch.protonmail.android.maildetail.presentation.model.AutoDeleteBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.ExpirationBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.MessageBannersUiModel
import ch.protonmail.android.maildetail.presentation.model.ScheduleSendBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.SnoozeBannerUiModel
import ch.protonmail.android.maildetail.presentation.usecase.FormatScheduleSendTime
import ch.protonmail.android.mailmessage.domain.model.MessageBanner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MessageBannersUiModelMapper @Inject constructor(
    @ApplicationContext val context: Context,
    val formatScheduleSendTime: FormatScheduleSendTime
) {

    fun toUiModel(messageBanners: List<MessageBanner>) = MessageBannersUiModel(
        shouldShowPhishingBanner = messageBanners.contains(MessageBanner.PhishingAttempt),
        shouldShowSpamBanner = messageBanners.contains(MessageBanner.Spam),
        shouldShowBlockedSenderBanner = messageBanners.contains(MessageBanner.BlockedSender),
        expirationBannerUiModel = toExpirationBannerUiModel(messageBanners),
        autoDeleteBannerUiModel = toAutoDeleteBannerUiModel(messageBanners),
        scheduleSendBannerUiModel = toScheduleSendBannerUiModel(messageBanners),
        snoozeBannerUiModel = toSnoozeBannerUiModel(messageBanners)
    )

    private fun toExpirationBannerUiModel(messageBanners: List<MessageBanner>): ExpirationBannerUiModel {
        return messageBanners.filterIsInstance<MessageBanner.Expiry>().firstOrNull()?.let {
            ExpirationBannerUiModel.Expiration(expiresAt = it.expiresAt)
        } ?: ExpirationBannerUiModel.NoExpiration
    }

    private fun toAutoDeleteBannerUiModel(messageBanners: List<MessageBanner>): AutoDeleteBannerUiModel {
        return messageBanners.filterIsInstance<MessageBanner.AutoDelete>().firstOrNull()?.let {
            AutoDeleteBannerUiModel.AutoDelete(deletesAt = it.deletesAt)
        } ?: AutoDeleteBannerUiModel.NoAutoDelete
    }

    private fun toScheduleSendBannerUiModel(messageBanners: List<MessageBanner>): ScheduleSendBannerUiModel {
        return messageBanners.filterIsInstance<MessageBanner.ScheduledSend>().firstOrNull()?.let {
            ScheduleSendBannerUiModel.SendScheduled(
                sendAt = formatScheduleSendTime(it.scheduledAt),
                isScheduleBeingCancelled = false
            )
        } ?: ScheduleSendBannerUiModel.NoScheduleSend
    }

    private fun toSnoozeBannerUiModel(messageBanners: List<MessageBanner>): SnoozeBannerUiModel {
        return messageBanners.filterIsInstance<MessageBanner.Snoozed>().firstOrNull()?.let {
            SnoozeBannerUiModel.SnoozeScheduled(
                snoozedUntil = formatScheduleSendTime(it.snoozedUntil)
            )
        } ?: SnoozeBannerUiModel.NotSnoozed
    }
}
