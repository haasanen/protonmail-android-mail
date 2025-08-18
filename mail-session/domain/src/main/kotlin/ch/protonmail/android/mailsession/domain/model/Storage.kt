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

package ch.protonmail.android.mailsession.domain.model

import kotlin.math.roundToInt

data class Storage(
    val size: Int,
    val unit: StorageUnit
) {

    companion object {

        fun fromBytes(bytes: Long): Storage {
            val absBytes = bytes.toDouble()
            return when {
                absBytes >= BYTES_PER_TIB -> Storage((absBytes / BYTES_PER_TIB).roundToInt(), StorageUnit.TiB)
                absBytes >= BYTES_PER_GIB -> Storage((absBytes / BYTES_PER_GIB).roundToInt(), StorageUnit.GiB)
                absBytes >= BYTES_PER_MIB -> Storage((absBytes / BYTES_PER_MIB).roundToInt(), StorageUnit.MiB)
                absBytes >= BYTES_PER_KIB -> Storage((absBytes / BYTES_PER_KIB).roundToInt(), StorageUnit.KiB)
                else -> Storage(absBytes.roundToInt(), StorageUnit.BYTES)
            }
        }
    }
}

private const val BYTES_PER_KIB = 1024L
private const val BYTES_PER_MIB = BYTES_PER_KIB * 1024L
private const val BYTES_PER_GIB = BYTES_PER_MIB * 1024L
private const val BYTES_PER_TIB = BYTES_PER_GIB * 1024L
