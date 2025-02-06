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

package me.proton.android.core.accountmanager.presentation.switcher.v1

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.accountmanager.presentation.ButtonWithIconAndText
import me.proton.android.core.accountmanager.presentation.R
import me.proton.core.presentation.R as CoreR

@Composable
fun ManageAccountsButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    ButtonWithIconAndText(
        modifier = modifier,
        text = R.string.manage_accounts_title,
        icon = CoreR.drawable.ic_proton_cog_wheel,
        onClick = onClick
    )
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ManageAccountsPreview() {
    ProtonTheme {
        Surface {
            ManageAccountsButton()
        }
    }
}
