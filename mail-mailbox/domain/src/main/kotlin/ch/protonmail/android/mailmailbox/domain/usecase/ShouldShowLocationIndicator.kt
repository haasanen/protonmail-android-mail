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

import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.model.toDynamicSystemMailLabel
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@MissingRustApi
// This use case could be deleted if Rust provides the information for a given location
class ShouldShowLocationIndicator @Inject constructor(
    private val labelRepository: LabelRepository
) {

    suspend operator fun invoke(userId: UserId, mailLabelId: MailLabelId): Boolean {
        return when (mailLabelId) {
            is MailLabelId.Custom.Label -> true
            is MailLabelId.System -> shouldShowIconForSystemLabel(userId, mailLabelId, labelRepository)
            else -> false
        }
    }

    private suspend fun shouldShowIconForSystemLabel(
        userId: UserId,
        mailLabelId: MailLabelId,
        labelRepository: LabelRepository
    ): Boolean {
        val locationList = labelRepository.observeSystemLabels(userId).firstOrNull()?.filter {
            it.systemLabelId.labelId == SystemLabelId.AllMail.labelId ||
                it.systemLabelId.labelId == SystemLabelId.Starred.labelId
        }?.toDynamicSystemMailLabel()
            ?.map { it.id }
            ?: emptyList()

        return locationList.contains(mailLabelId)
    }

}
