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

package ch.protonmail.android.mailupselling.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.comparisontable.ComparisonTableEntitlement.Free
import ch.protonmail.android.mailupselling.presentation.model.comparisontable.ComparisonTableEntitlement.Plus
import ch.protonmail.android.mailupselling.presentation.model.comparisontable.ComparisonTableEntitlementItemUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeEntitlementsListUiModel
import ch.protonmail.android.testdata.upselling.UpsellingTestData
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PlanUpgradeEntitlementsUiMapperTest {

    private val mapper = PlanUpgradeEntitlementsUiMapper()

    @Test
    fun `should return comparison table entitlements when requested `() {
        // Given
        val expected = PlanUpgradeEntitlementsListUiModel.ComparisonTableList(
            listOf(
                ComparisonTableEntitlementItemUiModel(
                    title = TextUiModel.TextRes(R.string.upselling_comparison_table_storage),
                    freeValue = Free.Value(TextUiModel.TextRes(R.string.upselling_comparison_table_storage_value_free)),
                    paidValue = Plus.Value(TextUiModel.TextRes(R.string.upselling_comparison_table_storage_value_plus))
                ),
                ComparisonTableEntitlementItemUiModel(
                    title = TextUiModel.TextRes(R.string.upselling_comparison_table_email_addresses),
                    freeValue = Free.Value(
                        TextUiModel.TextRes(R.string.upselling_comparison_table_email_addresses_value_free)
                    ),
                    paidValue = Plus.Value(
                        TextUiModel.TextRes(R.string.upselling_comparison_table_email_addresses_value_plus)
                    )
                ),
                ComparisonTableEntitlementItemUiModel(
                    title = TextUiModel.TextRes(R.string.upselling_comparison_table_custom_email_domain),
                    freeValue = Free.NotPresent,
                    paidValue = Plus.Present
                ),
                ComparisonTableEntitlementItemUiModel(
                    title = TextUiModel.TextRes(R.string.upselling_comparison_table_desktop_app),
                    freeValue = Free.NotPresent,
                    paidValue = Plus.Present
                ),
                ComparisonTableEntitlementItemUiModel(
                    title = TextUiModel.TextRes(R.string.upselling_comparison_table_unlimited_folders_labels),
                    freeValue = Free.NotPresent,
                    paidValue = Plus.Present
                ),
                ComparisonTableEntitlementItemUiModel(
                    title = TextUiModel.TextRes(R.string.upselling_comparison_table_priority_support),
                    freeValue = Free.NotPresent,
                    paidValue = Plus.Present
                )
            )
        )

        // When
        val actual = mapper.toUiModel(
            plan = UpsellingTestData.MailPlusProducts.MonthlyProductDetail,
            upsellingEntryPoint = UpsellingEntryPoint.Feature.Navbar
        )

        // Then
        assertEquals(expected, actual)
    }
}
