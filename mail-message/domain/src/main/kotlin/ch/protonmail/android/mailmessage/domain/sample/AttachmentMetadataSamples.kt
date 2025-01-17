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

package ch.protonmail.android.mailmessage.domain.sample

import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.AttachmentMetadata
import ch.protonmail.android.mailmessage.domain.model.MimeTypeCategory

object AttachmentMetadataSamples {
    val Image = AttachmentMetadata(
        id = AttachmentId("1"),
        mimeTypeCategory = MimeTypeCategory.Image,
        name = "profile_picture.png",
        size = 2048L
    )

    val Audio = AttachmentMetadata(
        id = AttachmentId("2"),
        mimeTypeCategory = MimeTypeCategory.Audio,
        name = "song.mp3",
        size = 5_242_880L
    )

    val Video = AttachmentMetadata(
        id = AttachmentId("3"),
        mimeTypeCategory = MimeTypeCategory.Video,
        name = "vacation_video.mp4",
        size = 10_485_760L
    )

    val Pdf = AttachmentMetadata(
        id = AttachmentId("4"),
        mimeTypeCategory = MimeTypeCategory.Pdf,
        name = "ebook.pdf",
        size = 302_976L
    )

    val Zip = AttachmentMetadata(
        id = AttachmentId("5"),
        mimeTypeCategory = MimeTypeCategory.Compressed,
        name = "archive.zip",
        size = 10_240L
    )
}
