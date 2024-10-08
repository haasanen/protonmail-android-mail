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

package ch.protonmail.android.mailsidebar.presentation

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ch.protonmail.android.mailcommon.domain.AppInformation
import ch.protonmail.android.maillabel.presentation.MailLabelsUiModel
import me.proton.android.core.accountmanager.presentation.switcher.AccountListItem

@Stable
class SidebarState(
    val appInformation: AppInformation = AppInformation(),
    val drawerState: DrawerState = DrawerState(DrawerValue.Closed),
    val showContacts: Boolean = true,
    mailLabels: MailLabelsUiModel = MailLabelsUiModel.Loading,
    isSubscriptionVisible: Boolean = true,
    primaryAccount: AccountListItem.Ready.Primary? = null,
    otherAccounts: List<AccountListItem> = emptyList()
) {

    var mailLabels by mutableStateOf(mailLabels)
    var isSubscriptionVisible by mutableStateOf(isSubscriptionVisible)
    var primaryAccount: AccountListItem.Ready.Primary? by mutableStateOf(primaryAccount)
    var otherAccounts: List<AccountListItem> by mutableStateOf(otherAccounts)
}

@Composable
fun rememberSidebarState(
    appInformation: AppInformation,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
): SidebarState = remember {
    SidebarState(
        appInformation = appInformation,
        drawerState = drawerState
    )
}
