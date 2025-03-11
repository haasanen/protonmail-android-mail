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

package ch.protonmail.android.testdata.account

import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.model.AccountState

object AccountTestSample {

    val Primary = build(
        primaryAddress = "primary-email@pm.me",
        username = "name"
    )

    val PrimaryNotReady = Primary.copy(state = AccountState.NotReady)

    fun build(
        primaryAddress: String,
        username: String,
        state: AccountState = AccountState.Ready
    ) = Account(
        primaryAddress = primaryAddress,
        state = state,
        userId = UserIdSample.Primary,
        name = username
    )
}
