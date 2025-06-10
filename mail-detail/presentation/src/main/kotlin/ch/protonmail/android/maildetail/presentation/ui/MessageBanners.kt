package ch.protonmail.android.maildetail.presentation.ui

import java.time.Duration
import java.time.Instant
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonBanner
import ch.protonmail.android.design.compose.component.ProtonBannerWithButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.MessageBannersUiModel
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumInverted
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.maildetail.presentation.model.AutoDeleteBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.ExpirationBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.ScheduleSendBannerUiModel
import ch.protonmail.android.maildetail.presentation.util.toFormattedAutoDeleteTime
import ch.protonmail.android.maildetail.presentation.util.toFormattedExpirationTime
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

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
        when (val uiModel = messageBannersUiModel.expirationBannerUiModel) {
            is ExpirationBannerUiModel.NoExpiration -> Unit
            is ExpirationBannerUiModel.Expiration -> ExpirationBanner(uiModel)
        }
        when (val uiModel = messageBannersUiModel.autoDeleteBannerUiModel) {
            is AutoDeleteBannerUiModel.NoAutoDelete -> Unit
            is AutoDeleteBannerUiModel.AutoDelete -> AutoDeleteBanner(uiModel)
        }
        if (messageBannersUiModel.shouldShowBlockedSenderBanner) {
            ProtonBannerWithButton(
                bannerText = TextUiModel.TextRes(R.string.message_blocked_sender_banner_text).string(),
                buttonText = TextUiModel.TextRes(R.string.message_blocked_sender_button_text).string(),
                icon = R.drawable.ic_proton_circle_slash,
                onButtonClicked = onUnblockSender
            )
        }
    }
}

@Composable
private fun AutoDeleteBanner(uiModel: AutoDeleteBannerUiModel.AutoDelete) {
    val context = LocalContext.current
    val deletesIn = remember {
        mutableStateOf(Duration.between(Instant.now(), uiModel.deletesAt).toKotlinDuration())
    }
    val formattedExpiration = deletesIn.value
        .toFormattedAutoDeleteTime(context.resources)
        .joinToString(separator = ", ")
    val autoDeleteText = stringResource(
        R.string.message_auto_delete_banner_text,
        formattedExpiration
    )

    LaunchedEffect(Unit) {
        repeat(deletesIn.value.inWholeSeconds.toInt()) {
            delay(1.seconds)
            deletesIn.value = deletesIn.value.minus(1.seconds).coerceAtLeast(1.seconds)
        }
    }

    ProtonBanner(
        icon = R.drawable.ic_proton_trash_clock,
        iconTint = ProtonTheme.colors.iconWeak,
        iconSize = ProtonDimens.IconSize.Medium,
        text = autoDeleteText,
        textStyle = ProtonTheme.typography.bodyMediumWeak,
        backgroundColor = ProtonTheme.colors.backgroundNorm
    )
}

@Composable
private fun ExpirationBanner(uiModel: ExpirationBannerUiModel.Expiration) {
    val context = LocalContext.current
    val expiresIn = remember {
        mutableStateOf(Duration.between(Instant.now(), uiModel.expiresAt).toKotlinDuration())
    }
    val formattedExpiration = expiresIn.value
        .toFormattedExpirationTime(context.resources)
        .joinToString(separator = ", ")
    val expirationText = stringResource(
        R.string.message_expiration_banner_text,
        formattedExpiration
    )

    LaunchedEffect(Unit) {
        repeat(expiresIn.value.inWholeSeconds.toInt()) {
            delay(1.seconds)
            expiresIn.value = expiresIn.value.minus(1.seconds).coerceAtLeast(1.seconds)
        }
    }

    fun isLessThanAnHour() = expiresIn.value.inWholeMinutes < 60.minutes.inWholeMinutes

    ProtonBanner(
        modifier = Modifier.fillMaxWidth(),
        icon = R.drawable.ic_proton_hourglass,
        iconTint = if (isLessThanAnHour()) {
            ProtonTheme.colors.iconInverted
        } else {
            ProtonTheme.colors.iconWeak
        },
        iconSize = ProtonDimens.IconSize.Medium,
        text = expirationText,
        textStyle = ProtonTheme.typography.bodyMedium.copy(
            color = if (isLessThanAnHour()) {
                ProtonTheme.colors.textInverted
            } else {
                ProtonTheme.colors.textWeak
            }
        ),
        backgroundColor = if (isLessThanAnHour()) {
            ProtonTheme.colors.notificationError
        } else {
            ProtonTheme.colors.backgroundNorm
        },
        borderColorIsBackgroundColor = isLessThanAnHour()
    )
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
                expirationBannerUiModel = ExpirationBannerUiModel.Expiration(
                    expiresAt = Instant.now()
                ),
                autoDeleteBannerUiModel = AutoDeleteBannerUiModel.AutoDelete(
                    deletesAt = Instant.now()
                ),
                scheduleSendBannerUiModel = ScheduleSendBannerUiModel.SendScheduled(
                    sendAt = Instant.now()
                )
            ),
            onMarkMessageAsLegitimate = {},
            onUnblockSender = {}
        )
    }
}
