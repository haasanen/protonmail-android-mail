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

package ch.protonmail.android.maillabel.data.wrapper

import java.lang.ref.WeakReference
import uniffi.proton_mail_uniffi.LabelType
import uniffi.proton_mail_uniffi.LiveQueryCallback
import uniffi.proton_mail_uniffi.Sidebar
import uniffi.proton_mail_uniffi.SidebarCustomFolder
import uniffi.proton_mail_uniffi.SidebarCustomLabel
import uniffi.proton_mail_uniffi.SidebarSystemLabel
import uniffi.proton_mail_uniffi.WatchHandle

class SidebarWrapper(private val sidebar: Sidebar) {

    suspend fun watchLabels(system: LabelType, callback: LiveQueryCallback): WeakReference<WatchHandle> =
        WeakReference(sidebar.watchLabels(system, callback))

    suspend fun systemLabels(): List<SidebarSystemLabel> = sidebar.systemLabels()

    suspend fun customLabels(): List<SidebarCustomLabel> = sidebar.customLabels()

    suspend fun allCustomFolders(): List<SidebarCustomFolder> = sidebar.allCustomFolders()

    fun destroy() {
        sidebar.destroy()
    }
}
