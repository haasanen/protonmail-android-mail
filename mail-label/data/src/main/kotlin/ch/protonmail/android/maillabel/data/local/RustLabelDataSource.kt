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

//    private var systemLabelsLiveQuery: MailLabelsLiveQueryByUserId? = null
//    private var messageLabelsLiveQuery: MailLabelsLiveQueryByUserId? = null
//    private var messageFoldersLiveQuery: MailLabelsLiveQueryByUserId? = null

    override fun observeSystemLabels(userId: UserId): Flow<List<LocalLabel>> {
        Timber.v("rust-label: observeSystemLabels called")
//        if (systemLabelsLiveQueryNotInitialised(userId)) {
//            initSystemLabelsLiveQuery(userId)
//        }

        return systemLabelsFlow
    }

    override fun observeMessageLabels(userId: UserId): Flow<List<LocalLabel>> {
        Timber.v("rust-label: observeMessageLabels called")
//        if (messageLabelsLiveQueryNotInitialised(userId)) {
//            initMessageLabelsLiveQuery(userId)
//        }

        return messageLabelsFlow
    }

    override fun observeMessageFolders(userId: UserId): Flow<List<LocalLabel>> {
        Timber.v("rust-label: observeMessageFolders called")
//        if (messageFoldersLiveQueryNotInitialised(userId)) {
//            initMessageFoldersLiveQuery(userId)
//        }

        return messageFoldersFlow
    }

    // TODO: Rust should provide live queries
    /*
        private fun systemLabelsLiveQueryNotInitialised(userId: UserId) =
            systemLabelsLiveQuery == null || systemLabelsLiveQuery?.userId != userId

        private fun messageLabelsLiveQueryNotInitialised(userId: UserId) =
            messageLabelsLiveQuery == null || systemLabelsLiveQuery?.userId != userId

        private fun messageFoldersLiveQueryNotInitialised(userId: UserId) =
            messageFoldersLiveQuery == null || systemLabelsLiveQuery?.userId != userId

        private fun initMessageLabelsLiveQuery(userId: UserId) {
            coroutineScope.launch {
                Timber.v("rust-label: initializing message labels live query")

                val session = userSessionRepository.getUserSession(userId)
                if (session == null) {
                    Timber.e("rust-label: trying to load labels with a null session")
                    return@launch
                }

                val messageLabelsUpdatedCallback = object : MailboxLiveQueryUpdatedCallback {
                    override fun onUpdated() {
                        mutableMessageLabelsFlow.value = messageLabelsLiveQuery?.liveQuery?.value() ?: emptyList()
                        Timber.v("rust-label: message labels updated: ${mutableMessageLabelsFlow.value}")
                    }
                }

                messageLabelsLiveQuery?.let { destroyMessageLabelsLiveQuery() }
                messageLabelsLiveQuery = MailLabelsLiveQueryByUserId(
                    userId,
                    session.newLabelLabelsObservedQuery(messageLabelsUpdatedCallback)
                )

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

                val messageFoldersUpdatedCallback = object : MailboxLiveQueryUpdatedCallback {
                    override fun onUpdated() {
                        mutableMessageFoldersFlow.value = messageFoldersLiveQuery?.liveQuery?.value() ?: emptyList()
                        Timber.v("rust-label: message folders updated: ${mutableMessageFoldersFlow.value}")
                    }
                }

                messageFoldersLiveQuery?.let { destroyMessageFoldersLiveQuery() }
                messageFoldersLiveQuery = MailLabelsLiveQueryByUserId(
                    userId,
                    session.newFolderLabelsObservedQuery(messageFoldersUpdatedCallback)
                )

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

             val systemLabelsUpdatedCallback = object : MailboxLiveQueryUpdatedCallback {
                    override fun onUpdated() {
                        mutableSystemLabelsFlow.value = systemLabelsLiveQuery?.liveQuery?.value() ?: emptyList()
                        Timber.v("rust-label: system labels updated: ${mutableSystemLabelsFlow.value}")
                    }
                }

                systemLabelsLiveQuery?.let { destroySystemLabelsLiveQuery() }
                systemLabelsLiveQuery = MailLabelsLiveQueryByUserId(
                    userId,
                    session.newSystemLabelsObservedQuery(systemLabelsUpdatedCallback)
                )

                Timber.d("rust-label: created systemLabelsLiveQuery")
            }
        }

        private fun destroySystemLabelsLiveQuery() {
            Timber.v("rust-label: destroySystemLabelsLiveQuery")
            systemLabelsLiveQuery?.liveQuery?.disconnect()
            systemLabelsLiveQuery = null
        }

        private fun destroyMessageLabelsLiveQuery() {
            Timber.v("rust-label: label: destroyMessageLabelsLiveQuery")
            messageLabelsLiveQuery?.liveQuery?.disconnect()
            messageLabelsLiveQuery = null
        }

        private fun destroyMessageFoldersLiveQuery() {
            Timber.v("rust-label: label: destroyMessageFoldersLiveQuery")
            messageFoldersLiveQuery?.liveQuery?.disconnect()
            messageFoldersLiveQuery = null
        }

        private data class MailLabelsLiveQueryByUserId(
            val userId: UserId,
            val liveQuery: MailLabelsLiveQuery
        )*/
}
