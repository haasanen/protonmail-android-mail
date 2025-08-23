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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonSolidButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.onboarding.OnboardingPlanUpgradeUiModel

@Composable
internal fun FreePlanCard(plan: OnboardingPlanUpgradeUiModel.Free, onDismiss: () -> Unit) {
    Card(
        shape = ProtonTheme.shapes.huge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(ProtonDimens.Spacing.Large)
            .shadow(
                elevation = ProtonDimens.ShadowElevation.Raised,
                ambientColor = ProtonTheme.colors.shadowMedium,
                spotColor = ProtonTheme.colors.shadowMedium,
                shape = ProtonTheme.shapes.huge
            ),
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors().copy(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        )
    ) {
        Column(
            modifier = Modifier.padding(ProtonDimens.Spacing.ExtraLarge),
            verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Compact)
        ) {
            Text(
                text = plan.planName.string(),
                color = ProtonTheme.colors.brandPlus30,
                style = ProtonTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${plan.currency} 0.00",
                style = ProtonTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = ProtonTheme.colors.textNorm
            )

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))

            ExpandableFeatureList(plan.entitlements)

            ProtonSolidButton(
                modifier = Modifier
                    .padding(top = ProtonDimens.Spacing.Standard)
                    .padding(vertical = ProtonDimens.Spacing.Large)
                    .height(MailDimens.onboardingBottomButtonHeight)
                    .fillMaxWidth(),
                onClick = onDismiss,
                shape = ProtonTheme.shapes.huge
            ) {
                Text(
                    text = stringResource(R.string.upselling_onboarding_button_continue_free),
                    style = ProtonTheme.typography.titleMedium,
                    color = ProtonTheme.colors.textInverted,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis
                )
            }
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
private fun FreePlanCardContentPreview() {
    ProtonTheme {
        FreePlanCard(OnboardingUpsellPreviewData.freePlan, onDismiss = {})
    }
}
