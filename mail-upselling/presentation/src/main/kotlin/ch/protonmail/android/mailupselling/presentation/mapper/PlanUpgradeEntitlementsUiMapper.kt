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

package ch.protonmail.android.mailupselling.presentation.mapper

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeIds
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.comparisontable.ComparisonTableEntitlements.Entitlements
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeEntitlementListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeEntitlementsListUiModel
import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.domain.model.ProductEntitlement
import javax.inject.Inject

class PlanUpgradeEntitlementsUiMapper @Inject constructor() {

    fun toUiModel(plan: ProductDetail, upsellingEntryPoint: UpsellingEntryPoint): PlanUpgradeEntitlementsListUiModel {
        if (upsellingEntryPoint is UpsellingEntryPoint.Feature) {
            return mapToComparisonTable()
        }

        val list = when (plan.header.title) {
            PlanUpgradeIds.PlusPlanId -> getPlusEntitlements(upsellingEntryPoint)
            PlanUpgradeIds.UnlimitedPlanId -> getUnlimitedEntitlements()
            else -> mapToDefaults(plan.entitlements)
        }

        return PlanUpgradeEntitlementsListUiModel.SimpleList(list)
    }

    private fun mapToComparisonTable() = PlanUpgradeEntitlementsListUiModel.ComparisonTableList(Entitlements)

    private fun mapToDefaults(list: List<ProductEntitlement>): List<PlanUpgradeEntitlementListUiModel> {
        return list.asSequence()
            .filterIsInstance(ProductEntitlement.Description::class.java)
            .map {
                PlanUpgradeEntitlementListUiModel.Default(
                    TextUiModel.Text(it.text),
                    it.iconName.toString()
                )
            }
            .toList()
    }

    private fun getPlusEntitlements(upsellingEntryPoint: UpsellingEntryPoint) = when (upsellingEntryPoint) {
        UpsellingEntryPoint.Feature.ContactGroups -> ContactGroupsPlusOverriddenEntitlements
        UpsellingEntryPoint.Feature.Folders -> FoldersPlusOverriddenEntitlements
        UpsellingEntryPoint.Feature.Labels -> LabelsPlusOverriddenEntitlements
        UpsellingEntryPoint.Feature.Navbar,
        UpsellingEntryPoint.Feature.Sidebar -> MailboxPlusOverriddenEntitlements

        UpsellingEntryPoint.Feature.MobileSignature -> MobileSignaturePlusOverriddenEntitlements
        UpsellingEntryPoint.Feature.AutoDelete -> AutoDeletePlusOverriddenEntitlements
        UpsellingEntryPoint.Feature.ScheduleSend -> ScheduleSendPlusOverriddenEntitlements
        UpsellingEntryPoint.Feature.Snooze -> SnoozeSendPlusOverriddenEntitlements
    }

    private fun getUnlimitedEntitlements() = UnlimitedOverriddenEntitlements

    companion object {

        private val MailboxPlusOverriddenEntitlements = listOf(
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_plus_feature_storage),
                localResource = R.drawable.ic_upselling_storage
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_plus_feature_email_addresses),
                localResource = R.drawable.ic_upselling_inbox
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_plus_feature_custom_domain),
                localResource = R.drawable.ic_upselling_globe
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_plus_feature_desktop_app),
                localResource = R.drawable.ic_upselling_rocket
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_plus_feature_folders_labels),
                localResource = R.drawable.ic_upselling_tag
            )
        )

        private val SharedPlusOverriddenEntitlements = listOf(
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_plus_feature_storage),
                localResource = R.drawable.ic_upselling_storage
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_plus_feature_email_addresses),
                localResource = R.drawable.ic_upselling_inbox
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_plus_feature_custom_domain),
                localResource = R.drawable.ic_upselling_globe
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_plus_feature_plus_7_features),
                localResource = R.drawable.ic_upselling_gift
            )
        )

        private val ContactGroupsPlusOverriddenEntitlements = SharedPlusOverriddenEntitlements

        private val FoldersPlusOverriddenEntitlements = SharedPlusOverriddenEntitlements

        private val LabelsPlusOverriddenEntitlements = SharedPlusOverriddenEntitlements

        @Suppress("VariableMaxLength")
        private val MobileSignaturePlusOverriddenEntitlements = SharedPlusOverriddenEntitlements

        private val AutoDeletePlusOverriddenEntitlements = SharedPlusOverriddenEntitlements
        private val ScheduleSendPlusOverriddenEntitlements = SharedPlusOverriddenEntitlements
        private val SnoozeSendPlusOverriddenEntitlements = SharedPlusOverriddenEntitlements

        private val UnlimitedOverriddenEntitlements = listOf(
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_unlimited_description_override),
                localResource = R.drawable.ic_upselling_storage
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_unlimited_feature_mail_calendar),
                localResource = R.drawable.ic_upselling_mail
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_unlimited_feature_vpn),
                localResource = R.drawable.ic_upselling_vpn
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_unlimited_feature_drive),
                localResource = R.drawable.ic_upselling_drive
            ),
            PlanUpgradeEntitlementListUiModel.Overridden(
                text = TextUiModel.TextRes(R.string.upselling_unlimited_feature_pass),
                localResource = R.drawable.ic_upselling_pass
            )
        )
    }
}
