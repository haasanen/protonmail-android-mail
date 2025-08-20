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

package ch.protonmail.android.mailsettings.domain.usecase.scenario

import arrow.core.Either
import ch.protonmail.android.mailsession.domain.model.Percent
import ch.protonmail.android.mailsession.domain.model.Storage
import ch.protonmail.android.mailsession.domain.model.StorageUnit
import ch.protonmail.android.mailsession.domain.model.User
import ch.protonmail.android.mailsettings.domain.model.StorageQuota
import ch.protonmail.android.mailsettings.domain.model.StorageQuotaError
import ch.protonmail.android.testdata.user.UserTestData
import me.proton.core.domain.entity.UserId

internal enum class UserStorageQuotaScenario(
    val primaryUserId: UserId?,
    val userList: List<User> = emptyList(),
    val expectedStorageQuota: Either<StorageQuotaError, StorageQuota>
) {

    FAILED_TO_RETRIEVE_PRIMARY_USER_ID(
        primaryUserId = null,
        expectedStorageQuota = Either.Left(StorageQuotaError.FailedToRetrievePrimaryUserId)
    ),
    FAILED_TO_RETRIEVE_USER(
        primaryUserId = UserId("some-user-id"),
        userList = emptyList(),
        expectedStorageQuota = Either.Left(StorageQuotaError.FailedToRetrieveUser)
    ),
    ZERO_USAGE(
        primaryUserId = UserId("user-zero"),
        userList = listOf(
            UserTestData.build(
                userId = UserId("user-zero"),
                usedSpace = 0L,
                maxSpace = 1_099_511_627_776L
            )
        ),
        expectedStorageQuota = Either.Right(
            StorageQuota(
                usagePercent = Percent(0.0),
                maxStorage = Storage(1, StorageUnit.TiB),
                isAboveAlertThreshold = false
            )
        )
    ),
    BELOW_THRESHOLD(
        primaryUserId = UserId("user-below"),
        userList = listOf(
            UserTestData.build(
                userId = UserId("user-below"),
                usedSpace = 900_000_000L,
                maxSpace = 1_500_000_000L
            )
        ),
        expectedStorageQuota = Either.Right(
            StorageQuota(
                usagePercent = Percent(60.0),
                maxStorage = Storage(1, StorageUnit.GiB),
                isAboveAlertThreshold = false
            )
        )
    ),
    AT_THRESHOLD(
        primaryUserId = UserId("user-at-threshold"),
        userList = listOf(
            UserTestData.build(
                userId = UserId("user-at-threshold"),
                usedSpace = 1_200_000L,
                maxSpace = 1_500_000L
            )
        ),
        expectedStorageQuota = Either.Right(
            StorageQuota(
                usagePercent = Percent(80.0),
                maxStorage = Storage(1, StorageUnit.MiB),
                isAboveAlertThreshold = true
            )
        )
    ),
    ABOVE_THRESHOLD(
        primaryUserId = UserId("user-above"),
        userList = listOf(
            UserTestData.build(
                userId = UserId("user-above"),
                usedSpace = 1_350L,
                maxSpace = 1_500L
            )
        ),
        expectedStorageQuota = Either.Right(
            StorageQuota(
                usagePercent = Percent(90.0),
                maxStorage = Storage(1, StorageUnit.KiB),
                isAboveAlertThreshold = true
            )
        )
    ),
    MAX_STORAGE(
        primaryUserId = UserId("user-max"),
        userList = listOf(
            UserTestData.build(
                userId = UserId("user-max"),
                usedSpace = 1L,
                maxSpace = 1L
            )
        ),
        expectedStorageQuota = Either.Right(
            StorageQuota(
                usagePercent = Percent(100.0),
                maxStorage = Storage(1, StorageUnit.BYTES),
                isAboveAlertThreshold = true
            )
        )
    )
}
