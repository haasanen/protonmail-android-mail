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

package ch.protonmail.android.maillabel.data.local

import ch.protonmail.android.maillabel.data.MailLabelRustCoroutineScope
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import timber.log.Timber
import uniffi.proton_mail_common.LocalLabelWithCount
import uniffi.proton_mail_uniffi.MailLabelsLiveQuery
import uniffi.proton_mail_uniffi.MailboxLiveQueryUpdatedCallback
import javax.inject.Inject

class RustLabelDataSource @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    @MailLabelRustCoroutineScope private val coroutineScope: CoroutineScope
) : LabelDataSource {

    private val mutableSystemLabelsFlow = MutableStateFlow<List<LocalLabelWithCount>?>(null)
    private val systemLabelsFlow: Flow<List<LocalLabelWithCount>> = mutableSystemLabelsFlow
        .asStateFlow()
        .filterNotNull()

    private var systemLabelsLiveQuery: MailLabelsLiveQuery? = null

    override fun observeSystemLabels(): Flow<List<LocalLabelWithCount>> {
        Timber.d("rustLib: observeSystemLabels")
        if (systemLabelsLiveQueryNotInitialised()) {
            initSystemLabelsLiveQuery()
        }

        return systemLabelsFlow
    }

    @SuppressWarnings("NotImplementedDeclaration")
    override fun observeMessageLabels(): Flow<List<LocalLabelWithCount>> {
        TODO("Not yet implemented")
    }

    @SuppressWarnings("NotImplementedDeclaration")
    override fun observeMessageFolders(): Flow<List<LocalLabelWithCount>> {
        TODO("Not yet implemented")
    }

    private fun systemLabelsLiveQueryNotInitialised() = systemLabelsLiveQuery == null

    private fun initSystemLabelsLiveQuery() {
        coroutineScope.launch {
            Timber.d("rustLib: systemlabel: initilizing system labels live query")

            val session = userSessionRepository.observeCurrentUserSession().firstOrNull()
            if (session == null) {
                Timber.e("rustLib: system labels: trying to load messages with a null session")
                return@launch
            }

            val systemLabelsUpdatedCallback = object : MailboxLiveQueryUpdatedCallback {
                override fun onUpdated() {
                    mutableSystemLabelsFlow.value = systemLabelsLiveQuery?.value() ?: emptyList()
                    Timber.d("rustLib: system labels updated: ${mutableSystemLabelsFlow.value}")
                }
            }

            systemLabelsLiveQuery?.let { destroySystemLabelsLiveQuery() }
            systemLabelsLiveQuery = session.newSystemLabelsObservedQuery(systemLabelsUpdatedCallback)

            Timber.d("rustLib: label: created systemLabelsLiveQuery")
        }
    }

    private fun destroySystemLabelsLiveQuery() {
        Timber.d("rustLib: label: destroySystemLabelsLiveQuery")
        systemLabelsLiveQuery?.disconnect()
        systemLabelsLiveQuery = null
    }

}
