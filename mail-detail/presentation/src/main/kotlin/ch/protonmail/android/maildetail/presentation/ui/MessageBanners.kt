package ch.protonmail.android.maildetail.presentation.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonBanner
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.MessageBannersUiModel
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumInverted
import ch.protonmail.android.mailcommon.presentation.model.string

@Composable
fun MessageBanners(messageBannersUiModel: MessageBannersUiModel) {
    Column {
        if (messageBannersUiModel.shouldShowPhishingBanner) {
            ProtonBanner(
                icon = R.drawable.ic_proton_hook,
                iconTint = ProtonTheme.colors.iconInverted,
                text = TextUiModel.TextRes(R.string.message_phishing_banner_text).string(),
                textStyle = ProtonTheme.typography.bodyMediumInverted,
                backgroundColor = ProtonTheme.colors.notificationError,
                borderColorIsBackgroundColor = true
            )
        }
        if (messageBannersUiModel.expirationBannerText != null) {
            ProtonBanner(
                modifier = Modifier.fillMaxWidth(),
                icon = R.drawable.ic_proton_hourglass,
                iconTint = ProtonTheme.colors.iconInverted,
                text = messageBannersUiModel.expirationBannerText.string(),
                textStyle = ProtonTheme.typography.bodyMediumInverted,
                backgroundColor = ProtonTheme.colors.notificationError,
                borderColorIsBackgroundColor = true
            )
        }
    }
}

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
                expirationBannerText = TextUiModel("This message will expire in 1 day, 2 hours, 3 minutes")
            )
        )
    }
}
