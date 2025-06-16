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

import ch.protonmail.android.mailsettings.data.local.MailSettingsDataSource
import ch.protonmail.android.mailsettings.data.mapper.MailSettingsMapper.toMailSettings
import ch.protonmail.android.mailsettings.domain.repository.MailSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
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
import ch.protonmail.android.maillabel.domain.model.ViewMode
import timber.log.Timber

@SuppressWarnings("NotImplementedDeclaration", "TooManyFunctions", "ComplexInterface")
class RustMailSettingsRepository(
    private val mailSettingsDataSource: MailSettingsDataSource
) : MailSettingsRepository {

    override fun getMailSettingsFlow(userId: UserId) =
        mailSettingsDataSource.observeMailSettings(userId).map { localSettings ->
            localSettings.toMailSettings()
        }.convertToDataResultFlow()

    @Throws(NoSuchElementException::class)
    override suspend fun getMailSettings(userId: UserId): MailSettings {
        return getMailSettingsFlow(userId).firstOrNull().successOrNull()
            ?: throw NoSuchElementException("No Mail Settings found")
    }

    override suspend fun updateMailSettings(mailSettings: MailSettings) {
        Timber.e("updateMailSettings function not implemented. Rust implementation should come.")
    }

    override suspend fun updateDisplayName(userId: UserId, displayName: String): MailSettings {
        Timber.e("updateDisplayName function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateSignature(userId: UserId, signature: String): MailSettings {
        Timber.e("updateSignature function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateAutoSaveContacts(userId: UserId, autoSaveContacts: Boolean): MailSettings {
        Timber.e("updateAutoSaveContacts function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateComposerMode(userId: UserId, composerMode: ComposerMode): MailSettings {
        Timber.e("updateComposerMode function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateMessageButtons(userId: UserId, messageButtons: MessageButtons): MailSettings {
        Timber.e("updateMessageButtons function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateShowImages(userId: UserId, showImage: ShowImage): MailSettings {
        Timber.e("updateShowImages function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateShowMoved(userId: UserId, showMoved: ShowMoved): MailSettings {
        Timber.e("updateShowMoved function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateViewMode(userId: UserId, viewMode: ViewMode): MailSettings {
        Timber.e("updateViewMode function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateViewLayout(userId: UserId, viewLayout: ViewLayout): MailSettings {
        Timber.e("updateViewLayout function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateSwipeLeft(userId: UserId, swipeAction: SwipeAction): MailSettings {
        Timber.e("updateSwipeLeft function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateSwipeRight(userId: UserId, swipeAction: SwipeAction): MailSettings {
        Timber.e("updateSwipeRight function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updatePMSignature(userId: UserId, pmSignature: PMSignature): MailSettings {
        Timber.e("updatePMSignature function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateDraftMimeType(userId: UserId, mimeType: MimeType): MailSettings {
        Timber.e("updateDraftMimeType function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateReceiveMimeType(userId: UserId, mimeType: MimeType): MailSettings {
        Timber.e("updateReceiveMimeType function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateShowMimeType(userId: UserId, mimeType: MimeType): MailSettings {
        Timber.e("updateShowMimeType function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateRightToLeft(userId: UserId, rightToLeft: Boolean): MailSettings {
        Timber.e("updateRightToLeft function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateAttachPublicKey(userId: UserId, attachPublicKey: Boolean): MailSettings {
        Timber.e("updateAttachPublicKey function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateSign(userId: UserId, sign: Boolean): MailSettings {
        Timber.e("updateSign function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updatePGPScheme(userId: UserId, packageType: PackageType): MailSettings {
        Timber.e("updatePGPScheme function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updatePromptPin(userId: UserId, promptPin: Boolean): MailSettings {
        Timber.e("updatePromptPin function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateStickyLabels(userId: UserId, stickyLabels: Boolean): MailSettings {
        Timber.e("updateStickyLabels function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateConfirmLink(userId: UserId, confirmLinks: Boolean): MailSettings {
        Timber.e("updateConfirmLink function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateInheritFolderColor(userId: UserId, inherit: Boolean): MailSettings {
        Timber.e("updateInheritFolderColor function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    override suspend fun updateEnableFolderColor(userId: UserId, enable: Boolean): MailSettings {
        Timber.e("updateEnableFolderColor function not implemented, rust implementation should come")
        throw NotImplementedError()
    }

    private fun Flow<MailSettings?>.convertToDataResultFlow(): Flow<DataResult<MailSettings>> {
        return this.map { settings ->
            if (settings != null) {
                DataResult.Success(ResponseSource.Remote, settings)
            } else {
                DataResult.Error.Local("No Mail Settings Found", null)
            }
        }
    }

    private fun DataResult<MailSettings>?.successOrNull() = when (this) {
        is DataResult.Success -> this.value
        else -> null
    }

}
