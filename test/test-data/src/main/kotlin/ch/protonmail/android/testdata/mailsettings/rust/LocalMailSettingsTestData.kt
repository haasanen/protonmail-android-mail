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

package ch.protonmail.android.testdata.mailsettings.rust

import uniffi.proton_api_mail.MailSettings
import uniffi.proton_api_mail.MailSettingsAlmostAllMail
import uniffi.proton_api_mail.MailSettingsComposerDirection
import uniffi.proton_api_mail.MailSettingsComposerMode
import uniffi.proton_api_mail.MailSettingsMessageButtons
import uniffi.proton_api_mail.MailSettingsNextMessageOnMove
import uniffi.proton_api_mail.MailSettingsPgpScheme
import uniffi.proton_api_mail.MailSettingsPmSignature
import uniffi.proton_api_mail.MailSettingsShowImages
import uniffi.proton_api_mail.MailSettingsShowMoved
import uniffi.proton_api_mail.MailSettingsSwipeAction
import uniffi.proton_api_mail.MailSettingsViewLayout
import uniffi.proton_api_mail.MailSettingsViewMode

object LocalMailSettingsTestData {

    val mailSettings = MailSettings(
        displayName = "displayName",
        signature = "signature",
        theme = "theme",
        autoSaveContacts = false,
        composerMode = MailSettingsComposerMode.MAXIMIZED,
        messageButtons = MailSettingsMessageButtons.READ_FIRST,
        showImages = MailSettingsShowImages.DO_NOT_AUTO_LOAD,
        showMoved = MailSettingsShowMoved.KEEP_BOTH,
        autoDeleteSpamAndTrashDays = 0.toUInt(),
        almostAllMail = MailSettingsAlmostAllMail.ALL_MAIL,
        nextMessageOnMove = MailSettingsNextMessageOnMove.DISABLED_EXPLICIT,
        viewMode = MailSettingsViewMode.CONVERSATIONS,
        viewLayout = MailSettingsViewLayout.COLUMN,
        swipeLeft = MailSettingsSwipeAction.TRASH,
        swipeRight = MailSettingsSwipeAction.ARCHIVE,
        shortcuts = false,
        pmSignature = MailSettingsPmSignature.ENABLED,
        pmSignatureReferralLink = false,
        imageProxy = 0.toUInt(),
        numMessagePerPage = 50.toUInt(),
        draftMimeType = "draftMimeType",
        receiveMimeType = "receiveMimeType",
        showMimeType = "showMimeType",
        enableFolderColor = false,
        inheritParentFolderColor = false,
        submissionAccess = false,
        rightToLeft = MailSettingsComposerDirection.RIGHT_TO_LEFT,
        attachPublicKey = false,
        sign = false,
        pgpScheme = MailSettingsPgpScheme.MIME,
        promptPin = false,
        stickyLabels = false,
        confirmLink = false,
        delaySendSeconds = 0.toUInt(),
        fontFace = "fontFace",
        spamAction = null,
        blockSenderConfirmation = false,
        mobileSettings = null,
        hideRemoteImages = false,
        hideSenderImages = false
    )

}
