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

package ch.protonmail.android.mailcontact.presentation.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcontact.presentation.R

@Composable
fun ContactDetailsTopBar(shouldShowActions: Boolean, actions: ContactDetailsTopBar.Actions) {
    ProtonTopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(ProtonDimens.MailTopBarMinHeight),
        backgroundColor = ProtonTheme.colors.backgroundInvertedNorm,
        title = {},
        navigationIcon = {
            IconButton(onClick = actions.onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    tint = ProtonTheme.colors.iconNorm,
                    contentDescription = stringResource(id = R.string.presentation_back)
                )
            }
        },
        actions = {
            if (shouldShowActions) {
                IconButton(onClick = actions.onEdit) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_proton_pen),
                        tint = ProtonTheme.colors.iconNorm,
                        contentDescription = stringResource(
                            id = R.string.contact_details_edit_contact_content_description
                        )
                    )
                }
                IconButton(onClick = actions.onDelete) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_proton_trash),
                        tint = ProtonTheme.colors.iconNorm,
                        contentDescription = stringResource(
                            id = R.string.contact_details_delete_contact_content_description
                        )
                    )
                }
            }
        }
    )
}

object ContactDetailsTopBar {

    data class Actions(
        val onBack: () -> Unit,
        val onEdit: () -> Unit,
        val onDelete: () -> Unit
    )
}
