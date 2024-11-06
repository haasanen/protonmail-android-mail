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

package me.proton.android.core.auth.presentation.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SignUpScreen(
        modifier = modifier,
        onSuccess = onSuccess,
        onErrorMessage = onErrorMessage,
        state = state
    )
}

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    state: SignUpState
) {
    LaunchedEffect(state) {
        when (state) {
            is SignUpState.Error -> onErrorMessage(state.message)
            is SignUpState.Success -> onSuccess()
            else -> Unit
        }
    }
    SignUpLoading(modifier = modifier)
}


