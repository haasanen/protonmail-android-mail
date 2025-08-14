/*
 * Copyright (c) 2024 Proton Technologies AG
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
import kotlinx.coroutines.flow.map
import me.proton.android.core.account.domain.model.CoreAccount
import me.proton.android.core.account.domain.model.toCoreAccount
import me.proton.android.core.account.domain.usecase.ObserveCoreAccounts
import me.proton.android.core.account.domain.usecase.ObserveStoredAccounts
import javax.inject.Inject

class ObserveCoreAccountsImpl @Inject constructor(
    private val observeStoredAccounts: ObserveStoredAccounts
) : ObserveCoreAccounts {

    override fun invoke(): Flow<List<CoreAccount>> =
        observeStoredAccounts().map { list -> list.map { it.toCoreAccount() } }
}
