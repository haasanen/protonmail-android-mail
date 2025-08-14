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

package me.proton.android.core.account.data.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.proton.android.core.account.domain.model.CoreAccount
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.account.domain.usecase.ObserveCoreAccount
import me.proton.android.core.account.domain.usecase.ObserveCoreAccounts
import javax.inject.Inject

class ObserveCoreAccountImpl @Inject constructor(
    private val observeCoreAccounts: ObserveCoreAccounts
) : ObserveCoreAccount {

    override operator fun invoke(userId: CoreUserId): Flow<CoreAccount?> = observeCoreAccounts().map { list ->
        list.firstOrNull { it.userId.id == userId.id }
    }.distinctUntilChanged()
}
