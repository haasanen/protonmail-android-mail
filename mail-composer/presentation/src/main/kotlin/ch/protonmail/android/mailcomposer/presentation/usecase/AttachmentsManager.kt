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

package ch.protonmail.android.mailcomposer.presentation.usecase

import android.content.Context
import android.net.Uri
import ch.protonmail.android.mailcomposer.domain.usecase.AddAttachment
import ch.protonmail.android.mailcomposer.domain.usecase.AddInlineAttachment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AttachmentsManager @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val addStandardAttachment: AddAttachment,
    private val addInlineAttachment: AddInlineAttachment
) {

    suspend fun addAttachment(fileUri: Uri) = when (fileUri.mimeType()) {
        "image/jpg",
        "image/webp",
        "image/jpeg",
        "image/gif",
        "image/apng",
        "image/png" -> addInlineAttachment(fileUri)

        else -> addStandardAttachment(fileUri)
    }

    private fun Uri.mimeType() = applicationContext.contentResolver.getType(this)
}
