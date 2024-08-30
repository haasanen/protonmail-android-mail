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

package ch.protonmail.android.mailcommon.domain.usecase

import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import timber.log.Timber
import javax.inject.Inject

class UndoLastOperation @Inject constructor() {

    @MissingRustApi
    suspend operator fun invoke(): Either<Error, Unit> {
        Timber.w("Undo operation requested, but not yet implemented. (Need rust API)")
        return Error.UndoFailed.left()
    }

    sealed interface Error {
        data object NoOperationToUndo : Error
        data object UndoFailed : Error
    }
}
