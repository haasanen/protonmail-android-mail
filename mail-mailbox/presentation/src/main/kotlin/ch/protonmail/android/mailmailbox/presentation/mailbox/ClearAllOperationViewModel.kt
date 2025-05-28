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

package ch.protonmail.android.mailmailbox.presentation.mailbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.mailmailbox.domain.model.AutoDeleteState
import ch.protonmail.android.mailmailbox.domain.usecase.GetAutoDeleteBanner
import ch.protonmail.android.mailmailbox.presentation.mailbox.mapper.ClearAllStateUiModelMapper
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllStateUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
internal class ClearAllOperationViewModel @Inject constructor(
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val selectedMailLabelId: SelectedMailLabelId,
    private val getAutoDeleteBanner: GetAutoDeleteBanner
) : ViewModel() {

    val state: StateFlow<ClearAllStateUiModel> = observeClearAllState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClearAllStateUiModel.Hidden
    )

    private fun observeClearAllState(): Flow<ClearAllStateUiModel> = observePrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userId ->
            selectedMailLabelId.flow.map { mailLabelId ->
                Pair(userId, mailLabelId.labelId)
            }
        }
        .map { (userId, labelId) ->
            val clearState = getAutoDeleteBanner(userId, labelId).fold(
                ifLeft = { ClearAllState.Hidden },
                ifRight = {
                    when (it.state) {
                        AutoDeleteState.AutoDeleteUpsell -> ClearAllState.UpsellBanner
                        AutoDeleteState.AutoDeleteDisabled -> ClearAllState.ClearAllActionBanner(
                            isAutoDeleteEnabled = false,
                            spamOrTrash = it.folder
                        )
                        AutoDeleteState.AutoDeleteEnabled -> ClearAllState.ClearAllActionBanner(
                            isAutoDeleteEnabled = true,
                            spamOrTrash = it.folder
                        )
                    }
                }
            )
            ClearAllStateUiModelMapper.toUiModel(clearState)
        }
}
