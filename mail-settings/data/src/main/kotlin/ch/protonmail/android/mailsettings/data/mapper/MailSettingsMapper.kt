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

package ch.protonmail.android.mailsettings.data.mapper

import ch.protonmail.android.mailcommon.domain.model.FAKE_USER_ID
import me.proton.core.domain.type.IntEnum
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
import uniffi.proton_api_mail.MailSettingsComposerDirection
import uniffi.proton_api_mail.MailSettingsComposerMode
import uniffi.proton_api_mail.MailSettingsMessageButtons
import uniffi.proton_api_mail.MailSettingsPgpScheme
import uniffi.proton_api_mail.MailSettingsPmSignature
import uniffi.proton_api_mail.MailSettingsShowImages
import uniffi.proton_api_mail.MailSettingsShowMoved
import uniffi.proton_api_mail.MailSettingsSwipeAction
import uniffi.proton_api_mail.MailSettingsViewLayout
import uniffi.proton_api_mail.MailSettingsViewMode
import uniffi.proton_api_mail.MailSettings as RustMailSettings

object MailSettingsMapper {

    fun RustMailSettings.toMailSettings(): MailSettings {
        return MailSettings(
            userId = FAKE_USER_ID,
            displayName = displayName,
            signature = signature,
            autoSaveContacts = autoSaveContacts,
            composerMode = composerMode.toComposerMode(),
            messageButtons = messageButtons.toMessageButtons(),
            showImages = showImages.toShowImage(),
            showMoved = showMoved.toShowMoved(),
            viewMode = viewMode.toViewMode(),
            viewLayout = viewLayout.toViewLayout(),
            swipeLeft = swipeLeft.toSwipeAction(),
            swipeRight = swipeRight.toSwipeAction(),
            shortcuts = shortcuts,
            pmSignature = pmSignature.toPMSignature(),
            numMessagePerPage = numMessagePerPage.toInt(),
            draftMimeType = MimeType.enumOf(draftMimeType),
            receiveMimeType = MimeType.enumOf(receiveMimeType),
            showMimeType = MimeType.enumOf(showMimeType),
            enableFolderColor = enableFolderColor,
            inheritParentFolderColor = inheritParentFolderColor,
            rightToLeft = rightToLeft.toAndroidComposerDirection(),
            attachPublicKey = attachPublicKey,
            sign = sign,
            pgpScheme = pgpScheme.toPackageType(),
            promptPin = promptPin,
            stickyLabels = stickyLabels,
            confirmLink = confirmLink
        )
    }

    private fun MailSettingsComposerMode.toComposerMode(): IntEnum<ComposerMode>? =
        ComposerMode.enumOf(this.value.toInt())

    private fun MailSettingsMessageButtons.toMessageButtons(): IntEnum<MessageButtons>? =
        MessageButtons.enumOf(this.value.toInt())

    private fun MailSettingsShowImages.toShowImage(): IntEnum<ShowImage>? = ShowImage.enumOf(this.value.toInt())

    private fun MailSettingsShowMoved.toShowMoved(): IntEnum<ShowMoved>? = ShowMoved.enumOf(this.value.toInt())

    private fun MailSettingsViewMode.toViewMode(): IntEnum<ViewMode>? = ViewMode.enumOf(this.value.toInt())

    private fun MailSettingsViewLayout.toViewLayout(): IntEnum<ViewLayout>? = ViewLayout.enumOf(this.value.toInt())

    private fun MailSettingsSwipeAction.toSwipeAction(): IntEnum<SwipeAction>? = SwipeAction.enumOf(this.value.toInt())

    private fun MailSettingsPgpScheme.toPackageType(): IntEnum<PackageType>? = PackageType.enumOf(this.value.toInt())

    private fun MailSettingsComposerDirection.toAndroidComposerDirection(): Boolean? {
        return when (this) {
            MailSettingsComposerDirection.LEFT_TO_RIGHT -> false
            MailSettingsComposerDirection.RIGHT_TO_LEFT -> true
            else -> null
        }
    }

    private fun MailSettingsPmSignature.toPMSignature(): IntEnum<PMSignature>? {
        val intValue = this.value.toInt()
        return PMSignature.enumOf(intValue)
    }
}
