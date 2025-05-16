package ch.protonmail.android.maildetail.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonBanner
import ch.protonmail.android.design.compose.component.ProtonBannerWithButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.MessageBannersUiModel
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumInverted
import ch.protonmail.android.mailcommon.presentation.model.string

@Composable
fun MessageBanners(
    messageBannersUiModel: MessageBannersUiModel,
    onMarkMessageAsLegitimate: (Boolean) -> Unit,
    onUnblockSender: () -> Unit
) {
    Column {
        if (messageBannersUiModel.shouldShowPhishingBanner) {
            ProtonBannerWithButton(
                icon = R.drawable.ic_proton_hook,
                iconTint = ProtonTheme.colors.iconInverted,
                iconSize = ProtonDimens.IconSize.Medium,
                text = TextUiModel.TextRes(R.string.message_phishing_banner_text).string(),
                buttonText = TextUiModel.TextRes(R.string.message_phishing_banner_mark_as_legitimate_button).string(),
                textStyle = ProtonTheme.typography.bodyMediumInverted,
                backgroundColor = ProtonTheme.colors.notificationError,
                buttonBackgroundColor = Color(PHISHING_BANNER_BUTTON_BACKGROUND),
                borderColorIsBackgroundColor = true,
                onButtonClicked = { onMarkMessageAsLegitimate(true) }
            )
        }
        if (messageBannersUiModel.shouldShowSpamBanner) {
            ProtonBannerWithButton(
                icon = R.drawable.ic_proton_fire,
                iconTint = ProtonTheme.colors.iconInverted,
                iconSize = ProtonDimens.IconSize.Medium,
                text = TextUiModel.TextRes(R.string.message_spam_banner_text).string(),
                buttonText = TextUiModel.TextRes(R.string.message_spam_banner_not_spam_button).string(),
                textStyle = ProtonTheme.typography.bodyMediumInverted,
                backgroundColor = ProtonTheme.colors.notificationError,
                buttonBackgroundColor = Color(PHISHING_BANNER_BUTTON_BACKGROUND),
                borderColorIsBackgroundColor = true,
                onButtonClicked = { onMarkMessageAsLegitimate(false) }
            )
        }
        if (messageBannersUiModel.shouldShowBlockedSenderBanner) {
            ProtonBannerWithButton(
                bannerText = TextUiModel.TextRes(R.string.message_blocked_sender_banner_text).string(),
                buttonText = TextUiModel.TextRes(R.string.message_blocked_sender_button_text).string(),
                icon = R.drawable.ic_proton_circle_slash,
                onButtonClicked = onUnblockSender
            )
        }
        if (messageBannersUiModel.expirationBannerText != null) {
            ProtonBanner(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.ic_proton_hourglass,
                iconTint = ProtonTheme.colors.iconInverted,
                iconSize = ProtonDimens.IconSize.Medium,
                text = messageBannersUiModel.expirationBannerText.string(),
                textStyle = ProtonTheme.typography.bodyMediumInverted,
                backgroundColor = ProtonTheme.colors.notificationError,
                borderColorIsBackgroundColor = true
            )
        }
    }
}

private const val PHISHING_BANNER_BUTTON_BACKGROUND = 0x33FFFFFF

@Preview(
    name = "Main settings screen light mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun PreviewMessageBanners() {
    ProtonTheme {
        MessageBanners(
            MessageBannersUiModel(
                shouldShowPhishingBanner = true,
                shouldShowSpamBanner = true,
                shouldShowBlockedSenderBanner = true,
                expirationBannerText = TextUiModel("This message will expire in 1 day, 2 hours, 3 minutes")
            ),
            onMarkMessageAsLegitimate = {},
            onUnblockSender = {}
        )
    }
}
