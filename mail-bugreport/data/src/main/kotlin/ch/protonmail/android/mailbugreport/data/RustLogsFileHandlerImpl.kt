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

package ch.protonmail.android.mailbugreport.data

import java.io.File
import android.content.Context
import ch.protonmail.android.mailbugreport.domain.LogsFileHandler
import ch.protonmail.android.mailcommon.domain.coroutines.IODispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * This additional handler is required until we merge the Logs to a single file.
 */
class RustLogsFileHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher
) : LogsFileHandler, CoroutineScope {

    override val coroutineContext: CoroutineContext = coroutineDispatcher + SupervisorJob()

    private val logDir by lazy {
        File(context.cacheDir, LogsDirName)
            .apply { mkdirs() }
    }

    override fun getParentPath(): File = logDir
    override fun getLastLogFile(): File? = logDir.listFiles()?.firstOrNull { it.name == UniffiLogsName }
    override fun writeLog(message: String) = Unit
    override fun close() = Unit

    companion object {

        private const val LogsDirName = "rust_logs"
        private const val UniffiLogsName = "proton-mail-uniffi.log"
    }
}
