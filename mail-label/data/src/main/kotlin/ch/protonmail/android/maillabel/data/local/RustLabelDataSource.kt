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

import java.lang.ref.WeakReference
import arrow.core.Either
import arrow.core.left
import ch.protonmail.android.mailcommon.datarust.mapper.LocalLabelId
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.maillabel.data.MailLabelRustCoroutineScope
import ch.protonmail.android.maillabel.data.usecase.CreateRustSidebar
import ch.protonmail.android.maillabel.data.usecase.RustGetAllMailLabelId
import ch.protonmail.android.maillabel.data.wrapper.SidebarWrapper
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.domain.entity.UserId
import timber.log.Timber
import uniffi.proton_mail_uniffi.LabelType
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.SidebarCustomFolder
import uniffi.proton_mail_uniffi.SidebarCustomLabel
import uniffi.proton_mail_uniffi.SidebarSystemLabel
import uniffi.proton_mail_uniffi.WatchHandle
import javax.inject.Inject

class RustLabelDataSource @Inject constructor(
    private val userSessionRepository: UserSessionRepository,
    private val createRustSidebar: CreateRustSidebar,
    private val rustGetAllMailLabelId: RustGetAllMailLabelId,
    @MailLabelRustCoroutineScope private val coroutineScope: CoroutineScope
) : LabelDataSource {

    private val mutex = Mutex()

    private val mutableSystemLabelsFlow = MutableStateFlow<List<SidebarSystemLabel>?>(null)
    private val systemLabelsFlow: Flow<List<SidebarSystemLabel>> = mutableSystemLabelsFlow
        .asStateFlow()
        .filterNotNull()

    private val mutableMessageLabelsFlow = MutableStateFlow<List<SidebarCustomLabel>?>(null)
    private val messageLabelsFlow: Flow<List<SidebarCustomLabel>> = mutableMessageLabelsFlow
        .asStateFlow()
        .filterNotNull()

    private val mutableMessageFoldersFlow = MutableStateFlow<List<SidebarCustomFolder>?>(null)
    private val messageFoldersFlow: Flow<List<SidebarCustomFolder>> = mutableMessageFoldersFlow
        .asStateFlow()
        .filterNotNull()

    private var sidebarWithUserId: SidebarWithUserId? = null
    private var systemLabelsWatchHandle: WeakReference<WatchHandle>? = null
    private var messageLabelsWatchHandle: WeakReference<WatchHandle>? = null
    private var messageFoldersWatchHandle: WeakReference<WatchHandle>? = null

    private val customLabelsUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            coroutineScope.launch {
                mutableMessageLabelsFlow.value = sidebarWithUserId?.sidebar?.customLabels()?.getOrNull()
                Timber.v("rust-label: message labels updated: ${mutableMessageLabelsFlow.value}")
            }
        }
    }

    private val foldersUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            coroutineScope.launch {
                mutableMessageFoldersFlow.value = sidebarWithUserId?.sidebar?.allCustomFolders()?.getOrNull()
                Timber.v("rust-label: message folders updated: ${mutableMessageFoldersFlow.value}")
            }
        }
    }

    private val systemLabelsUpdatedCallback = object : LiveQueryCallback {
        override fun onUpdate() {
            coroutineScope.launch {
                mutableSystemLabelsFlow.value = sidebarWithUserId?.sidebar?.systemLabels()?.getOrNull()
                Timber.v("rust-label: system labels updated: ${mutableSystemLabelsFlow.value}")
            }
        }
    }

    override fun observeSystemLabels(userId: UserId): Flow<List<SidebarSystemLabel>> {
        Timber.v("rust-label: observeSystemLabels called")
        if (shouldInitSystemLabelWatcher(userId)) {
            initSystemLabelsWatcher(userId)
        }

        return systemLabelsFlow
    }

    override fun observeMessageLabels(userId: UserId): Flow<List<SidebarCustomLabel>> {
        Timber.v("rust-label: observeMessageLabels called")
        if (shouldInitMessageLabelsWatcher(userId)) {
            initMessageLabelsWatcher(userId)
        }

        return messageLabelsFlow
    }

    override fun observeMessageFolders(userId: UserId): Flow<List<SidebarCustomFolder>> {
        Timber.v("rust-label: observeMessageFolders called")
        if (shouldInitMessageFoldersWatcher(userId)) {
            initMessageFoldersWatcher(userId)
        }

        return messageFoldersFlow
    }

    override suspend fun getAllMailLabelId(userId: UserId): Either<DataError, LocalLabelId> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-label: trying to get all mail label id with null session.")
            return DataError.Local.NoDataCached.left()
        }
        return rustGetAllMailLabelId(session)
    }

    private fun shouldInitSystemLabelWatcher(userId: UserId) =
        systemLabelsWatchHandle == null || sidebarWithUserId?.userId != userId

    private fun shouldInitMessageLabelsWatcher(userId: UserId) =
        messageLabelsWatchHandle == null || sidebarWithUserId?.userId != userId

    private fun shouldInitMessageFoldersWatcher(userId: UserId) =
        messageFoldersWatchHandle == null || sidebarWithUserId?.userId != userId

    private fun initMessageLabelsWatcher(userId: UserId) {
        coroutineScope.launch {
            Timber.v("rust-label: initializing message labels live query")

            val sidebar = getRustSidebarInstance(userId) ?: return@launch

            sidebar.watchLabels(LabelType.LABEL, customLabelsUpdatedCallback)
                .onLeft { Timber.e("rust-label: failed to watch labels! $it") }
                .onRight { messageLabelsWatchHandle = WeakReference(it) }

            Timber.v("rust-label: Setting initial value for labels: ${sidebar.customLabels()}")
            mutableMessageLabelsFlow.value = sidebar.customLabels().getOrNull()
            Timber.d("rust-label: created message labels live query")
        }
    }

    private fun initMessageFoldersWatcher(userId: UserId) {
        coroutineScope.launch {
            Timber.v("rust-label: initializing message folders live query")

            val sidebar = getRustSidebarInstance(userId) ?: return@launch

            sidebar.watchLabels(LabelType.FOLDER, foldersUpdatedCallback)
                .onLeft { Timber.e("rust-label: failed to watch folders! $it") }
                .onRight { messageFoldersWatchHandle = WeakReference(it) }

            Timber.v("rust-label: Setting initial value for folders: ${sidebar.allCustomFolders()}")
            mutableMessageFoldersFlow.value = sidebar.allCustomFolders().getOrNull()
            Timber.d("rust-label: created message folders live query")
        }
    }

    private fun initSystemLabelsWatcher(userId: UserId) {
        coroutineScope.launch {
            Timber.v("rust-label: initializing system labels live query")

            val sidebar = getRustSidebarInstance(userId) ?: return@launch

            sidebar.watchLabels(LabelType.SYSTEM, systemLabelsUpdatedCallback)
                .onLeft { Timber.e("rust-label: failed to watch system labels! $it") }
                .onRight { systemLabelsWatchHandle = WeakReference(it) }

            Timber.v("rust-label: Setting initial value for system folders ${sidebar.systemLabels()}")
            mutableSystemLabelsFlow.value = sidebar.systemLabels().getOrNull()
            Timber.d("rust-label: created systemLabelsLiveQuery")
        }
    }

    private suspend fun getRustSidebarInstance(userId: UserId): SidebarWrapper? = mutex.withLock {
        if (shouldInitialiseSidebar(userId)) {
            destroySidebarAndWatchers()

            val session = userSessionRepository.getUserSession(userId)
            if (session == null) {
                Timber.e("rust-label: trying to load labels with a null session")
                return null
            }
            sidebarWithUserId = createRustSidebar(session).withUserId(userId)
        }

        return sidebarWithUserId?.sidebar
    }

    private fun shouldInitialiseSidebar(userId: UserId) =
        sidebarWithUserId == null || sidebarWithUserId?.userId != userId

    private fun destroySidebarAndWatchers() {
        sidebarWithUserId?.sidebar?.destroy()
        sidebarWithUserId = null
        destroySystemLabelsWatcher()
        destroyMessageLabelsWatcher()
        destroyMessageFoldersWatcher()
    }

    private fun destroySystemLabelsWatcher() {
        Timber.v("rust-label: destroySystemLabelsLiveQuery")
        systemLabelsWatchHandle?.clear()
        systemLabelsWatchHandle = null
    }

    private fun destroyMessageLabelsWatcher() {
        Timber.v("rust-label: label: destroyMessageLabelsLiveQuery")
        messageLabelsWatchHandle?.clear()
        messageLabelsWatchHandle = null
    }

    private fun destroyMessageFoldersWatcher() {
        Timber.v("rust-label: label: destroyMessageFoldersLiveQuery")
        messageFoldersWatchHandle?.clear()
        messageFoldersWatchHandle = null
    }

    private fun SidebarWrapper.withUserId(userId: UserId) = SidebarWithUserId(userId, this)

    private data class SidebarWithUserId(
        val userId: UserId,
        val sidebar: SidebarWrapper
    )
}
