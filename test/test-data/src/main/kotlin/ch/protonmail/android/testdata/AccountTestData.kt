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

package ch.protonmail.android.testdata

import ch.protonmail.android.mailsession.domain.model.Account
import ch.protonmail.android.mailsession.domain.model.AccountState
import ch.protonmail.android.testdata.user.UserIdTestData

object AccountTestData {
    private const val RAW_USERNAME = "username"
    private const val RAW_EMAIL = "email@protonmail.ch"

    val readyAccount = Account(
        userId = UserIdTestData.userId,
        state = AccountState.Ready,
        name = RAW_USERNAME,
        primaryAddress = RAW_EMAIL
    )

    val notReadyAccount = Account(
        userId = UserIdTestData.userId,
        state = AccountState.NotReady,
        name = RAW_USERNAME,
        primaryAddress = RAW_EMAIL
    )
}
