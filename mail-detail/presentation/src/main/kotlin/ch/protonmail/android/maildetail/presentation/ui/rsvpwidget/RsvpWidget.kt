/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.maildetail.presentation.ui.rsvpwidget

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.labelMediumNorm
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.RsvpAttendeeAnswer
import ch.protonmail.android.maildetail.presentation.model.RsvpAttendeeUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpButtonsUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpEventUiModel
import ch.protonmail.android.maildetail.presentation.model.RsvpStatusUiModel
import ch.protonmail.android.maildetail.presentation.previewdata.RsvpWidgetPreviewData

@Composable
fun RsvpWidget(uiModel: RsvpEventUiModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = ProtonDimens.Spacing.Large)
            .background(
                color = ProtonTheme.colors.backgroundNorm,
                shape = ProtonTheme.shapes.extraLarge
            )
            .border(
                width = ProtonDimens.OutlinedBorderSize,
                color = ProtonTheme.colors.borderNorm,
                shape = ProtonTheme.shapes.extraLarge
            )
            .clip(ProtonTheme.shapes.extraLarge)
    ) {
        uiModel.status?.let {
            RSVPStatus(it)
        }

        Column(
            modifier = Modifier.padding(ProtonDimens.Spacing.ExtraLarge)
        ) {
            RsvpOverview(
                title = uiModel.title,
                dateTime = uiModel.dateTime,
                isAttendanceOptional = uiModel.isAttendanceOptional
            )

            RsvpResponse(uiModel.buttons)
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

            uiModel.calendar?.let {
                RsvpDetailsRow(
                    icon = R.drawable.ic_proton_circle_filled,
                    text = uiModel.calendar.name.string(),
                    iconTint = uiModel.calendar.color
                )
            }

            uiModel.recurrence?.let {
                RsvpDetailsRow(
                    icon = R.drawable.ic_proton_arrows_rotate,
                    text = uiModel.recurrence.string()
                )
            }

            uiModel.location?.let {
                RsvpDetailsRow(
                    icon = R.drawable.ic_proton_map_pin,
                    text = uiModel.location.string()
                )
            }

            val organizerName = (uiModel.organizer.name ?: uiModel.organizer.email).string()
            RsvpDetailsRow(
                icon = R.drawable.ic_proton_user,
                text = "$organizerName ${stringResource(id = R.string.rsvp_widget_organizer)}"
            )

            RsvpAttendees(uiModel.attendees)
        }
    }
}

@Composable
private fun RSVPStatus(uiModel: RsvpStatusUiModel, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .background(color = uiModel.getBackgroundColor())
            .padding(
                horizontal = ProtonDimens.Spacing.ExtraLarge,
                vertical = ProtonDimens.Spacing.Large
            ),
        text = stringResource(id = uiModel.getMessage()),
        textAlign = TextAlign.Start,
        style = ProtonTheme.typography.bodyMedium.copy(
            color = uiModel.getTextColor()
        )
    )
}

@Composable
private fun RsvpOverview(
    title: TextUiModel,
    dateTime: TextUiModel,
    isAttendanceOptional: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title.string(),
                style = ProtonTheme.typography.titleLargeNorm
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))

            Text(
                text = dateTime.string(),
                style = ProtonTheme.typography.bodyLargeNorm
            )

            if (isAttendanceOptional) {
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))
                Text(
                    text = stringResource(id = R.string.rsvp_widget_attendance_optional),
                    style = ProtonTheme.typography.bodyMediumWeak
                )
            }
        }

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Medium))
        Box(
            modifier = Modifier
                .size(MailDimens.RsvpCalendarLogoSize)
                .border(
                    width = ProtonDimens.OutlinedBorderSize,
                    color = ProtonTheme.colors.borderNorm,
                    shape = ProtonTheme.shapes.large
                )
        ) {
            Image(
                modifier = Modifier.align(Alignment.Center),
                painter = painterResource(id = R.drawable.ic_logo_calendar),
                contentDescription = NO_CONTENT_DESCRIPTION
            )
        }
    }
}

@Composable
private fun RsvpResponse(uiModel: RsvpButtonsUiModel) {
    when (uiModel) {
        is RsvpButtonsUiModel.Hidden -> Unit
        is RsvpButtonsUiModel.Shown -> {
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
            Text(
                text = stringResource(id = R.string.rsvp_widget_attending),
                style = ProtonTheme.typography.labelMediumNorm
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))

            when (uiModel.answer) {
                RsvpAttendeeAnswer.Yes -> RsvpSingleButton(label = R.string.rsvp_widget_yes_long)
                RsvpAttendeeAnswer.No -> RsvpSingleButton(label = R.string.rsvp_widget_no_long)
                RsvpAttendeeAnswer.Maybe -> RsvpSingleButton(label = R.string.rsvp_widget_maybe_long)
                RsvpAttendeeAnswer.Unanswered -> RsvpAllButtons()
            }
        }
    }
}

