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

package ch.protonmail.android.maildetail.presentation.model

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import kotlin.time.Instant

data class MessageBannersUiModel(
    val shouldShowPhishingBanner: Boolean,
    val shouldShowSpamBanner: Boolean,
    val shouldShowBlockedSenderBanner: Boolean,
    val expirationBannerUiModel: ExpirationBannerUiModel,
    val autoDeleteBannerUiModel: AutoDeleteBannerUiModel,
    val scheduleSendBannerUiModel: ScheduleSendBannerUiModel
)

sealed class ExpirationBannerUiModel {
    data object NoExpiration : ExpirationBannerUiModel()
    data class Expiration(
        val expiresAt: Instant
    ) : ExpirationBannerUiModel()
}

sealed class AutoDeleteBannerUiModel {
    data object NoAutoDelete : AutoDeleteBannerUiModel()
    data class AutoDelete(
        val deletesAt: Instant
    ) : AutoDeleteBannerUiModel()
}

sealed class ScheduleSendBannerUiModel {
    data object NoScheduleSend : ScheduleSendBannerUiModel()
    data class SendScheduled(
        val sendAt: TextUiModel,
        val isScheduleBeingCancelled: Boolean
    ) : ScheduleSendBannerUiModel()
}
