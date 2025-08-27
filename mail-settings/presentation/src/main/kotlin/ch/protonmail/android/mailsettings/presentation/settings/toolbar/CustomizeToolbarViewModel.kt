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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsRefreshSignal
import ch.protonmail.android.mailsettings.domain.repository.MailSettingsRepository
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.mapper.CustomizeToolbarActionsUiMapper
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.CustomizeToolbarState
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.model.ToolbarActionsSet
import ch.protonmail.android.mailsettings.presentation.settings.toolbar.usecase.GetToolbarActions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import javax.inject.Inject

@HiltViewModel
internal class CustomizeToolbarViewModel @Inject constructor(
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val getToolbarActions: GetToolbarActions,
    private val mailSettingsRepository: MailSettingsRepository,
    private val mapper: CustomizeToolbarActionsUiMapper,
    private val refreshSignal: ToolbarActionsRefreshSignal
) : ViewModel() {

    val state: StateFlow<CustomizeToolbarState> = observePrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userId ->
            merge(
                flowOf(Unit),
                refreshSignal.refreshEvents
            ).flatMapLatest {
                flow {
                    val state = getToolbarActions(userId)
                        .flatMap { actions -> mapActionsToToolbarList(actions) }
                        .fold(
                            ifLeft = { CustomizeToolbarState.Error },
                            ifRight = { uiModels -> CustomizeToolbarState.Data(uiModels) }
                        )
                    emit(state)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
            initialValue = CustomizeToolbarState.Loading
        )

    private suspend fun mapActionsToToolbarList(
        actionsSet: ToolbarActionsSet
    ): Either<CustomizeToolbarState.Error, List<ToolbarActionsUiModel>> {

        val userId = observePrimaryUserId().first()
            ?: return CustomizeToolbarState.Error.left()

        val mailSettings = mailSettingsRepository.getMailSettings(userId)
        val viewMode = mailSettings.viewMode?.enum
            ?: return CustomizeToolbarState.Error.left()

        return mapper.mapToList(actionsSet, viewMode).right()
    }
}