@Composable
private fun RsvpSingleButton(@StringRes label: Int, modifier: Modifier = Modifier) {
    val expanded = remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MailDimens.RsvpButtonHeight)
            .background(
                color = ProtonTheme.colors.interactionBrandWeakNorm,
                shape = ProtonTheme.shapes.massive
            )
            .clip(ProtonTheme.shapes.massive)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ProtonTheme.colors.interactionBrandWeakPressed),
                role = Role.Button,
                onClick = { expanded.value = true }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = label),
            style = ProtonTheme.typography.bodyLarge.copy(
                color = ProtonTheme.colors.brandPlus30
            )
        )
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
        Icon(
            modifier = Modifier.size(ProtonDimens.IconSize.Small),
            painter = painterResource(id = R.drawable.ic_proton_chevron_down_filled),
            contentDescription = NO_CONTENT_DESCRIPTION,
            tint = ProtonTheme.colors.brandPlus30
        )

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            containerColor = ProtonTheme.colors.backgroundNorm
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.rsvp_widget_yes_long),
                        style = ProtonTheme.typography.bodyMediumNorm
                    )
                },
                onClick = { expanded.value = false }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.rsvp_widget_maybe_long),
                        style = ProtonTheme.typography.bodyMediumNorm
                    )
                },
                onClick = { expanded.value = false }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.rsvp_widget_no_long),
                        style = ProtonTheme.typography.bodyMediumNorm
                    )
                },
                onClick = { expanded.value = false }
            )
        }
    }
}

@Composable
private fun RsvpAllButtons(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        RsvpButton(label = R.string.rsvp_widget_yes, onClick = {}, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))
        RsvpButton(label = R.string.rsvp_widget_maybe, onClick = {}, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Compact))
        RsvpButton(label = R.string.rsvp_widget_no, onClick = {}, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RsvpButton(
    @StringRes label: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(MailDimens.RsvpButtonHeight)
            .background(
                color = ProtonTheme.colors.interactionBrandWeakNorm,
                shape = ProtonTheme.shapes.massive
            )
            .clip(ProtonTheme.shapes.massive)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = ProtonTheme.colors.interactionBrandWeakPressed),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = label),
            style = ProtonTheme.typography.bodyLarge.copy(
                color = ProtonTheme.colors.brandPlus30
            )
        )
    }
}

@Composable
private fun RsvpDetailsRow(
    @DrawableRes icon: Int,
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes endIcon: Int? = null,
    iconTint: Color = ProtonTheme.colors.iconWeak
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(ProtonDimens.Spacing.Standard),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(ProtonDimens.IconSize.Medium),
            painter = painterResource(id = icon),
            contentDescription = NO_CONTENT_DESCRIPTION,
            tint = iconTint
        )
        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))
        Text(
            text = text,
            style = ProtonTheme.typography.bodyMediumWeak
        )
        if (endIcon != null) {
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.MediumLight))
            Icon(
                modifier = Modifier.size(ProtonDimens.IconSize.Small),
                painter = painterResource(id = endIcon),
                contentDescription = NO_CONTENT_DESCRIPTION,
                tint = iconTint
            )
        }
    }
}

@Composable
private fun RsvpAttendees(attendees: List<RsvpAttendeeUiModel>) {
    if (attendees.size == 1) {
        val attendee = attendees.first()
        val text = "${stringResource(id = R.string.rsvp_widget_you)} • ${attendee.email.string()}"

        RsvpDetailsRow(
            icon = attendee.answer.getIcon(isOnlyAttendee = true),
            text = text,
            iconTint = attendee.answer.getIconTint(isOnlyAttendee = true)
        )
    } else {
        val isExpanded = remember { mutableStateOf(false) }

        RsvpDetailsRow(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { isExpanded.value = !isExpanded.value }
                ),
            icon = R.drawable.ic_proton_users,
            text = pluralStringResource(
                id = R.plurals.rsvp_widget_participants,
                count = attendees.size,
                attendees.size
            ),
            endIcon = if (isExpanded.value) {
                R.drawable.ic_proton_chevron_up_filled
            } else {
                R.drawable.ic_proton_chevron_down_filled
            }
        )

        if (isExpanded.value) {
            attendees.forEach { attendee ->
                val text = if (attendee.name == null) {
                    attendee.email.string()
                } else {
                    "${attendee.name.string()} • ${attendee.email.string()}"
                }

                RsvpDetailsRow(
                    icon = attendee.answer.getIcon(isOnlyAttendee = false),
                    text = text,
                    iconTint = attendee.answer.getIconTint(isOnlyAttendee = false)
                )
            }
        }
    }
}

