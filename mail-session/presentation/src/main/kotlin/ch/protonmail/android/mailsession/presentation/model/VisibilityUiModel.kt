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

package ch.protonmail.android.mailsession.presentation.model

import ch.protonmail.android.mailsession.presentation.model.VisibilityUiModel.Hidden
import ch.protonmail.android.mailsession.presentation.model.VisibilityUiModel.Visible
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class VisibilityUiModel<out T> {
    data class Visible<out T>(val data: T) : VisibilityUiModel<T>()
    data object Hidden : VisibilityUiModel<Nothing>()
}

inline fun <reified T> VisibilityUiModel<T>.getOrNull(): T? = if (this.isVisible()) data else null

@OptIn(ExperimentalContracts::class)
inline fun <reified T> VisibilityUiModel<T>.isInvisible(): Boolean {
    contract {
        returns(false) implies (this@isInvisible is Visible<T>)
    }
    return this is Hidden
}

@OptIn(ExperimentalContracts::class)
inline fun <reified T> VisibilityUiModel<T>.isVisible(): Boolean {
    contract {
        returns(true) implies (this@isVisible is Visible<T>)
    }
    return this is Visible<T>
}