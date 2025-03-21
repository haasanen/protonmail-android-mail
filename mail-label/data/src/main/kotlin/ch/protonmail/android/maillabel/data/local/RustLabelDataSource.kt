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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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

    private suspend fun getRustSidebarInstance(userId: UserId): SidebarWrapper? {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-label: trying to load labels with a null session")
            return null
        }
        return createRustSidebar(session)
    }

    override fun observeSystemLabels(userId: UserId): Flow<List<SidebarSystemLabel>> = callbackFlow {
        Timber.v("rust-label: initializing system labels live query")

        val sidebar = getRustSidebarInstance(userId) ?: return@callbackFlow
        var labelsWatchHandle: WatchHandle? = null
        val labelsUpdatedCallback = object : LiveQueryCallback {
            override fun onUpdate() {
                coroutineScope.launch {
                    sidebar.systemLabels().getOrNull()?.let {
                        send(it)
                        Timber.v("rust-label: system labels updated: $it")
                    }
                }
            }
        }

        sidebar.watchLabels(LabelType.SYSTEM, labelsUpdatedCallback)
            .onLeft {
                close()
                Timber.e("rust-label: failed to watch system labels! $it")
            }
            .onRight { watcher ->
                labelsWatchHandle = watcher
                sidebar.systemLabels().getOrNull()?.let { systemLabels ->
                    send(systemLabels)
                    Timber.v("rust-label: Setting initial value for system folders $systemLabels")
                }
            }

        awaitClose {
            labelsWatchHandle?.disconnect()
            sidebar.destroy()
            Timber.d("rust-label: system labels watcher disconnected")
        }
    }

    override fun observeMessageLabels(userId: UserId): Flow<List<SidebarCustomLabel>> = callbackFlow {
        Timber.v("rust-label: initializing message labels live query")

        val sidebar = getRustSidebarInstance(userId) ?: return@callbackFlow
        var labelsWatchHandle: WatchHandle? = null
        val labelsUpdatedCallback = object : LiveQueryCallback {
            override fun onUpdate() {
                coroutineScope.launch {
                    sidebar.customLabels().getOrNull()?.let {
                        send(it)
                        Timber.v("rust-label: message labels updated: $it")
                    }
                }
            }
        }

        sidebar.watchLabels(LabelType.LABEL, labelsUpdatedCallback)
            .onLeft {
                close()
                Timber.e("rust-label: failed to watch message labels! $it")
            }
            .onRight { watcher ->
                labelsWatchHandle = watcher
                sidebar.customLabels().getOrNull()?.let { labels ->
                    send(labels)
                    Timber.v("rust-label: Setting initial value for message labels $labels")
                }
            }

        awaitClose {
            labelsWatchHandle?.disconnect()
            sidebar.destroy()
            Timber.d("rust-label: message labels watcher disconnected")
        }
    }

    override fun observeMessageFolders(userId: UserId): Flow<List<SidebarCustomFolder>> = callbackFlow {
        Timber.v("rust-label: initializing message folders live query")

        val sidebar = getRustSidebarInstance(userId) ?: return@callbackFlow
        var labelsWatchHandle: WatchHandle? = null
        val labelsUpdatedCallback = object : LiveQueryCallback {
            override fun onUpdate() {
                coroutineScope.launch {
                    sidebar.allCustomFolders().getOrNull()?.let {
                        send(it)
                        Timber.v("rust-label: message folders updated: $it")
                    }
                }
            }
        }

        sidebar.watchLabels(LabelType.FOLDER, labelsUpdatedCallback)
            .onLeft {
                close()
                Timber.e("rust-label: failed to watch message folders! $it")
            }
            .onRight { watcher ->
                labelsWatchHandle = watcher
                sidebar.allCustomFolders().getOrNull()?.let { folders ->
                    send(folders)
                    Timber.v("rust-label: Setting initial value for message folders $folders")
                }
            }

        awaitClose {
            labelsWatchHandle?.disconnect()
            sidebar.destroy()
            Timber.d("rust-label: message folders watcher disconnected")
        }
    }

    override suspend fun getAllMailLabelId(userId: UserId): Either<DataError, LocalLabelId> {
        val session = userSessionRepository.getUserSession(userId)
        if (session == null) {
            Timber.e("rust-label: trying to get all mail label id with null session.")
            return DataError.Local.NoDataCached.left()
        }
        return rustGetAllMailLabelId(session)
    }
}
