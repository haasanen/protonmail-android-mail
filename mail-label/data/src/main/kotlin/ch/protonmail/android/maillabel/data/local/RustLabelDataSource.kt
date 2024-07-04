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

    private val mutableMessageLabelsFlow = MutableStateFlow<List<LocalLabelWithCount>?>(null)
    private val messageLabelsFlow: Flow<List<LocalLabelWithCount>> = mutableMessageLabelsFlow
        .asStateFlow()
        .filterNotNull()

    private val mutableMessageFoldersFlow = MutableStateFlow<List<LocalLabelWithCount>?>(null)
    private val messageFoldersFlow: Flow<List<LocalLabelWithCount>> = mutableMessageFoldersFlow
        .asStateFlow()
        .filterNotNull()

    private var systemLabelsLiveQuery: MailLabelsLiveQuery? = null
    private var messageLabelsLiveQuery: MailLabelsLiveQuery? = null
    private var messageFoldersLiveQuery: MailLabelsLiveQuery? = null


    override fun observeSystemLabels(): Flow<List<LocalLabelWithCount>> {
        Timber.d("rustLib: observeSystemLabels called")
        if (systemLabelsLiveQueryNotInitialised()) {
            initSystemLabelsLiveQuery()
        }

        return systemLabelsFlow
    }

    override fun observeMessageLabels(): Flow<List<LocalLabelWithCount>> {
        Timber.d("rustLib: observeMessageLabels called")
        if (messageLabelsLiveQueryNotInitialised()) {
            initMessageLabelsLiveQuery()
        }

        return messageLabelsFlow
    }

    override fun observeMessageFolders(): Flow<List<LocalLabelWithCount>> {
        Timber.d("rustLib: observeMessageFolders called")
        if (messageFoldersLiveQueryNotInitialised()) {
            initMessageFoldersLiveQuery()
        }

        return messageFoldersFlow
    }

    private fun systemLabelsLiveQueryNotInitialised() = systemLabelsLiveQuery == null
    private fun messageLabelsLiveQueryNotInitialised() = messageLabelsLiveQuery == null
    private fun messageFoldersLiveQueryNotInitialised() = messageFoldersLiveQuery == null

    private fun initMessageLabelsLiveQuery() {
        coroutineScope.launch {
            Timber.d("rustLib: message label: initilizing message labels live query")

            val session = userSessionRepository.observeCurrentUserSession().firstOrNull()
            if (session == null) {
                Timber.e("rustLib: message labels: trying to load labels with a null session")
                return@launch
            }

            val messageLabelsUpdatedCallback = object : MailboxLiveQueryUpdatedCallback {
                override fun onUpdated() {
                    mutableMessageLabelsFlow.value = messageLabelsLiveQuery?.value() ?: emptyList()
                    Timber.d("rustLib: message labels updated: ${mutableMessageLabelsFlow.value}")
                }
            }

            messageLabelsLiveQuery?.let { destroyMessageLabelsLiveQuery() }
            messageLabelsLiveQuery = session.newLabelLabelsObservedQuery(messageLabelsUpdatedCallback)

            Timber.d("rustLib: label: created message labels live query")
        }
    }

    private fun initMessageFoldersLiveQuery() {
        coroutineScope.launch {
            Timber.d("rustLib: message folders: initilizing message folders live query")

            val session = userSessionRepository.observeCurrentUserSession().firstOrNull()
            if (session == null) {
                Timber.e("rustLib: message folders: trying to load labels with a null session")
                return@launch
            }

            val messageFoldersUpdatedCallback = object : MailboxLiveQueryUpdatedCallback {
                override fun onUpdated() {
                    mutableMessageFoldersFlow.value = messageFoldersLiveQuery?.value() ?: emptyList()
                    Timber.d("rustLib: message folders updated: ${mutableMessageFoldersFlow.value}")
                }
            }

            messageFoldersLiveQuery?.let { destroyMessageFoldersLiveQuery() }
            messageFoldersLiveQuery = session.newFolderLabelsObservedQuery(messageFoldersUpdatedCallback)

            Timber.d("rustLib: label: created message folders live query")
        }
    }

    private fun initSystemLabelsLiveQuery() {
        coroutineScope.launch {
            Timber.d("rustLib: systemlabel: initilizing system labels live query")

            val session = userSessionRepository.observeCurrentUserSession().firstOrNull()
            if (session == null) {
                Timber.e("rustLib: system labels: trying to load labels with a null session")
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

    private fun destroyMessageLabelsLiveQuery() {
        Timber.d("rustLib: label: destroyMessageLabelsLiveQuery")
        messageLabelsLiveQuery?.disconnect()
        messageLabelsLiveQuery = null
    }

    private fun destroyMessageFoldersLiveQuery() {
        Timber.d("rustLib: label: destroyMessageFoldersLiveQuery")
        messageFoldersLiveQuery?.disconnect()
        messageFoldersLiveQuery = null
    }

}
