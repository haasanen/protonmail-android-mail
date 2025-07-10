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

package me.proton.android.core.devicemigration.presentation.origin.codeinput

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.proton.android.core.devicemigration.presentation.R
import me.proton.android.core.devicemigration.presentation.origin.usecase.ForkSessionIntoTargetDevice
import me.proton.core.compose.effect.Effect
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.util.kotlin.coroutine.flowWithResultContext
import javax.inject.Inject

@HiltViewModel
internal class ManualCodeInputViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val forkSessionIntoTargetDevice: ForkSessionIntoTargetDevice
) : BaseViewModel<ManualCodeInputAction, ManualCodeInputStateHolder>(
    initialAction = ManualCodeInputAction.Load,
    initialState = ManualCodeInputStateHolder(state = ManualCodeInputState.Loading)
) {

    override fun onAction(action: ManualCodeInputAction): Flow<ManualCodeInputStateHolder> = when (action) {
        is ManualCodeInputAction.Load -> onLoad()
        is ManualCodeInputAction.Submit -> onSubmit(action)
    }

    override suspend fun FlowCollector<ManualCodeInputStateHolder>.onError(throwable: Throwable) {
        emit(
            ManualCodeInputStateHolder(
                effect = Effect.of(
                    ManualCodeInputEvent.ErrorMessage(
                        throwable.localizedMessage ?: context.getString(R.string.presentation_error_general)
                    )
                ),
                state = ManualCodeInputState.Idle
            )
        )
    }

    // ACTION HANDLERS

    private fun onLoad() = flow {
        emit(ManualCodeInputStateHolder(state = ManualCodeInputState.Idle))
    }

    private fun onSubmit(action: ManualCodeInputAction.Submit) = flow {
        if (action.code.isBlank()) {
            emit(ManualCodeInputStateHolder(state = ManualCodeInputState.Error.EmptyCode))
        } else {
            emitAll(submitCode(code = action.code))
        }
    }

    private fun submitCode(code: String) = flowWithResultContext {
        emit(ManualCodeInputStateHolder(state = ManualCodeInputState.Loading))

        when (val result = forkSessionIntoTargetDevice(qrCode = code)) {
            is ForkSessionIntoTargetDevice.Result.Error -> emit(
                ManualCodeInputStateHolder(
                    state = ManualCodeInputState.Error.InvalidCode(result.message)
                )
            )

            is ForkSessionIntoTargetDevice.Result.Success -> emit(
                ManualCodeInputStateHolder(
                    effect = Effect.of(ManualCodeInputEvent.Success),
                    state = ManualCodeInputState.SignedInSuccessfully
                )
            )
        }
    }
}
