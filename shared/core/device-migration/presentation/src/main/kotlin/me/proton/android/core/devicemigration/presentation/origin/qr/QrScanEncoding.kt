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

package me.proton.android.core.devicemigration.presentation.origin.qr

import java.nio.charset.Charset

internal sealed class QrScanEncoding<T : Any> {

    internal abstract val charset: Charset
    internal abstract fun decode(from: String): T

    data object Binary : QrScanEncoding<ByteArray>() {

        override val charset: Charset get() = Charsets.ISO_8859_1
        override fun decode(from: String): ByteArray = from.toByteArray(charset)
    }

    data object Utf8 : QrScanEncoding<String>() {

        override val charset: Charset get() = Charsets.UTF_8
        override fun decode(from: String): String = from
    }

    companion object {

        internal val default = Utf8
    }
}
