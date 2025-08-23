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

package ch.protonmail.android.mailupselling.presentation.ui.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeEntitlementListUiModel
import coil.compose.rememberAsyncImagePainter
import me.proton.android.core.payment.presentation.IconResource

@Composable
fun ExpandableFeatureList(
    features: List<PlanUpgradeEntitlementListUiModel>,
    modifier: Modifier = Modifier,
    initialVisibleCount: Int = 3
) {
    var isExpanded by remember { mutableStateOf(false) }

    val visibleFeatures = if (isExpanded) {
        features
    } else {
        features.take(initialVisibleCount)
    }

    val hasMoreItems = features.size > initialVisibleCount

    Column(
        modifier = modifier.animateContentSize(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Large)
    ) {
        visibleFeatures.forEach { feature ->
            PlanEntitlement(feature)
        }

        if (hasMoreItems) {
            ShowMoreButton(
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded }
            )
        }
    }
}

@Composable
private fun PlanEntitlement(entitlementUiModel: PlanUpgradeEntitlementListUiModel) {
    val painter = when (entitlementUiModel) {
        is PlanUpgradeEntitlementListUiModel.Local -> painterResource(entitlementUiModel.localResource)
        is PlanUpgradeEntitlementListUiModel.Remote -> rememberAsyncImagePainter(
            model = IconResource(entitlementUiModel.remoteResource),
            error = painterResource(R.drawable.ic_logo_mail_mono),
            fallback = painterResource(R.drawable.ic_logo_mail_mono),
            placeholder = painterResource(R.drawable.ic_logo_mail_mono)
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .background(
                    color = ProtonTheme.colors.backgroundInvertedDeep,
                    shape = RoundedCornerShape(size = ProtonDimens.Spacing.Standard)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(ProtonDimens.IconSize.Default).padding(ProtonDimens.Spacing.Small),
                painter = painter,
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        }

        Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Standard))
        Text(
            text = entitlementUiModel.text.string(),
            style = ProtonTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ShowMoreButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(vertical = ProtonDimens.Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Standard)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = ProtonTheme.colors.backgroundInvertedDeep,
                    shape = RoundedCornerShape(size = ProtonDimens.Spacing.Standard)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isExpanded) {
                    ImageVector.vectorResource(R.drawable.ic_proton_chevron_up_filled)
                } else {
                    ImageVector.vectorResource(R.drawable.ic_proton_chevron_down_filled)
                },
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak,
                modifier = Modifier
                    .size(ProtonDimens.IconSize.Default)
                    .padding(ProtonDimens.Spacing.Small)
            )
        }

        Text(
            text = if (isExpanded) {
                stringResource(R.string.upselling_onboarding_entitlements_show_less)
            } else {
                stringResource(R.string.upselling_onboarding_entitlements_show_more)
            },
            style = ProtonTheme.typography.bodyLarge,
            color = ProtonTheme.colors.textNorm
        )
    }
}
