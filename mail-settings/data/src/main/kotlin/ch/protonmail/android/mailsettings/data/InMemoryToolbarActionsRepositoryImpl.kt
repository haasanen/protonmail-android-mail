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

package ch.protonmail.android.mailsettings.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.Action
import ch.protonmail.android.mailsession.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.mailsettings.domain.model.ToolbarActionsPreference
import ch.protonmail.android.mailsettings.domain.model.ToolbarType
import ch.protonmail.android.mailsettings.domain.repository.InMemoryToolbarActionsRepository
import ch.protonmail.android.mailsettings.domain.repository.InMemoryToolbarActionsRepository.Error
import ch.protonmail.android.mailsettings.domain.repository.ToolbarActionsRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@ViewModelScoped
class InMemoryToolbarActionsRepositoryImpl @Inject constructor(
    private val observePrimaryUserId: ObservePrimaryUserId,
    private val toolbarActionsRepository: ToolbarActionsRepository
) : InMemoryToolbarActionsRepository {

    private val proposedPreferences = MutableStateFlow<ToolbarActionsPreference?>(null)

    override fun inMemoryPreferences(type: ToolbarType): Flow<Either<Error, ToolbarActionsPreference>> {
        return observePrimaryUserId().transformLatest { userId ->
            if (userId == null) {
                emit(Error.UserNotLoggedIn.left())
            } else {
                val actionsResult = toolbarActionsRepository.getToolbarActions(userId, type)
                actionsResult.fold(
                    ifLeft = {
                        emit(Error.InvalidActions.left())
                    },
                    ifRight = { actions ->
                        val allActions = toolbarActionsRepository.getAllActions(type)
                        val preference = ToolbarActionsPreference.create(actions, type, allActions)
                        proposedPreferences.update { preference }

                        emitAll(
                            proposedPreferences
                                .filterNotNull()
                                .map { it.right() }
                        )
                    }
                )
            }
        }
    }

    override fun toggleSelection(action: Action, toggled: Boolean) {
        proposedPreferences.update { pref ->
            pref?.update {
                it.toggleSelection(action, toggled)
            }
        }
    }

    override fun resetToDefault() {
        proposedPreferences.update { pref ->
            pref?.update {
                it.resetToDefault()
            }
        }
    }

    override fun reorder(fromIndex: Int, toIndex: Int) {
        proposedPreferences.update { pref ->
            pref?.update {
                it.reorder(fromIndex = fromIndex, toIndex = toIndex)
            }
        }
    }
}
