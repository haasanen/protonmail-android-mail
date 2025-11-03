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

package ch.protonmail.android.mailupselling.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility

typealias HeaderIcons = Pair<Painter, Painter>
typealias SidebarIcon = Painter

@Composable
fun UpsellingVisibility.Promotional.BlackFriday.getHeaderIcons(): HeaderIcons = when (this) {
    UpsellingVisibility.Promotional.BlackFriday.Wave1 ->
        Pair(
            painterResource(id = R.drawable.ic_upselling_mail),
            painterResource(id = R.drawable.ic_percentage)
        )

    UpsellingVisibility.Promotional.BlackFriday.Wave2 ->
        Pair(
            painterResource(id = R.drawable.ic_proton_brand_proton_mail),
            painterResource(id = R.drawable.ic_zap)
        )
}

@Composable
fun UpsellingVisibility.Promotional.BlackFriday.getSidebarIcon(): SidebarIcon = when (this) {
    UpsellingVisibility.Promotional.BlackFriday.Wave1 -> painterResource(id = R.drawable.ic_percentage)
    UpsellingVisibility.Promotional.BlackFriday.Wave2 -> painterResource(id = R.drawable.ic_zap)
}
