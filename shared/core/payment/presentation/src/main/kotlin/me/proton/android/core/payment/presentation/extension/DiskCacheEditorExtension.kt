/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.payment.presentation.extension

import coil.annotation.ExperimentalCoilApi
import coil.disk.DiskCache

/**
 * Executes the given [block] function on this [DiskCache.Editor] and [DiskCache.Editor.commit] it,
 * or [DiskCache.Editor.abort] it when an exception is thrown.
 */
@OptIn(ExperimentalCoilApi::class)
@Suppress("TooGenericExceptionCaught", "SwallowedException")
suspend inline fun <R> DiskCache.Editor.use(crossinline block: suspend (DiskCache.Editor) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when {
            exception == null -> commit()
            else ->
                try {
                    abort()
                } catch (abortException: Throwable) {
                    // cause.addSuppressed(abortException) // ignored here
                }
        }
    }
}
