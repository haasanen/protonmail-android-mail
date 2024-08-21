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

import ch.protonmail.android.mailcommon.domain.mapper.LocalLabel
import ch.protonmail.android.maillabel.data.MailLabelRustCoroutineScope
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LabelType
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.Sidebar
import uniffi.proton_mail_uniffi.WatchHandle
import javax.inject.Inject

class RustLabelDataSource @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    @MailLabelRustCoroutineScope private val coroutineScope: CoroutineScope
) : LabelDataSource {

    private val mutableSystemLabelsFlow = MutableStateFlow<List<LocalLabel>?>(null)
    private val systemLabelsFlow: Flow<List<LocalLabel>> = mutableSystemLabelsFlow
        .asStateFlow()
        .filterNotNull()

    private val mutableMessageLabelsFlow = MutableStateFlow<List<LocalLabel>?>(null)
    private val messageLabelsFlow: Flow<List<LocalLabel>> = mutableMessageLabelsFlow
        .asStateFlow()
        .filterNotNull()

    private val mutableMessageFoldersFlow = MutableStateFlow<List<LocalLabel>?>(null)
    private val messageFoldersFlow: Flow<List<LocalLabel>> = mutableMessageFoldersFlow
        .asStateFlow()
        .filterNotNull()

    private var systemLabelsWatchHandle: MailLabelsWatchHandleByUserId? = null
    private var messageLabelsWatchHandle: MailLabelsWatchHandleByUserId? = null
    private var messageFoldersWatchHandle: MailLabelsWatchHandleByUserId? = null

    override fun observeSystemLabels(userId: UserId): Flow<List<LocalLabel>> {
        Timber.v("rust-label: observeSystemLabels called")
        if (systemLabelsLiveQueryNotInitialised(userId)) {
            initSystemLabelsLiveQuery(userId)
        }

        return systemLabelsFlow
    }

    override fun observeMessageLabels(userId: UserId): Flow<List<LocalLabel>> {
        Timber.v("rust-label: observeMessageLabels called")
        if (messageLabelsLiveQueryNotInitialised(userId)) {
            initMessageLabelsLiveQuery(userId)
        }

        return messageLabelsFlow
    }

    override fun observeMessageFolders(userId: UserId): Flow<List<LocalLabel>> {
        Timber.v("rust-label: observeMessageFolders called")
        if (messageFoldersLiveQueryNotInitialised(userId)) {
            initMessageFoldersLiveQuery(userId)
        }

        return messageFoldersFlow
    }

    private fun systemLabelsLiveQueryNotInitialised(userId: UserId) =
        systemLabelsWatchHandle == null || systemLabelsWatchHandle?.userId != userId

    private fun messageLabelsLiveQueryNotInitialised(userId: UserId) =
        messageLabelsWatchHandle == null || systemLabelsWatchHandle?.userId != userId

    private fun messageFoldersLiveQueryNotInitialised(userId: UserId) =
        messageFoldersWatchHandle == null || systemLabelsWatchHandle?.userId != userId

    private fun initMessageLabelsLiveQuery(userId: UserId) {
        coroutineScope.launch {
            Timber.v("rust-label: initializing message labels live query")

            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-label: trying to load labels with a null session")
                return@launch
            }
            val sidebar = Sidebar(session)

            val messageLabelsUpdatedCallback = object : LiveQueryCallback {
                override fun onUpdate() {
                    coroutineScope.launch {
                        mutableMessageLabelsFlow.value = sidebar.customLabels()
                        Timber.v("rust-label: message labels updated: ${mutableMessageLabelsFlow.value}")
                    }
                }
            }

            messageLabelsWatchHandle?.let { destroyMessageLabelsLiveQuery() }
            messageLabelsWatchHandle = MailLabelsWatchHandleByUserId(
                userId,
                sidebar.watchLabels(LabelType.LABEL, messageLabelsUpdatedCallback)
            )

            Timber.v("rust-label: Setting initial value for labels: ${sidebar.customLabels()}")
            mutableMessageLabelsFlow.value = sidebar.customLabels()
            Timber.d("rust-label: created message labels live query")
        }
    }

    private fun initMessageFoldersLiveQuery(userId: UserId) {
        coroutineScope.launch {
            Timber.v("rust-label: initializing message folders live query")

            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-label: trying to load labels with a null session")
                return@launch
            }
            val sidebar = Sidebar(session)

            val messageFoldersUpdatedCallback = object : LiveQueryCallback {
                override fun onUpdate() {
                    coroutineScope.launch {
                        mutableMessageFoldersFlow.value = sidebar.customFolders(null)
                        Timber.v("rust-label: message folders updated: ${mutableMessageFoldersFlow.value}")
                    }
                }
            }

            messageFoldersWatchHandle?.let { destroyMessageFoldersLiveQuery() }
            messageFoldersWatchHandle = MailLabelsWatchHandleByUserId(
                userId,
                sidebar.watchLabels(LabelType.FOLDER, messageFoldersUpdatedCallback)
            )

            Timber.v("rust-label: Setting initial value for folders: ${sidebar.customFolders(null)}")
            mutableMessageFoldersFlow.value = sidebar.customFolders(null)
            Timber.d("rust-label: created message folders live query")
        }
    }

    private fun initSystemLabelsLiveQuery(userId: UserId) {
        coroutineScope.launch {
            Timber.v("rust-label: initializing system labels live query")

            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-label: trying to load labels with a null session")
                return@launch
            }
            val sidebar = Sidebar(session)

            val systemLabelsUpdatedCallback = object : LiveQueryCallback {
                override fun onUpdate() {
                    coroutineScope.launch {
                        mutableSystemLabelsFlow.value = sidebar.systemLabels()
                        Timber.v("rust-label: system labels updated: ${mutableSystemLabelsFlow.value}")
                    }
                }
            }
            systemLabelsWatchHandle?.let { destroySystemLabelsWatchHandle() }
            systemLabelsWatchHandle = MailLabelsWatchHandleByUserId(
                userId,
                sidebar.watchLabels(LabelType.SYSTEM, systemLabelsUpdatedCallback)
            )

            Timber.v("rust-label: Setting initial value for system folders ${sidebar.systemLabels()}")
            mutableSystemLabelsFlow.value = sidebar.systemLabels()
            Timber.d("rust-label: created systemLabelsLiveQuery")
        }
    }

    private fun destroySystemLabelsWatchHandle() {
        Timber.v("rust-label: destroySystemLabelsLiveQuery")
        systemLabelsWatchHandle?.watchHandle?.disconnect()
        systemLabelsWatchHandle = null
    }

    private fun destroyMessageLabelsLiveQuery() {
        Timber.v("rust-label: label: destroyMessageLabelsLiveQuery")
        messageLabelsWatchHandle?.watchHandle?.disconnect()
        messageLabelsWatchHandle = null
    }

    private fun destroyMessageFoldersLiveQuery() {
        Timber.v("rust-label: label: destroyMessageFoldersLiveQuery")
        messageFoldersWatchHandle?.watchHandle?.disconnect()
        messageFoldersWatchHandle = null
    }

    private data class MailLabelsWatchHandleByUserId(
        val userId: UserId,
        val watchHandle: WatchHandle
    )
}
