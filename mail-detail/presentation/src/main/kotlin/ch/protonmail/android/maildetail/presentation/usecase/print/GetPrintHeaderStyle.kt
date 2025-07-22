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

package ch.protonmail.android.maildetail.presentation.usecase.print

import android.content.Context
import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.maildetail.presentation.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class GetPrintHeaderStyle @Inject constructor(
    @ApplicationContext private val context: Context
) {

    operator fun invoke(): Either<GetPrintHeaderStyleError, String> = either {
        runCatching {
            context.resources.openRawResource(R.raw.print_header_props)
                .bufferedReader()
                .use { it.readText() }
        }.getOrElse {
            Timber.e(it, "Unable to read print_header_props file.")
            raise(GetPrintHeaderStyleError)
        }
    }
}

data object GetPrintHeaderStyleError
