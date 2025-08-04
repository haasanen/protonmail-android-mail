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

package ch.protonmail.android.mailupselling.domain.usecase

import arrow.core.flatMap
import arrow.core.right
import ch.protonmail.android.mailsession.domain.model.hasSubscription
import ch.protonmail.android.mailsession.domain.usecase.ObserveUser
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class IsPaidUser @Inject constructor(
    private val observeUser: ObserveUser
) {

    suspend operator fun invoke(userId: UserId) = observeUser(userId).first().flatMap { it ->
        it.hasSubscription().right()
    }
}
