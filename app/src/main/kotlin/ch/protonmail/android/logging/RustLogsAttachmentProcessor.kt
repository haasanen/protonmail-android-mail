/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.logging

import java.io.File
import java.io.RandomAccessFile
import ch.protonmail.android.mailbugreport.domain.LogsFileHandler
import ch.protonmail.android.mailbugreport.domain.annotations.RustLogsFileHandler
import io.sentry.Attachment
import io.sentry.EventProcessor
import io.sentry.Hint
import io.sentry.SentryEvent
import javax.inject.Inject

private const val MAX_BYTES = 256 * 1024

class RustLogsAttachmentProcessor @Inject constructor(
    @RustLogsFileHandler
    private val logsFileHandler: LogsFileHandler
) : EventProcessor {

    override fun process(event: SentryEvent, hint: Hint): SentryEvent {
        if (event.isCrashed) {
            getRustLogsAttachment()?.let { logs ->
                hint.addAttachment(logs)
            }
        }
        return event
    }

    private fun getRustLogsAttachment(maxBytes: Int = MAX_BYTES): Attachment? = runCatching {
        logsFileHandler.getLastLogFile()?.let { file ->
            val bytes = readFileTail(file, maxBytes)
            Attachment(bytes, file.name)
        }
    }.getOrNull()

    private fun readFileTail(file: File, maxBytes: Int): ByteArray = RandomAccessFile(file, "r").use { raf ->
        val fileLen = raf.length()
        val bytesToRead = minOf(fileLen, maxBytes.toLong())
        raf.seek(maxOf(fileLen - bytesToRead, 0))

        val bytes = ByteArray(bytesToRead.toInt())
        raf.readFully(bytes)
        bytes
    }
}
