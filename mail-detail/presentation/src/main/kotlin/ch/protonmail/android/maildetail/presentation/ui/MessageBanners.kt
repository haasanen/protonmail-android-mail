package ch.protonmail.android.maildetail.presentation.ui

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonBanner
import ch.protonmail.android.design.compose.component.ProtonBannerWithButton
import ch.protonmail.android.design.compose.component.ProtonCompactBannerWithButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumInverted
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.AutoDeleteBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.ExpirationBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.MessageBannersUiModel
import ch.protonmail.android.maildetail.presentation.model.ScheduleSendBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.SnoozeBannerUiModel
import ch.protonmail.android.maildetail.presentation.model.UnsubscribeFromNewsletterBannerUiModel
import ch.protonmail.android.maildetail.presentation.util.toFormattedAutoDeleteTime
import ch.protonmail.android.maildetail.presentation.util.toFormattedExpirationTime
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
@Suppress("UseComposableActions")
fun MessageBanners(
    messageBannersUiModel: MessageBannersUiModel,
    onMarkMessageAsLegitimate: (Boolean) -> Unit,
    onUnblockSender: () -> Unit,
    onCancelScheduleMessage: () -> Unit,
    onUnsnoozeMessage: () -> Unit,
    onUnsubscribeFromNewsletter: () -> Unit
) {
    Column {
        if (messageBannersUiModel.shouldShowPhishingBanner) {
            ProtonBannerWithButton(
                icon = R.drawable.ic_proton_hook,
                iconTint = ProtonTheme.colors.iconInverted,
                iconSize = ProtonDimens.IconSize.Medium,
                text = stringResource(R.string.message_phishing_banner_text),
                buttonText = stringResource(R.string.message_phishing_banner_mark_as_legitimate_button),
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
                text = stringResource(R.string.message_spam_banner_text),
                buttonText = stringResource(R.string.message_spam_banner_not_spam_button),
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
        when (messageBannersUiModel.unsubscribeFromNewsletterBannerUiModel) {
            UnsubscribeFromNewsletterBannerUiModel.NoUnsubscribe -> Unit
            UnsubscribeFromNewsletterBannerUiModel.AlreadyUnsubscribed -> AlreadyUnsubscribedFromNewsletterBanner()
            UnsubscribeFromNewsletterBannerUiModel.UnsubscribeNewsletter ->
                UnsubscribeFromNewsletterBanner(onButtonClick = onUnsubscribeFromNewsletter)
        }
        when (val uiModel = messageBannersUiModel.scheduleSendBannerUiModel) {
            ScheduleSendBannerUiModel.NoScheduleSend -> Unit
            is ScheduleSendBannerUiModel.SendScheduled -> ScheduleSendBanner(uiModel, onCancelScheduleMessage)
        }
        when (val uiModel = messageBannersUiModel.snoozeBannerUiModel) {
            SnoozeBannerUiModel.NotSnoozed -> Unit
            is SnoozeBannerUiModel.SnoozeScheduled -> SnoozeBanner(uiModel, onUnsnoozeMessage)
        }
        if (messageBannersUiModel.shouldShowBlockedSenderBanner) {
            ProtonBannerWithButton(
                bannerText = stringResource(R.string.message_blocked_sender_banner_text),
                buttonText = stringResource(R.string.message_blocked_sender_button_text),
                icon = R.drawable.ic_proton_circle_slash,
                onButtonClicked = onUnblockSender
            )
        }
    }
}

@Composable
private fun SnoozeBanner(uiModel: SnoozeBannerUiModel.SnoozeScheduled, onUnsnoozeMessage: () -> Unit) {
    val bannerBaseText = stringResource(R.string.snooze_message_snoozed_until_banner_title)
    val sendTimeFormatted = uiModel.snoozedUntil.string()
    val bannerText = buildAnnotatedString {
        append(bannerBaseText)
        appendLine()
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(sendTimeFormatted)
        }
    }
    ProtonBannerWithButton(
        bannerText = bannerText,
        buttonText = stringResource(R.string.snooze_message_unsnooze_banner_button),
        icon = R.drawable.ic_proton_clock,
        onButtonClicked = onUnsnoozeMessage
    )
}

@Composable
private fun ScheduleSendBanner(uiModel: ScheduleSendBannerUiModel.SendScheduled, onCancelScheduleMessage: () -> Unit) {
    val bannerBaseText = stringResource(R.string.schedule_message_sent_at_banner_title)
    val sendTimeFormatted = uiModel.sendAt.string()
    val bannerText = buildAnnotatedString {
        append(bannerBaseText)
        appendLine()
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(sendTimeFormatted)
        }
    }
    ProtonBannerWithButton(
        bannerText = bannerText,
        buttonText = stringResource(R.string.schedule_message_edit_message_button),
        icon = R.drawable.ic_proton_clock_paper_plane,
        onButtonClicked = onCancelScheduleMessage,
        isLoading = uiModel.isScheduleBeingCancelled
    )
}


@Composable
private fun AutoDeleteBanner(uiModel: AutoDeleteBannerUiModel.AutoDelete) {
    val context = LocalContext.current
    val timeBetweenNowAndDeletion = uiModel.deletesAt - Clock.System.now()
    val deletesIn = remember { mutableStateOf(timeBetweenNowAndDeletion) }
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
    val timeBetweenNowAndExpiration = uiModel.expiresAt - Clock.System.now()
    val expiresIn = remember { mutableStateOf(timeBetweenNowAndExpiration) }
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

@Composable
private fun UnsubscribeFromNewsletterBanner(onButtonClick: () -> Unit) {
    ProtonCompactBannerWithButton(
        bannerText = stringResource(id = R.string.unsubscribe_newsletter_banner_message),
        buttonText = stringResource(R.string.unsubscribe_newsletter_banner_button),
        icon = R.drawable.ic_proton_envelopes,
        onButtonClicked = onButtonClick
    )
}

@Composable
private fun AlreadyUnsubscribedFromNewsletterBanner() {
    ProtonBanner(
        modifier = Modifier.fillMaxWidth(),
        icon = R.drawable.ic_proton_envelope_cross,
        iconTint = ProtonTheme.colors.iconWeak,
        iconSize = ProtonDimens.IconSize.Medium,
        text = stringResource(id = R.string.already_unsubscribed_banner_message),
        textStyle = ProtonTheme.typography.bodyMediumWeak,
        backgroundColor = ProtonTheme.colors.backgroundNorm,
        contentAlignedWithText = false
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
                    expiresAt = Clock.System.now()
                ),
                autoDeleteBannerUiModel = AutoDeleteBannerUiModel.AutoDelete(
                    deletesAt = Clock.System.now()
                ),
                scheduleSendBannerUiModel = ScheduleSendBannerUiModel.SendScheduled(
                    sendAt = TextUiModel.Text("tomorrow at 08:00"),
                    isScheduleBeingCancelled = false
                ),
                snoozeBannerUiModel = SnoozeBannerUiModel.SnoozeScheduled(
                    snoozedUntil = TextUiModel.Text("tomorrow at 08:00")
                ),
                unsubscribeFromNewsletterBannerUiModel = UnsubscribeFromNewsletterBannerUiModel.UnsubscribeNewsletter
            ),
            onMarkMessageAsLegitimate = {},
            onUnblockSender = {},
            onCancelScheduleMessage = {},
            onUnsnoozeMessage = {},
            onUnsubscribeFromNewsletter = {}
        )
    }
}
