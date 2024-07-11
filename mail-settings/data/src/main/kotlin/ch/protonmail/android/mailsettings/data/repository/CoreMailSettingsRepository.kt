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
import me.proton.core.mailsettings.domain.repository.MailSettingsRepository as CoreLibsMailSettingsRepository

@Suppress("TooManyFunctions", "ComplexInterface")
class CoreMailSettingsRepository(
    private val coreLibsMailSettingsRepository: CoreLibsMailSettingsRepository
) : MailSettingsRepository {

    /**
     * Observe [MailSettings], by [userId].
     */
    override fun getMailSettingsFlow(userId: UserId, refresh: Boolean): Flow<DataResult<MailSettings>> =
        coreLibsMailSettingsRepository.getMailSettingsFlow(userId, refresh)

    /**
     * Get [MailSettings], by [userId].
     *
     */
    override suspend fun getMailSettings(userId: UserId, refresh: Boolean): MailSettings =
        coreLibsMailSettingsRepository.getMailSettings(userId, refresh)

    /**
     * Update [MailSettings], locally.
     *
     * Note: This function is usually used for Events handling.
     *
     * @throws IllegalArgumentException if corresponding user doesn't exist.
     */
    override suspend fun updateMailSettings(mailSettings: MailSettings) =
        coreLibsMailSettingsRepository.updateMailSettings(mailSettings)

    /**
     * Update [displayName] for [userId]
     */
    override suspend fun updateDisplayName(userId: UserId, displayName: String): MailSettings =
        coreLibsMailSettingsRepository.updateDisplayName(userId, displayName)

    /**
     * Update [signature] for [userId]
     */
    override suspend fun updateSignature(userId: UserId, signature: String): MailSettings =
        coreLibsMailSettingsRepository.updateSignature(userId, signature)

    /**
     * Update [autoSaveContacts] for [userId]
     */
    override suspend fun updateAutoSaveContacts(userId: UserId, autoSaveContacts: Boolean): MailSettings =
        coreLibsMailSettingsRepository.updateAutoSaveContacts(userId, autoSaveContacts)

    /**
     * Update [composerMode] for [userId]
     */
    override suspend fun updateComposerMode(userId: UserId, composerMode: ComposerMode): MailSettings =
        coreLibsMailSettingsRepository.updateComposerMode(userId, composerMode)

    /**
     * Update [messageButtons] for [userId]
     */
    override suspend fun updateMessageButtons(userId: UserId, messageButtons: MessageButtons): MailSettings =
        coreLibsMailSettingsRepository.updateMessageButtons(userId, messageButtons)

    /**
     * Update [showImage] for [userId]
     */
    override suspend fun updateShowImages(userId: UserId, showImage: ShowImage): MailSettings =
        coreLibsMailSettingsRepository.updateShowImages(userId, showImage)

    /**
     * Update [showMoved] for [userId]
     */
    override suspend fun updateShowMoved(userId: UserId, showMoved: ShowMoved): MailSettings =
        coreLibsMailSettingsRepository.updateShowMoved(userId, showMoved)

    /**
     * Update [viewMode] for [userId]
     */
    override suspend fun updateViewMode(userId: UserId, viewMode: ViewMode): MailSettings =
        coreLibsMailSettingsRepository.updateViewMode(userId, viewMode)

    /**
     * Update [viewLayout] for [userId]
     */
    override suspend fun updateViewLayout(userId: UserId, viewLayout: ViewLayout): MailSettings =
        coreLibsMailSettingsRepository.updateViewLayout(userId, viewLayout)

    /**
     * Update [swipeAction] for [userId]
     */
    override suspend fun updateSwipeLeft(userId: UserId, swipeAction: SwipeAction): MailSettings =
        coreLibsMailSettingsRepository.updateSwipeLeft(userId, swipeAction)

    /**
     * Update [swipeAction] for [userId]
     */
    override suspend fun updateSwipeRight(userId: UserId, swipeAction: SwipeAction): MailSettings =
        coreLibsMailSettingsRepository.updateSwipeRight(userId, swipeAction)

    /**
     * Update [pmSignature] for [userId]
     */
    override suspend fun updatePMSignature(userId: UserId, pmSignature: PMSignature): MailSettings =
        coreLibsMailSettingsRepository.updatePMSignature(userId, pmSignature)

    /**
     * Update [mimeType] for [userId]
     */
    override suspend fun updateDraftMimeType(userId: UserId, mimeType: MimeType): MailSettings =
        coreLibsMailSettingsRepository.updateDraftMimeType(userId, mimeType)

    /**
     * Update [mimeType] for [userId]
     */
    override suspend fun updateReceiveMimeType(userId: UserId, mimeType: MimeType): MailSettings =
        coreLibsMailSettingsRepository.updateReceiveMimeType(userId, mimeType)

    /**
     * Update [mimeType] for [userId]
     */
    override suspend fun updateShowMimeType(userId: UserId, mimeType: MimeType): MailSettings =
        coreLibsMailSettingsRepository.updateShowMimeType(userId, mimeType)

    /**
     * Update [rightToLeft] for [userId]
     */
    override suspend fun updateRightToLeft(userId: UserId, rightToLeft: Boolean): MailSettings =
        coreLibsMailSettingsRepository.updateRightToLeft(userId, rightToLeft)

    /**
     * Update [attachPublicKey] for [userId]
     */
    override suspend fun updateAttachPublicKey(userId: UserId, attachPublicKey: Boolean): MailSettings =
        coreLibsMailSettingsRepository.updateAttachPublicKey(userId, attachPublicKey)

    /**
     * Update [sign] for [userId]
     */
    override suspend fun updateSign(userId: UserId, sign: Boolean): MailSettings =
        coreLibsMailSettingsRepository.updateSign(userId, sign)

    /**
     * Update [packageType] for [userId]
     */
    override suspend fun updatePGPScheme(userId: UserId, packageType: PackageType): MailSettings =
        coreLibsMailSettingsRepository.updatePGPScheme(userId, packageType)

    /**
     * Update [promptPin] for [userId]
     */
    override suspend fun updatePromptPin(userId: UserId, promptPin: Boolean): MailSettings =
        coreLibsMailSettingsRepository.updatePromptPin(userId, promptPin)

    /**
     * Update [stickyLabels] for [userId]
     */
    override suspend fun updateStickyLabels(userId: UserId, stickyLabels: Boolean): MailSettings =
        coreLibsMailSettingsRepository.updateStickyLabels(userId, stickyLabels)

    /**
     * Update [confirmLinks] for [userId]
     */
    override suspend fun updateConfirmLink(userId: UserId, confirmLinks: Boolean): MailSettings =
        coreLibsMailSettingsRepository.updateConfirmLink(userId, confirmLinks)

    /**
     * Update [inherit] for [userId]
     */
    override suspend fun updateInheritFolderColor(userId: UserId, inherit: Boolean): MailSettings =
        coreLibsMailSettingsRepository.updateInheritFolderColor(userId, inherit)

    /**
     * Update [enable] for [userId]
     */
    override suspend fun updateEnableFolderColor(userId: UserId, enable: Boolean): MailSettings =
        coreLibsMailSettingsRepository.updateEnableFolderColor(userId, enable)
}
