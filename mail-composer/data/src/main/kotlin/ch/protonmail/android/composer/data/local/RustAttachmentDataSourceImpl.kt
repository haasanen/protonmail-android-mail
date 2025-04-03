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

package ch.protonmail.android.composer.data.local

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.composer.data.mapper.toAttachmentMetaData
import ch.protonmail.android.composer.data.wrapper.AttachmentListWrapper
import ch.protonmail.android.mailcommon.datarust.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.domain.model.AttachmentMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import uniffi.proton_mail_uniffi.AsyncLiveQueryCallback
import uniffi.proton_mail_uniffi.AttachmentListAttachmentsResult
import uniffi.proton_mail_uniffi.AttachmentListWatcherResult
import javax.inject.Inject
import timber.log.Timber
import uniffi.proton_mail_uniffi.DraftAttachmentWatcher

class RustAttachmentDataSourceImpl @Inject constructor(
    private val rustDraftDataSource: RustDraftDataSource
) : RustAttachmentDataSource {

    private val attachmentListMutableStateFlow = MutableStateFlow<Either<DataError, List<AttachmentMetadata>>?>(null)
    private val attachmentListStateFlow = attachmentListMutableStateFlow
        .asStateFlow()
        .filterNotNull()

    private var attachmentListWatcher: DraftAttachmentWatcher? = null
    private val updateCallback = object : AsyncLiveQueryCallback {
        override suspend fun onUpdate() {
            Timber.d("rust-draft-attachments: Attachment list updated")
            rustDraftDataSource.attachmentList().onLeft { error ->
                attachmentListMutableStateFlow.value = error.left()
            }.onRight { attachmentListWrapper ->

                attachmentListMutableStateFlow.value = attachmentListWrapper.getAttachments()
            }
        }
    }

    override suspend fun observeAttachments(): Flow<Either<DataError, List<AttachmentMetadata>>> {
        initWatcher()
        return attachmentListStateFlow
    }

    private suspend fun initWatcher() {
        if (attachmentListWatcher != null) return

        val result = rustDraftDataSource.attachmentList()

        result.onLeft {
            Timber.e("rust-draft-attachments: Failed to get attachment list: $it")
        }.onRight { attachmentListWrapper ->
            attachmentListMutableStateFlow.value = attachmentListWrapper.getAttachments()

            Timber.d("rust-draft-attachments: Got attachment list, creating watcher")
            when (val watcherResult = attachmentListWrapper.createWatcher(updateCallback)) {
                is AttachmentListWatcherResult.Error -> {
                    Timber.e("rust-draft-attachments: Failed to create attachment list watcher: ${watcherResult.v1}")
                }

                is AttachmentListWatcherResult.Ok -> {
                    Timber.d("rust-draft-attachments: Created attachment list watcher")
                    attachmentListWatcher = watcherResult.v1
                }
            }
        }
    }

    private suspend fun AttachmentListWrapper.getAttachments(): Either<DataError, List<AttachmentMetadata>> {
        return when (val result = this.attachments()) {
            is AttachmentListAttachmentsResult.Ok -> {
                val attachments = result.v1.map { it.toAttachmentMetaData() }
                attachments.right()
            }

            is AttachmentListAttachmentsResult.Error -> {
                result.v1.toDataError().left()
            }
        }
    }

}
