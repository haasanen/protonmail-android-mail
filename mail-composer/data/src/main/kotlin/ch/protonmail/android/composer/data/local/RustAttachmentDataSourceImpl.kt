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

import android.net.Uri
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.composer.data.mapper.toAttachmentMetaDataWithState
import ch.protonmail.android.composer.data.wrapper.AttachmentsWrapper
import ch.protonmail.android.mailattachments.data.mapper.toLocalAttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentMetadataWithState
import ch.protonmail.android.mailcommon.data.mapper.toDataError
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailmessage.data.local.AttachmentFileStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import uniffi.proton_mail_uniffi.AsyncLiveQueryCallback
import uniffi.proton_mail_uniffi.AttachmentListAddResult
import uniffi.proton_mail_uniffi.AttachmentListAttachmentsResult
import uniffi.proton_mail_uniffi.AttachmentListRemoveResult
import uniffi.proton_mail_uniffi.AttachmentListWatcherResult
import uniffi.proton_mail_uniffi.DraftAttachmentWatcher
import javax.inject.Inject

class RustAttachmentDataSourceImpl @Inject constructor(
    private val rustDraftDataSource: RustDraftDataSource,
    private val attachmentFileStorage: AttachmentFileStorage
) : RustAttachmentDataSource {

    override suspend fun observeAttachments(): Flow<Either<DataError, List<AttachmentMetadataWithState>>> =
        callbackFlow {
            Timber.d("rust-draft-attachments: Starting attachment observation")

            val result = rustDraftDataSource.attachmentList()
            var watcher: DraftAttachmentWatcher? = null
            val updateCallback = object : AsyncLiveQueryCallback {
                override suspend fun onUpdate() {
                    Timber.d("rust-draft-attachments: Attachment list updated")
                    rustDraftDataSource.attachmentList().onLeft { error ->
                        send(error.left())
                    }.onRight { attachmentListWrapper ->
                        send(attachmentListWrapper.getAttachments())
                    }
                }
            }
            result.onLeft { error ->
                Timber.e("rust-draft-attachments: Failed to get attachment list: $error")
                send(error.left())
                close()
                return@callbackFlow

            }.onRight { attachmentListWrapper ->
                send(attachmentListWrapper.getAttachments())

                Timber.d("rust-draft-attachments: Got attachment list, creating watcher")
                when (val watcherResult = attachmentListWrapper.createWatcher(updateCallback)) {
                    is AttachmentListWatcherResult.Error -> {
                        Timber.e("rust-draft-attachments: Failed to create watcher: ${watcherResult.v1}")
                        send(watcherResult.v1.toDataError().left())
                        close()
                        return@callbackFlow
                    }

                    is AttachmentListWatcherResult.Ok -> {
                        Timber.d("rust-draft-attachments: Created attachment list watcher")
                        watcher = watcherResult.v1
                    }
                }
            }

            awaitClose {
                Timber.d("rust-draft-attachments: Closing watcher")
                watcher?.disconnect()
            }
        }

    override suspend fun addAttachment(fileUri: Uri): Either<DataError, Unit> {
        val listResult = rustDraftDataSource.attachmentList()

        return listResult.fold(
            ifLeft = { error ->
                Timber.e("rust-draft-attachments: Failed to get attachment list: $error")
                error.left()
            },
            ifRight = { attachmentListWrapper ->
                // Save attachment to local storage
                val fileInfo = attachmentFileStorage.saveAttachment(
                    attachmentListWrapper.attachmentUploadDirectory(),
                    fileUri
                ) ?: return DataError.Local.FailedToStoreFile.left()

                when (val addResult = attachmentListWrapper.addAttachment(fileInfo.path, fileInfo.name)) {
                    is AttachmentListAddResult.Ok -> {
                        Timber.d("rust-draft-attachments: Added attachment: ${fileInfo.path}")
                        Unit.right()
                    }

                    is AttachmentListAddResult.Error -> {
                        Timber.e("rust-draft-attachments: Failed to add attachment: ${addResult.v1}")
                        addResult.v1.toDataError().left()
                    }
                }
            }
        )
    }

    override suspend fun removeAttachment(attachmentId: AttachmentId): Either<DataError, Unit> {
        val listResult = rustDraftDataSource.attachmentList()

        return listResult.fold(
            ifLeft = { error ->
                Timber.e("rust-draft-attachments: Failed to get attachment list: $error")
                error.left()
            },
            ifRight = { attachmentListWrapper ->

                when (val removeResult = attachmentListWrapper.removeAttachment(attachmentId.toLocalAttachmentId())) {
                    is AttachmentListRemoveResult.Ok -> {
                        Timber.d("rust-draft-attachments: Removed attachment: $attachmentId")
                        Unit.right()
                    }

                    is AttachmentListRemoveResult.Error -> {
                        Timber.e("rust-draft-attachments: Failed to remove attachment: ${removeResult.v1}")
                        removeResult.v1.toDataError().left()
                    }
                }
            }
        )
    }

    private suspend fun AttachmentsWrapper.getAttachments(): Either<DataError, List<AttachmentMetadataWithState>> {
        return when (val result = this.attachments()) {
            is AttachmentListAttachmentsResult.Ok -> {
                val attachments = result.v1.map { it.toAttachmentMetaDataWithState() }
                attachments.right()
            }

            is AttachmentListAttachmentsResult.Error -> {
                result.v1.toDataError().left()
            }
        }
    }

}
