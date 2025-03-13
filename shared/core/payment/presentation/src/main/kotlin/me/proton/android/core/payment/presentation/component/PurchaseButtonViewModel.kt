/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.payment.presentation.component

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import me.proton.core.compose.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class PurchaseButtonViewModel @Inject constructor(
    private val purchaseProcessor: PurchaseButtonProcessor
) : BaseViewModel<PurchaseButtonAction, PurchaseButtonState>(
    initialAction = PurchaseButtonAction.Init,
    initialState = PurchaseButtonState.Idle
) {

    override fun onAction(action: PurchaseButtonAction): Flow<PurchaseButtonState> = purchaseProcessor.onAction(action)

    override suspend fun FlowCollector<PurchaseButtonState>.onError(throwable: Throwable) {
        emit(PurchaseButtonState.Error(throwable.message ?: "Unknown error"))
    }
}
