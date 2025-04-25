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

import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveCurrentMailLabel
import ch.protonmail.android.maillabel.domain.usecase.ObserveSystemMailLabels
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class ShouldShowRecipients @Inject constructor(
    private val observeCurrentMailLabel: ObserveCurrentMailLabel,
    private val observeSystemMailLabels: ObserveSystemMailLabels
) {

    suspend operator fun invoke(userId: UserId): Boolean {
        return when (val currentMailLabelId = observeCurrentMailLabel(userId).firstOrNull()?.id) {
            is MailLabelId.Custom.Label -> false
            is MailLabelId.System -> shouldShowRecipientsForSystemLabel(
                userId, currentMailLabelId, observeSystemMailLabels
            )
            else -> false
        }
    }

    private suspend fun shouldShowRecipientsForSystemLabel(
        userId: UserId,
        mailLabelId: MailLabelId,
        observeSystemMailLabels: ObserveSystemMailLabels
    ): Boolean {
        val locationList = observeSystemMailLabels(userId).firstOrNull()?.fold(
            ifLeft = { emptyList() },
            ifRight = { systemMailLabels ->
                systemMailLabels
                    .filter {
                        it.systemLabelId.labelId == SystemLabelId.Drafts.labelId ||
                            it.systemLabelId.labelId == SystemLabelId.AllDrafts.labelId ||
                            it.systemLabelId.labelId == SystemLabelId.Sent.labelId ||
                            it.systemLabelId.labelId == SystemLabelId.AllSent.labelId ||
                            it.systemLabelId.labelId == SystemLabelId.AllScheduled.labelId
                    }
                    .map { it.id }
            }
        )

        return locationList?.contains(mailLabelId) ?: false
    }
}
