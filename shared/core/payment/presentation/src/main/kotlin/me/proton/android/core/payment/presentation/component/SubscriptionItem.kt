/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.payment.presentation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ch.protonmail.android.design.compose.theme.ProtonDimens.Spacing
import ch.protonmail.android.design.compose.theme.ProtonTheme
import me.proton.android.core.payment.presentation.model.Subscription

@Composable
fun SubscriptionItem(subscription: Subscription, modifier: Modifier = Modifier) {
    SubscriptionCard(
        subscription = subscription,
        modifier = modifier
    )
}

@Composable
@PreviewLightDark
internal fun SubscriptionItemPreview() {
    ProtonTheme {
        SubscriptionItem(
            subscription = Subscription.test,
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}
