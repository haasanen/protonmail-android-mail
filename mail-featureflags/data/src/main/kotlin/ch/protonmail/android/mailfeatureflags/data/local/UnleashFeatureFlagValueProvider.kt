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

package ch.protonmail.android.mailfeatureflags.data.local

import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagProviderPriority
import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagValueProvider
import ch.protonmail.android.mailfeatureflags.domain.annotation.FeatureFlagsCoroutineScope
import ch.protonmail.android.mailsession.data.repository.MailSessionRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnleashFeatureFlagValueProvider @Inject constructor(
    private val mailSessionRepository: MailSessionRepository,
    @FeatureFlagsCoroutineScope private val coroutineScope: CoroutineScope
) : FeatureFlagValueProvider {

    override val priority: Int = FeatureFlagProviderPriority.UnleashProvider

    override val name: String = "Unleash FF provider"

    override suspend fun getFeatureFlagValue(key: String): Boolean? = with(coroutineScope) {
        val mailSession = mailSessionRepository.getMailSession().getRustMailSession()
        return@with mailSession.isFeatureEnabled(key)
    }
}
