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

package ch.protonmail.android.mailmailbox.domain.usecase

import ch.protonmail.android.maillabel.domain.model.ExclusiveLocation
import ch.protonmail.android.maillabel.domain.model.LabelWithSystemLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class ShouldShowLocationIndicator @Inject constructor(
    private val labelRepository: LabelRepository
) {

    private val systemLabelsCache = mutableMapOf<UserId, List<LabelWithSystemLabelId>?>()

    suspend operator fun invoke(
        userId: UserId,
        mailLabelId: MailLabelId,
        exclusiveLocations: List<ExclusiveLocation>
    ): Boolean {
        return when (mailLabelId) {
            is MailLabelId.Custom.Label -> true
            is MailLabelId.System -> shouldShowIconForSystemLabel(userId, mailLabelId, exclusiveLocations)
            else -> false
        }
    }

    private suspend fun shouldShowIconForSystemLabel(
        userId: UserId,
        currentMailLabel: MailLabelId,
        exclusiveLocations: List<ExclusiveLocation>
    ): Boolean {
        val systemLabels = systemLabelsCache.getOrPut(userId) {
            labelRepository.observeSystemLabels(userId).firstOrNull()
        }
        val isItemScheduledForSend = isItemScheduledForSend(exclusiveLocations)
        val isCurrentLocationSent = isCurrentLocationSent(systemLabels, currentMailLabel)
        val itemIsScheduledAndLocationIsSent = isItemScheduledForSend && isCurrentLocationSent

        val isCurrentLocationAllMailOrStarred = isCurrentLocationAllMailOrStarred(systemLabels, currentMailLabel)

        return isCurrentLocationAllMailOrStarred || itemIsScheduledAndLocationIsSent
    }

    private fun isCurrentLocationAllMailOrStarred(
        systemLabels: List<LabelWithSystemLabelId>?,
        currentMailLabel: MailLabelId
    ) = systemLabels?.filter {
        it.systemLabelId == SystemLabelId.AllMail ||
            it.systemLabelId == SystemLabelId.AlmostAllMail ||
            it.systemLabelId == SystemLabelId.Starred
    }
        .orEmpty()
        .map { it.label.labelId }
        .contains(currentMailLabel.labelId)

    private fun isCurrentLocationSent(systemLabels: List<LabelWithSystemLabelId>?, currentMailLabel: MailLabelId) =
        systemLabels?.filter {
            it.systemLabelId == SystemLabelId.Sent ||
                it.systemLabelId == SystemLabelId.AllSent
        }
            .orEmpty()
            .map { it.label.labelId }
            .contains(currentMailLabel.labelId)

    private fun isItemScheduledForSend(exclusiveLocations: List<ExclusiveLocation>) =
        exclusiveLocations.any { location ->
            when (location) {
                is ExclusiveLocation.System -> location.systemLabelId == SystemLabelId.AllScheduled
                is ExclusiveLocation.Folder,
                ExclusiveLocation.NoLocation -> false
            }
        }
}
