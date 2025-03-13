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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ch.protonmail.android.design.compose.theme.ProtonDimens.BorderSize
import ch.protonmail.android.design.compose.theme.ProtonDimens.Spacing
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodySmallHint
import me.proton.android.core.payment.presentation.model.Subscription

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = ProtonTheme.colors.backgroundNorm
    ),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = BorderStroke(
        width = BorderSize.Default,
        color = ProtonTheme.colors.borderStrong
    )
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border
    ) {
        Column(modifier = Modifier.padding(Spacing.Medium)) {
            ProductHeader(subscription.header)
            EntitlementList(subscription.entitlements)
            if (subscription.additionalText.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Spacing.Medium),
                    color = ProtonTheme.colors.separatorNorm
                )
                subscription.additionalText.forEach {
                    Text(
                        modifier = Modifier.padding(Spacing.Small),
                        text = it,
                        style = ProtonTheme.typography.bodySmallHint
                    )
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
internal fun SubscriptionCardPreview() {
    ProtonTheme {
        SubscriptionCard(
            subscription = Subscription.test,
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}

@Composable
@PreviewLightDark
internal fun SubscriptionCardFreePreview() {
    ProtonTheme {
        SubscriptionCard(
            subscription = Subscription.test_free,
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}
