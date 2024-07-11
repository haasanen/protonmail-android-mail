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

package ch.protonmail.android.mailsettings.data.repository

import ch.protonmail.android.mailsettings.domain.repository.MailSettingsRepository
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.mailsettings.domain.entity.ComposerMode
import me.proton.core.mailsettings.domain.entity.MailSettings
import me.proton.core.mailsettings.domain.entity.MessageButtons
import me.proton.core.mailsettings.domain.entity.MimeType
import me.proton.core.mailsettings.domain.entity.PMSignature
import me.proton.core.mailsettings.domain.entity.PackageType
import me.proton.core.mailsettings.domain.entity.ShowImage
import me.proton.core.mailsettings.domain.entity.ShowMoved
import me.proton.core.mailsettings.domain.entity.SwipeAction
import me.proton.core.mailsettings.domain.entity.ViewLayout
import me.proton.core.mailsettings.domain.entity.ViewMode

@SuppressWarnings("NotImplementedDeclaration", "TooManyFunctions", "ComplexInterface")
class RustMailSettingsRepository : MailSettingsRepository {

    override fun getMailSettingsFlow(userId: UserId, refresh: Boolean): Flow<DataResult<MailSettings>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMailSettings(userId: UserId, refresh: Boolean): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateMailSettings(mailSettings: MailSettings) {
        TODO("Not yet implemented")
    }

    override suspend fun updateDisplayName(userId: UserId, displayName: String): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateSignature(userId: UserId, signature: String): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateAutoSaveContacts(userId: UserId, autoSaveContacts: Boolean): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateComposerMode(userId: UserId, composerMode: ComposerMode): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateMessageButtons(userId: UserId, messageButtons: MessageButtons): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateShowImages(userId: UserId, showImage: ShowImage): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateShowMoved(userId: UserId, showMoved: ShowMoved): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateViewMode(userId: UserId, viewMode: ViewMode): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateViewLayout(userId: UserId, viewLayout: ViewLayout): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateSwipeLeft(userId: UserId, swipeAction: SwipeAction): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateSwipeRight(userId: UserId, swipeAction: SwipeAction): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updatePMSignature(userId: UserId, pmSignature: PMSignature): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateDraftMimeType(userId: UserId, mimeType: MimeType): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateReceiveMimeType(userId: UserId, mimeType: MimeType): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateShowMimeType(userId: UserId, mimeType: MimeType): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateRightToLeft(userId: UserId, rightToLeft: Boolean): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateAttachPublicKey(userId: UserId, attachPublicKey: Boolean): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateSign(userId: UserId, sign: Boolean): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updatePGPScheme(userId: UserId, packageType: PackageType): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updatePromptPin(userId: UserId, promptPin: Boolean): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateStickyLabels(userId: UserId, stickyLabels: Boolean): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateConfirmLink(userId: UserId, confirmLinks: Boolean): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateInheritFolderColor(userId: UserId, inherit: Boolean): MailSettings {
        TODO("Not yet implemented")
    }

    override suspend fun updateEnableFolderColor(userId: UserId, enable: Boolean): MailSettings {
        TODO("Not yet implemented")
    }
}
