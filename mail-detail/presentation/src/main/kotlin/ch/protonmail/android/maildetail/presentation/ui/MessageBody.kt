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

package ch.protonmail.android.maildetail.presentation.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonBannerWithButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailcommon.presentation.system.LocalDeviceCapabilitiesProvider
import ch.protonmail.android.mailcommon.presentation.ui.MailDivider
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyUiModel
import ch.protonmail.android.mailmessage.presentation.ui.MessageBodyWebView

@Composable
@Suppress("LongParameterList", "LongMethod")
fun MessageBody(
    modifier: Modifier = Modifier,
    messageBodyUiModel: MessageBodyUiModel,
    actions: MessageBody.Actions,
    onMessageBodyLoaded: (messageId: MessageId, height: Int) -> Unit = { _, _ -> }
) {
    val hasWebView = LocalDeviceCapabilitiesProvider.current.hasWebView

    when {
        messageBodyUiModel.shouldShowEmbeddedImagesBanner && messageBodyUiModel.shouldShowRemoteContentBanner -> {
            ProtonBannerWithButton(
                bannerText = stringResource(id = R.string.message_body_embedded_and_remote_content_banner_text),
                buttonText = stringResource(id = R.string.message_body_load_embedded_and_remote_content_button_text),
                icon = R.drawable.ic_proton_image,
                onButtonClicked = { actions.onLoadRemoteAndEmbeddedContent(messageBodyUiModel.messageId) }
            )
        }

        messageBodyUiModel.shouldShowEmbeddedImagesBanner -> {
            ProtonBannerWithButton(
                bannerText = stringResource(id = R.string.message_body_embedded_images_banner_text),
                buttonText = stringResource(id = R.string.message_body_load_embedded_images_button_text),
                icon = R.drawable.ic_proton_image,
                onButtonClicked = { actions.onLoadEmbeddedImages(messageBodyUiModel.messageId) }
            )
        }

        messageBodyUiModel.shouldShowRemoteContentBanner -> {
            ProtonBannerWithButton(
                bannerText = stringResource(id = R.string.message_body_remote_content_banner_text),
                buttonText = stringResource(id = R.string.message_body_load_remote_content_button_text),
                icon = R.drawable.ic_proton_cog_wheel,
                onButtonClicked = { actions.onLoadRemoteContent(messageBodyUiModel.messageId) }
            )
        }
    }

    if (messageBodyUiModel.shouldShowOpenInProtonCalendar) {
        OpenInProtonCalendarBanner(
            modifier = Modifier.testTag(MessageBodyTestTags.MessageBodyBannerProtonCalendar),
            onOpenInProtonCalendarClick = { actions.onOpenInProtonCalendar(messageBodyUiModel.messageId) }
        )
    }

    MailDivider(modifier = Modifier.padding(top = ProtonDimens.Spacing.Standard))

    if (hasWebView) {
        MessageBodyWebView(
            modifier = modifier,
            messageBodyUiModel = messageBodyUiModel,
            webViewActions = MessageBodyWebView.Actions(
                onMessageBodyLinkClicked = actions.onMessageBodyLinkClicked,
                onMessageBodyLinkLongClicked = {}, // Deferred init to MessageBodyWebView.
                onShowAllAttachments = actions.onShowAllAttachments,
                onToggleAttachmentsExpandCollapseMode = actions.onToggleAttachmentsExpandCollapseMode,
                onExpandCollapseButtonCLicked = actions.onExpandCollapseButtonClicked,
                onAttachmentClicked = actions.onAttachmentClicked,
                loadEmbeddedImage = actions.loadEmbeddedImage,
                onPrint = actions.onPrint
            ),
            onMessageBodyLoaded = onMessageBodyLoaded
        )
    } else {
        MessageBodyNoWebView(
            modifier = modifier
        )
    }
}

@Composable
internal fun MessageBodyNoWebView(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .testTag(MessageBodyTestTags.WebViewAlternative)
            .padding(ProtonDimens.Spacing.ExtraLarge),
        text = stringResource(id = R.string.message_body_error_no_webview)
    )
}

@Composable
@Preview(showBackground = true)
fun MessageBodyButtonBannerPreview() {
    Column(
        modifier = Modifier.padding(vertical = ProtonDimens.Spacing.Large),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ProtonBannerWithButton(
            bannerText = stringResource(id = R.string.message_body_embedded_and_remote_content_banner_text),
            buttonText = stringResource(id = R.string.message_body_load_embedded_and_remote_content_button_text),
            icon = R.drawable.ic_proton_cog_wheel,
            onButtonClicked = {}
        )

        ProtonBannerWithButton(
            bannerText = stringResource(id = R.string.message_body_embedded_images_banner_text),
            buttonText = stringResource(id = R.string.message_body_load_embedded_images_button_text),
            icon = R.drawable.ic_proton_cog_wheel,
            onButtonClicked = {}
        )


        ProtonBannerWithButton(
            bannerText = stringResource(id = R.string.message_body_remote_content_banner_text),
            buttonText = stringResource(id = R.string.message_body_load_remote_content_button_text),
            icon = R.drawable.ic_proton_cog_wheel,
            onButtonClicked = {}
        )
    }
}

object MessageBody {

    data class Actions(
        val onExpandCollapseButtonClicked: () -> Unit,
        val onMessageBodyLinkClicked: (uri: Uri) -> Unit,
        val onShowAllAttachments: () -> Unit,
        val onAttachmentClicked: (attachmentId: AttachmentId) -> Unit,
        val onToggleAttachmentsExpandCollapseMode: () -> Unit,
        val loadEmbeddedImage: (messageId: MessageId, contentId: String) -> EmbeddedImage?,
        val onReply: (MessageId) -> Unit,
        val onReplyAll: (MessageId) -> Unit,
        val onForward: (MessageId) -> Unit,
        val onLoadRemoteContent: (MessageId) -> Unit,
        val onLoadEmbeddedImages: (MessageId) -> Unit,
        val onLoadRemoteAndEmbeddedContent: (MessageId) -> Unit,
        val onOpenInProtonCalendar: (MessageId) -> Unit,
        val onPrint: (MessageId) -> Unit
    )
}

object MessageBodyTestTags {

    const val WebViewAlternative = "MessageBodyWithoutWebView"
    const val MessageBodyBanner = "MessageBodyBanner"
    const val MessageBodyBannerIcon = "MessageBodyBannerIcon"
    const val MessageBodyBannerText = "MessageBodyBannerText"
    const val MessageActionsRootItem = "MessageActionsRootItem"
    const val MessageReplyButton = "MessageReplyButton"
    const val MessageReplyAllButton = "MessageReplyAllButton"
    const val MessageForwardButton = "MessageForwardButton"
    const val MessageBodyBannerProtonCalendar = "MessageBodyBannerProtonCalendar"
}
