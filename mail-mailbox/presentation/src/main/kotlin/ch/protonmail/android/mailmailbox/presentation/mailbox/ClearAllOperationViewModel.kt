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
import ch.protonmail.android.maillabel.domain.model.isTrashOrSpam
import ch.protonmail.android.maillabel.domain.usecase.ObserveMailLabels
import ch.protonmail.android.mailmailbox.presentation.mailbox.mapper.ClearAllStateUiModelMapper
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllState
import ch.protonmail.android.mailmailbox.presentation.mailbox.model.ClearAllStateUiModel
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.usecase.ObserveAutoDeleteSpamAndTrashEnabled
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@HiltViewModel
internal class ClearAllOperationViewModel @Inject constructor(
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val observeMailLabels: ObserveMailLabels,
    private val selectedMailLabelId: SelectedMailLabelId,
    private val observeAutoDeleteSpamAndTrashEnabled: ObserveAutoDeleteSpamAndTrashEnabled
) : ViewModel() {

    val state: StateFlow<ClearAllStateUiModel> = observeClearAllState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ClearAllStateUiModel.Hidden
    )

    private fun observeClearAllState(): Flow<ClearAllStateUiModel> = observePrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userId ->
            observeCurrentMailLabel(userId).map { mailLabel ->
                Pair(userId, mailLabel)
            }
        }
        .flatMapLatest { (userId, mailLabel) ->
            val eligibleLabel = mailLabel.takeIf { it?.isTrashOrSpam() == true }
                ?: return@flatMapLatest flowOf(ClearAllStateUiModel.Hidden)

            observeAutoDeleteSpamAndTrashEnabled(userId).map { isEnabled ->
                val clearState = if (isEnabled) ClearAllState.ClearAllActionBanner else ClearAllState.UpsellBanner
                ClearAllStateUiModelMapper.toUiModel(clearState, eligibleLabel)
            }
        }

    private fun observeCurrentMailLabel(userId: UserId) = combine(
        observeMailLabels(userId),
        selectedMailLabelId.flow
    ) { mailLabels, selectedLabel ->
        mailLabels.allById[selectedLabel]
    }
}
