/*
 * Copyright (c) 2024 Proton AG
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.accountmanager.presentation.switcher

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import ch.protonmail.android.design.compose.theme.LocalColors
import ch.protonmail.android.design.compose.theme.LocalShapes
import ch.protonmail.android.design.compose.theme.ProtonColors
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId

@Composable
fun AccountSwitcher(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    primaryAccount: AccountListItem.Ready.Primary?,
    otherAccounts: List<AccountListItem>,
    onExpandedChange: (Boolean) -> Unit,
    onEvent: (AccountSwitchEvent) -> Unit,
    sidebarColors: ProtonColors = ProtonTheme.colors.sidebarColors ?: ProtonTheme.colors
) {
    var rowSize by remember { mutableStateOf(Size.Zero) }

    Column(
        modifier = modifier.clip(LocalShapes.current.large)
    ) {
        if (primaryAccount != null) {
            CompositionLocalProvider(LocalColors provides sidebarColors) {
                AccountSwitcherRowWithChevron(
                    accountListItem = primaryAccount,
                    modifier = Modifier
                        .clickable { onExpandedChange(true) }
                        .onGloballyPositioned { coordinates ->
                            rowSize = coordinates.size.toSize()
                        }
                )
            }
        }

        DropdownMenu(
            modifier = Modifier
                .background(LocalColors.current.backgroundNorm)
                .widthIn(min = with(LocalDensity.current) { rowSize.width.toDp() }, max = 400.dp),
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            AccountSwitcherDropdownMenuContent(
                primary = primaryAccount,
                other = otherAccounts,
                onEvent = onEvent
            )
        }
    }
}


@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AccountSwitcherListPreview() {
    var isExpanded by remember { mutableStateOf(false) }
    AccountSwitcher(
        isExpanded = isExpanded,
        primaryAccount = AccountListItem.Ready.Primary(
            AccountItem(
                userId = UserId("1"),
                name = "John Doe",
                email = "john.doe@example.test",
                initials = "JD"
            )
        ),
        otherAccounts = emptyList(),
        onExpandedChange = { isExpanded = it },
        onEvent = {},
        modifier = Modifier
            .background(LocalColors.current.backgroundNorm)
            .padding(16.dp)
    )
}
