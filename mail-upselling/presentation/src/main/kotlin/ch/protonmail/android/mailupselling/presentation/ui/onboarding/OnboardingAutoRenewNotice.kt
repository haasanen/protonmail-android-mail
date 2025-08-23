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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeCycle
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel

@Composable
internal fun OnboardingAutoRenewNotice(modifier: Modifier = Modifier, planUiModel: PlanUpgradeInstanceUiModel) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = getRenewalNoticeForPromotion(planUiModel),
        style = ProtonTheme.typography.labelLarge,
        fontWeight = FontWeight.Normal,
        color = ProtonTheme.colors.brandNorm,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun getRenewalNoticeForPromotion(planUiModel: PlanUpgradeInstanceUiModel): String {
    val displayedPrice = planUiModel.primaryPrice
    val period = when (planUiModel.cycle) {
        PlanUpgradeCycle.Monthly -> stringResource(R.string.upselling_onboarding_autorenew_month)
        PlanUpgradeCycle.Yearly -> stringResource(R.string.upselling_onboarding_autorenew_year)
    }

    val (baseText, price) = when (planUiModel) {
        is PlanUpgradeInstanceUiModel.Promotional ->
            Pair(
                R.string.upselling_onboarding_autorenew_welcome_offer,
                displayedPrice.secondaryPrice?.getFullFormat()
                    ?: displayedPrice.highlightedPrice.getFullFormat()
            )

        is PlanUpgradeInstanceUiModel.Standard ->
            Pair(R.string.upselling_onboarding_autorenew, displayedPrice.highlightedPrice.getFullFormat())
    }

    return stringResource(baseText, price, period)
}
