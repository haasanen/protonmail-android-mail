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

package ch.protonmail.android.maillabel.domain

import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailcommon.domain.usecase.ObservePrimaryUserId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.usecase.ObserveMailLabels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectedMailLabelId @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val observeMailLabels: ObserveMailLabels,
    observePrimaryUserId: ObservePrimaryUserId
) {

    /**
     * Before the flow is actually initialized,
     * we can't resolve which of the local labelIds is corresponding to the
     * remote "Inbox" one (id 0).
     * Since this class relies on a StateFlow which needs initialization, we init
     * with the tentative value of SystemLabelId.Inbox (0) and then update it
     * as soon as we have the definitive initial ID.
     */
    private val tentativeInitialLocation = MailLabelId.System(SystemLabelId.Inbox.labelId)

    private val mutableFlow = MutableStateFlow<MailLabelId>(tentativeInitialLocation)

    val flow: StateFlow<MailLabelId> = mutableFlow.asStateFlow()

    init {
        observePrimaryUserId()
            .filterNotNull()
            .onEach { userId ->
                getInitialLabelId(userId)?.let { set(it) }
            }
            .launchIn(appScope)
    }

    fun set(value: MailLabelId) {
        appScope.launch { mutableFlow.emit(value) }
    }

    private suspend fun getInitialLabelId(userId: UserId): MailLabelId.System? {
        return observeMailLabels(userId).firstOrNull()?.let { mailLabels ->
            mailLabels.system.firstOrNull {
                it.systemLabelId.labelId == SystemLabelId.Inbox.labelId
            }
        }?.id
    }


}
