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

package me.proton.android.core.accountmanager.presentation.switcher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.toColorInt
import ch.protonmail.android.design.compose.theme.LocalTypography
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.accountmanager.presentation.R
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountItem
import me.proton.android.core.accountmanager.presentation.switcher.v1.AccountListItem
import me.proton.core.util.kotlin.takeIfNotBlank

/**
 * @param trailingRowContent The composable content that is displayed at the end of the row.
 */
@Composable
internal fun BaseAccountSwitcherRow(
    accountListItem: AccountListItem,
    modifier: Modifier = Modifier,
    accountInitialsShape: Shape = ProtonTheme.shapes.large,
    trailingRowContent: @Composable () -> Unit = {},
    trailingColumnContent: @Composable () -> Unit = {}
) {
    Column {

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Large)
        ) {
            val boxColor =
                accountListItem.accountItem.color?.toColorInt()?.let { Color(it) }
                    ?: ProtonTheme.colors.interactionBrandDefaultNorm
            Box(
                modifier = Modifier
                    .background(boxColor, accountInitialsShape)
                    .size(ProtonDimens.IconSize.MediumLarge)
            ) {
                Text(
                    text = accountListItem.accountItem.initials
                        ?: stringResource(R.string.account_switcher_no_initials),
                    color = Color.White,
                    style = LocalTypography.current.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                val isDisabled = accountListItem is AccountListItem.Disabled
                Text(
                    text = accountListItem.accountItem.name,
                    color = if (isDisabled) ProtonTheme.colors.textWeak else ProtonTheme.colors.textNorm,
                    style = LocalTypography.current.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                accountListItem.accountItem.email?.takeIfNotBlank()?.let {
                    Text(
                        text = it,
                        color = if (isDisabled) ProtonTheme.colors.textHint else ProtonTheme.colors.textWeak,
                        style = LocalTypography.current.bodyMedium,
                        modifier = Modifier.padding(top = ProtonDimens.Spacing.Small),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

            trailingRowContent()
        }
        trailingColumnContent()
    }
}

@Preview(showBackground = true)
@Composable
fun BaseAccountSwitcherRowPreview() {
    BaseAccountSwitcherRow(
        accountListItem = AccountListItem.Ready.Primary(
            AccountItem(
                userId = CoreUserId("test"),
                "Annie Ann",
                "annieanne@proton.me",
                "AA",
                counter = 10
            )
        )
    )
}
