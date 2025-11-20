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

package ch.protonmail.android.mailcommon.presentation.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

/**
 * Represents a set of focusable fields such that:
 * - the focused field retains focus when the configuration change happens,
 * - the focused field is brought into the view only after the IME is visible.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <FocusedField> FocusableForm(
    fieldList: List<FocusedField>,
    initialFocus: FocusedField?,
    onFocusedField: (FocusedField) -> Unit = {},
    content: @Composable FocusableFormScope<FocusedField>.(Map<FocusedField, FocusRequester>) -> Unit
) {
    var focusedField by rememberSaveable(inputs = emptyArray()) { mutableStateOf(initialFocus) }

    // Build requester maps once per field list identity
    val focusRequesters = remember(fieldList) {
        fieldList.associateWith {
            FocusRequester()
        }
    }

    val onFieldFocused: (FocusedField) -> Unit = {
        focusedField = it
        onFocusedField(it)
    }

    FocusableFormScope(focusRequesters, onFieldFocused).content(focusRequesters)

    LaunchedEffect(Unit) {
        if (focusedField != initialFocus) {
            focusRequesters[focusedField]?.requestFocus()
        } else {
            focusRequesters[initialFocus]?.requestFocus()
        }
    }
}

class FocusableFormScope<FocusedField> @OptIn(ExperimentalFoundationApi::class) constructor(
    private val focusRequesters: Map<FocusedField, FocusRequester>,
    private val onFieldFocused: (focusedField: FocusedField) -> Unit
) {

    @OptIn(ExperimentalFoundationApi::class)
    @Stable
    fun Modifier.retainFieldFocusOnConfigurationChange(fieldType: FocusedField): Modifier {
        val focusRequester = focusRequesters[fieldType]
        return if (focusRequester != null) {
            focusRequester(focusRequester)
        } else {
            this
        }.onFocusChanged {
            if (it.hasFocus || it.isFocused) onFieldFocused(fieldType)
        }
    }
}
