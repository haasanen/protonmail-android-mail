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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ch.protonmail.android.design.compose.theme.ProtonDimens.IconSize
import ch.protonmail.android.design.compose.theme.ProtonDimens.Spacing
import ch.protonmail.android.design.compose.theme.ProtonTheme
import coil.compose.rememberAsyncImagePainter
import me.proton.android.core.payment.domain.model.ProductEntitlement
import me.proton.android.core.payment.presentation.IconResource
import me.proton.android.core.payment.presentation.model.Subscription
import me.proton.core.presentation.R

@Composable
fun EntitlementList(entitlements: List<ProductEntitlement>) {
    Column(modifier = Modifier.padding(top = Spacing.Medium)) {
        entitlements.forEach { EntitlementItem(it) }
    }
}

@Composable
fun EntitlementItem(item: ProductEntitlement, modifier: Modifier = Modifier) {
    when (item) {
        is ProductEntitlement.Description -> DescriptionItem(item, modifier)
        is ProductEntitlement.Progress -> ProgressItem(item, modifier)
    }
}

@Composable
internal fun DescriptionItem(item: ProductEntitlement.Description, modifier: Modifier = Modifier) {
    val defaultIcon = painterResource(R.drawable.ic_proton_checkmark)
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier.size(IconSize.Medium),
            painter = rememberAsyncImagePainter(
                model = item.iconName?.let { IconResource(it) },
                error = defaultIcon,
                fallback = defaultIcon,
                placeholder = defaultIcon
            ),
            contentDescription = null,
            tint = ProtonTheme.colors.iconAccent
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically).padding(start = Spacing.MediumLight),
            text = item.text,
            style = ProtonTheme.typography.labelMedium,
            color = ProtonTheme.colors.textWeak,
            textAlign = TextAlign.Start,
            maxLines = 2
        )
    }
}

@Composable
internal fun ProgressItem(item: ProductEntitlement.Progress, modifier: Modifier = Modifier) {
    val level1 = ProtonTheme.colors.notificationSuccess
    val level2 = ProtonTheme.colors.notificationWarning
    val level3 = ProtonTheme.colors.notificationError
    val progress = remember { (item.current - item.min) / item.max.toFloat() }
    val color = remember {
        when (progress) {
            in 0.0f..0.5f -> level1
            in 0.5f..0.8f -> level2
            else -> level3
        }
    }
    Column(modifier = modifier) {
        Row {
            Text(
                modifier = Modifier.padding(end = Spacing.MediumLight).weight(1f),
                text = item.startText,
                style = ProtonTheme.typography.labelMedium,
                color = ProtonTheme.colors.textWeak,
                textAlign = TextAlign.Start,
                maxLines = 1
            )
            Text(
                modifier = Modifier.padding(start = Spacing.MediumLight),
                text = item.endText,
                style = ProtonTheme.typography.labelMedium,
                color = ProtonTheme.colors.textWeak,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.MediumLight),
            progress = { progress },
            drawStopIndicator = {},
            color = color
        )
    }
}

@Composable
@PreviewLightDark
internal fun EntitlementListSubscriptionCardPreview() {
    ProtonTheme {
        SubscriptionCard(
            subscription = Subscription.test,
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}
