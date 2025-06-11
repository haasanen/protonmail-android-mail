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

package ch.protonmail.android.mailmessage.data.repository

import ch.protonmail.android.mailmessage.domain.model.PreviousScheduleSendTime
import ch.protonmail.android.mailmessage.domain.repository.PreviousScheduleSendTimeRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Caches the "schedule send" time of a message when the user cancels the scheduling
 * (which can happened through the "undo" snackbar right after scheduling or the "edit" banner in the message detail).
 * This time is then shown again in the "schedule send" bottom sheet in composer.
 */
@Singleton
class PreviousScheduleSendTimeInMemoryRepository @Inject constructor() : PreviousScheduleSendTimeRepository {

    private var previousTimeCache: PreviousScheduleSendTime? = null

    override suspend fun save(time: PreviousScheduleSendTime) {
        previousTimeCache = time
    }

    override suspend fun get(): PreviousScheduleSendTime? = previousTimeCache
}
