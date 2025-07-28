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

package ch.protonmail.android.mailupselling.presentation.ui.screen.footer.cyclebuttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.AdaptivePreviews
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel
import ch.protonmail.android.mailupselling.presentation.ui.screen.UpsellingContentPreviewData

@Composable
internal fun CycleOptions(
    modifier: Modifier = Modifier,
    plans: PlanUpgradeInstanceListUiModel.Data,
    selectedPlan: PlanUpgradeInstanceUiModel,
    onPlanSelected: (plan: PlanUpgradeInstanceUiModel) -> Unit
) {
    val shorterInteractionSource = remember { MutableInteractionSource() }
    val longerInteractionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier.height(IntrinsicSize.Max)
    ) {
        // Yearly card
        CycleOptionCard(
            cycleOptionUiModel = plans.longerCycle,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f)
                .clickable(
                    interactionSource = longerInteractionSource,
                    indication = null
                ) {
                    onPlanSelected(plans.longerCycle)
                },
            isSelected = plans.longerCycle == selectedPlan
        )

        // Monthly card - will automatically match yearly card height
        CycleOptionCard(
            cycleOptionUiModel = plans.shorterCycle,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f)
                .clickable(
                    interactionSource = shorterInteractionSource,
                    indication = null
                ) {
                    onPlanSelected(plans.shorterCycle)
                },
            isSelected = plans.shorterCycle == selectedPlan
        )
    }
}

@AdaptivePreviews
@Composable
private fun CycleOptionsPreview() {
    ProtonTheme {
        Column(modifier = Modifier.height(500.dp)) {
            CycleOptions(
                plans = UpsellingContentPreviewData.Base.plans.list as PlanUpgradeInstanceListUiModel.Data,
                selectedPlan = UpsellingContentPreviewData.Base.plans.list.longerCycle
            ) { }
        }
    }
}
