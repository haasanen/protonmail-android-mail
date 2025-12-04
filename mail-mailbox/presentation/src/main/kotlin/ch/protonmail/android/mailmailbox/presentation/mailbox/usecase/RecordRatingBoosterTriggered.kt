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

package ch.protonmail.android.mailmailbox.presentation.mailbox.usecase

import ch.protonmail.android.mailfeatureflags.domain.model.ShowRatingBoosterEnabled
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class RecordRatingBoosterTriggered @Inject constructor(
    val observePrimaryUserId: ObservePrimaryUserId,
    val userSessionRepository: UserSessionRepository
) {

    suspend operator fun invoke() {
        observePrimaryUserId().first()?.let { userId ->
            userSessionRepository.overrideFeatureFlag(
                userId, ShowRatingBoosterEnabled.key,
                false
            )
        } ?: run {
            Timber.e("RatingBooster:: Unable to set feature flag because there is no primary user")
        }
    }
}
