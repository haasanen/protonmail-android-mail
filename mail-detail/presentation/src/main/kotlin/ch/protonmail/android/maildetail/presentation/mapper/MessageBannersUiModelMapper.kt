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
import ch.protonmail.android.maildetail.presentation.model.ExpirationBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.MessageBannersUiModel
import ch.protonmail.android.mailmessage.domain.model.MessageBanner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MessageBannersUiModelMapper @Inject constructor(@ApplicationContext val context: Context) {

    fun toUiModel(messageBanners: List<MessageBanner>) = MessageBannersUiModel(
        shouldShowPhishingBanner = messageBanners.contains(MessageBanner.PhishingAttempt),
        shouldShowSpamBanner = messageBanners.contains(MessageBanner.Spam),
        shouldShowBlockedSenderBanner = messageBanners.contains(MessageBanner.BlockedSender),
        expirationBannerUiModel = toExpirationBannerUiModel(messageBanners)
    )

    private fun toExpirationBannerUiModel(messageBanners: List<MessageBanner>): ExpirationBannerUiModel {
        return messageBanners.filterIsInstance<MessageBanner.Expiry>().firstOrNull()?.let {
            ExpirationBannerUiModel.Expiration(expiresAt = it.expiresAt)
        } ?: ExpirationBannerUiModel.NoExpiration
    }
}