@Composable
private fun RsvpAttendeeAnswer.getIcon(isOnlyAttendee: Boolean) = when (this) {
    RsvpAttendeeAnswer.Yes -> R.drawable.ic_proton_checkmark_circle
    RsvpAttendeeAnswer.No -> R.drawable.ic_proton_cross_circle
    RsvpAttendeeAnswer.Maybe -> R.drawable.ic_proton_question_circle
    RsvpAttendeeAnswer.Unanswered -> if (isOnlyAttendee) {
        R.drawable.ic_proton_users
    } else {
        R.drawable.ic_proton_circle
    }
}

@Composable
private fun RsvpAttendeeAnswer.getIconTint(isOnlyAttendee: Boolean) = when (this) {
    RsvpAttendeeAnswer.Yes -> ProtonTheme.colors.notificationSuccess
    RsvpAttendeeAnswer.No -> ProtonTheme.colors.notificationError
    RsvpAttendeeAnswer.Maybe -> ProtonTheme.colors.notificationWarning
    RsvpAttendeeAnswer.Unanswered -> if (isOnlyAttendee) {
        ProtonTheme.colors.iconWeak
    } else {
        ProtonTheme.colors.iconDisabled
    }
}

private fun RsvpStatusUiModel.getMessage() = when (this) {
    RsvpStatusUiModel.EventCancelled -> R.string.rsvp_widget_event_cancelled
    RsvpStatusUiModel.EventCancelledInviteOutdated -> R.string.rsvp_widget_event_cancelled_invite_outdated
    RsvpStatusUiModel.EventEnded -> R.string.rsvp_widget_event_ended
    RsvpStatusUiModel.HappeningNow -> R.string.rsvp_widget_happening_now
    RsvpStatusUiModel.InviteOutdated -> R.string.rsvp_widget_invite_outdated
    RsvpStatusUiModel.OfflineInviteOutdated -> R.string.rsvp_widget_offline_invite_outdated
    RsvpStatusUiModel.AddressIsIncorrect -> R.string.rsvp_widget_address_is_incorrect
    RsvpStatusUiModel.UserIsOrganizer -> R.string.rsvp_widget_user_is_organizer
}

@Composable
private fun RsvpStatusUiModel.getTextColor() = when (this) {
    RsvpStatusUiModel.EventCancelled -> ProtonTheme.colors.notificationError900
    RsvpStatusUiModel.EventCancelledInviteOutdated -> ProtonTheme.colors.notificationError900
    RsvpStatusUiModel.EventEnded -> ProtonTheme.colors.notificationWarning900
    RsvpStatusUiModel.HappeningNow -> ProtonTheme.colors.notificationSuccess900
    RsvpStatusUiModel.InviteOutdated -> ProtonTheme.colors.textNorm
    RsvpStatusUiModel.OfflineInviteOutdated -> ProtonTheme.colors.textNorm
    RsvpStatusUiModel.AddressIsIncorrect -> ProtonTheme.colors.textNorm
    RsvpStatusUiModel.UserIsOrganizer -> ProtonTheme.colors.textNorm
}

@Composable
private fun RsvpStatusUiModel.getBackgroundColor() = when (this) {
    RsvpStatusUiModel.EventCancelled -> ProtonTheme.colors.notificationError100
    RsvpStatusUiModel.EventCancelledInviteOutdated -> ProtonTheme.colors.notificationError100
    RsvpStatusUiModel.EventEnded -> ProtonTheme.colors.notificationWarning100
    RsvpStatusUiModel.HappeningNow -> ProtonTheme.colors.notificationSuccess100
    RsvpStatusUiModel.InviteOutdated -> ProtonTheme.colors.backgroundDeep
    RsvpStatusUiModel.OfflineInviteOutdated -> ProtonTheme.colors.backgroundDeep
    RsvpStatusUiModel.AddressIsIncorrect -> ProtonTheme.colors.backgroundDeep
    RsvpStatusUiModel.UserIsOrganizer -> ProtonTheme.colors.backgroundDeep
}

@Preview
@Composable
fun RsvpWidgetUnansweredPreview() {
    RsvpWidget(
        uiModel = RsvpWidgetPreviewData.UnansweredWithMultipleParticipants
    )
}

@Preview
@Composable
fun RsvpWidgetAnsweredPreview() {
    RsvpWidget(
        uiModel = RsvpWidgetPreviewData.AnsweredWithOneParticipantAndStatus
    )
}
