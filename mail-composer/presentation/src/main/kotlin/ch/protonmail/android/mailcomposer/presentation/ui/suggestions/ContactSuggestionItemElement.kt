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

package ch.protonmail.android.mailcomposer.presentation.ui.suggestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.toColorInt
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleSmallNorm
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcomposer.presentation.model.ContactSuggestionUiModel
import ch.protonmail.android.mailcomposer.presentation.ui.suggestions.ContactSuggestionsColor.ContactGroupsBackground
import ch.protonmail.android.uicomponents.R
import ch.protonmail.android.uicomponents.text.HighlightedText

@Composable
fun ContactSuggestionItemElement(
    currentText: String,
    item: ContactSuggestionUiModel,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .background(color = ProtonTheme.colors.backgroundInvertedSecondary)
            .fillMaxWidth()
            .padding(horizontal = ProtonDimens.Spacing.Large)
            .padding(vertical = ProtonDimens.Spacing.Medium)
    ) {
        when (item) {
            is ContactSuggestionUiModel.ContactGroup -> ContactSuggestionGroupEntry(currentText, item)
            is ContactSuggestionUiModel.Contact -> ContactSuggestionEntry(currentText, item)
        }
    }
}

@Composable
private fun ContactSuggestionEntry(currentText: String, item: ContactSuggestionUiModel.Contact) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ContactAvatar(item)

        Spacer(Modifier.width(ProtonDimens.Spacing.Large))

        Column {
            HighlightedText(
                text = item.name,
                highlight = currentText,
                maxLines = 1,
                style = ProtonTheme.typography.bodyLargeNorm,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Tiny))
            HighlightedText(
                text = item.email,
                highlight = currentText,
                maxLines = 1,
                style = ProtonTheme.typography.bodyMediumWeak,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ContactSuggestionGroupEntry(currentText: String, item: ContactSuggestionUiModel.ContactGroup) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ContactGroupAvatar()

        Spacer(Modifier.width(ProtonDimens.Spacing.Large))

        Column {
            HighlightedText(
                text = item.name,
                highlight = currentText,
                maxLines = 1,
                style = ProtonTheme.typography.bodyLargeNorm,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Small))
            Text(
                text = item.name,
                maxLines = 1,
                style = ProtonTheme.typography.bodyMediumWeak,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ContactGroupAvatar() {
    Box(
        modifier = Modifier
            .sizeIn(
                minWidth = MailDimens.AvatarSize,
                minHeight = MailDimens.AvatarSize
            )
            .background(
                color = Color(ContactGroupsBackground.toColorInt()),
                shape = ProtonTheme.shapes.large
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_proton_users),
            tint = ProtonTheme.colors.iconInverted,
            contentDescription = null,
            modifier = Modifier.size(MailDimens.AvatarIconSize)
        )
    }
}

@Composable
fun ContactAvatar(contact: ContactSuggestionUiModel.Contact) {
    Box(
        modifier = Modifier
            .sizeIn(
                minWidth = MailDimens.AvatarSize,
                minHeight = MailDimens.AvatarSize
            )
            .background(
                color = contact.avatarColor,
                shape = ProtonTheme.shapes.large
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            textAlign = TextAlign.Center,
            style = ProtonTheme.typography.titleSmallNorm,
            color = Color.White,
            text = contact.initial
        )
    }
}

@Preview(
    name = "Single Contact - Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
@Suppress("MagicNumber")
fun PreviewSingleContactSuggestionItemLight() {
    ProtonTheme(isDark = false) {
        ContactSuggestionItemElement(
            currentText = "doe",
            item = ContactSuggestionUiModel.Contact(
                initial = "JD",
                name = "John Doe",
                email = "john.doe@example.com",
                avatarColor = Color(0xFF3CBB3A)
            ),
            onClick = {}
        )
    }
}

@Preview(
    name = "Single Contact - Dark",
    showBackground = true,
    backgroundColor = 0xFF1A1A1A
)
@Composable
@Suppress("MagicNumber")
fun PreviewSingleContactSuggestionItemDark() {
    ProtonTheme(isDark = true) {
        ContactSuggestionItemElement(
            currentText = "doe",
            item = ContactSuggestionUiModel.Contact(
                initial = "JD",
                name = "John Doe",
                email = "john.doe@example.com",
                avatarColor = Color(0xFF3CBB3A)
            ),
            onClick = {}
        )
    }
}

@Preview(
    name = "Group Suggestion - Light",
    showBackground = true
)
@Composable
fun PreviewContactGroupSuggestionItemLight() {
    ProtonTheme(isDark = false) {
        ContactSuggestionItemElement(
            currentText = "team",
            item = ContactSuggestionUiModel.ContactGroup(
                name = "Design Team",
                emails = listOf("team@company.com", "design@company.com"),
                color = Color.Blue.toString()
            ),
            onClick = {}
        )
    }
}

@Preview(
    name = "Group Suggestion - Dark",
    showBackground = true
)
@Composable
fun PreviewContactGroupSuggestionItemDark() {
    ProtonTheme(isDark = true) {
        ContactSuggestionItemElement(
            currentText = "team",
            item = ContactSuggestionUiModel.ContactGroup(
                name = "Design Team",
                emails = listOf("team@company.com", "design@company.com"),
                color = Color.Blue.toString()
            ),
            onClick = {}
        )
    }
}


private object ContactSuggestionsColor {

    const val ContactGroupsBackground = "#FF3CBB3A"
}
