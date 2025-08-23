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

package ch.protonmail.android.mailupselling.presentation.ui.screen

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.domain.model.PlanUpgradeCycle
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.UpsellingScreenContentState
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeDescriptionUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeEntitlementListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeEntitlementsListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeIconUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceListUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeInstanceUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradePriceUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeTitleUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeVariant
import me.proton.android.core.payment.domain.model.ProductHeader
import me.proton.android.core.payment.presentation.model.Product

internal object UpsellingContentPreviewData {

    private val MailPlusPlanModelMonthly = PlanUpgradeInstanceUiModel.Standard(
        name = "Mail Plus",
        pricePerCycle = PlanUpgradePriceUiModel(amount = 4.99f, currencyCode = "EUR"),
        totalPrice = PlanUpgradePriceUiModel(amount = 4.99f, currencyCode = "EUR"),
        discountRate = null,
        cycle = PlanUpgradeCycle.Monthly,
        yearlySaving = null,
        product = Product(
            planName = "Plan name",
            productId = "123",
            accountId = "456",
            cycle = 1,
            header = ProductHeader("Title", "Description", "EUR 12.99", "Cycle text", false),
            entitlements = emptyList(),
            renewalText = null
        )
    )

    private val MailPlusPlanModelMonthlyPromo = PlanUpgradeInstanceUiModel.Promotional(
        name = "Mail Plus",
        pricePerCycle = PlanUpgradePriceUiModel(amount = 5.99f, currencyCode = "EUR"),
        promotionalPrice = PlanUpgradePriceUiModel(amount = 4.99f, currencyCode = "EUR"),
        renewalPrice = PlanUpgradePriceUiModel(amount = 5.99f, currencyCode = "EUR"),
        discountRate = null,
        cycle = PlanUpgradeCycle.Monthly,
        yearlySaving = null,
        product = Product(
            planName = "Plan name",
            productId = "123",
            accountId = "456",
            cycle = 1,
            header = ProductHeader("Title", "Description", "EUR 12.99", "Cycle text", false),
            entitlements = emptyList(),
            renewalText = null
        )
    )

    private val MailPlusPlanModelYearly = PlanUpgradeInstanceUiModel.Standard(
        name = "Mail Plus",
        pricePerCycle = PlanUpgradePriceUiModel(amount = 4.99f, currencyCode = "EUR"),
        totalPrice = PlanUpgradePriceUiModel(amount = 49.99f, currencyCode = "EUR"),
        discountRate = null,
        cycle = PlanUpgradeCycle.Yearly,
        yearlySaving = null,
        product = Product(
            planName = "Plan name",
            productId = "123",
            accountId = "456",
            cycle = 1,
            header = ProductHeader("Title", "Description", "EUR 12.99", "Cycle text", false),
            entitlements = emptyList(),
            renewalText = null
        )
    )

    private val MailPlusPlanModelYearlyPromo = PlanUpgradeInstanceUiModel.Promotional(
        name = "Mail Plus",
        pricePerCycle = PlanUpgradePriceUiModel(amount = 49.99f, currencyCode = "EUR"),
        promotionalPrice = PlanUpgradePriceUiModel(amount = 39.99f, currencyCode = "EUR"),
        renewalPrice = PlanUpgradePriceUiModel(amount = 49.99f, currencyCode = "EUR"),
        discountRate = 20,
        cycle = PlanUpgradeCycle.Monthly,
        yearlySaving = null,
        product = Product(
            planName = "Plan name",
            productId = "123",
            accountId = "456",
            cycle = 1,
            header = ProductHeader("Title", "Description", "EUR 12.99", "Cycle text", false),
            entitlements = emptyList(),
            renewalText = null
        )
    )

    private val UnlimitedPlanModelMonthly = PlanUpgradeInstanceUiModel.Standard(
        name = "Proton Unlimited",
        pricePerCycle = PlanUpgradePriceUiModel(amount = 4.99f, currencyCode = "EUR"),
        totalPrice = PlanUpgradePriceUiModel(amount = 4.99f, currencyCode = "EUR"),
        discountRate = null,
        cycle = PlanUpgradeCycle.Monthly,
        yearlySaving = null,
        product = Product(
            planName = "Plan name",
            productId = "123",
            accountId = "456",
            cycle = 1,
            header = ProductHeader("Title", "Description", "EUR 12.99", "Cycle text", false),
            entitlements = emptyList(),
            renewalText = null
        )
    )
    private val UnlimitedPlanModelYearly = PlanUpgradeInstanceUiModel.Standard(
        name = "Proton Unlimited",
        pricePerCycle = PlanUpgradePriceUiModel(amount = 4.99f, currencyCode = "EUR"),
        totalPrice = PlanUpgradePriceUiModel(amount = 4.99f, currencyCode = "EUR"),
        discountRate = null,
        cycle = PlanUpgradeCycle.Monthly,
        yearlySaving = null,
        product = Product(
            planName = "Plan name",
            productId = "123",
            accountId = "456",
            cycle = 12,
            header = ProductHeader("Title", "Description", "EUR 12.99", "Cycle text", false),
            entitlements = emptyList(),
            renewalText = null
        )
    )

    val NormalList = PlanUpgradeInstanceListUiModel.Data.Standard(
        MailPlusPlanModelMonthly,
        MailPlusPlanModelYearly
    )

    val SocialProofList = PlanUpgradeInstanceListUiModel.Data.SocialProof(
        MailPlusPlanModelMonthly,
        MailPlusPlanModelYearly
    )

    val PromoList = PlanUpgradeInstanceListUiModel.Data.IntroPrice(
        MailPlusPlanModelMonthlyPromo,
        MailPlusPlanModelYearlyPromo
    )

    val UnlimitedNormalList = PlanUpgradeInstanceListUiModel.Data.Standard(
        UnlimitedPlanModelMonthly,
        UnlimitedPlanModelYearly
    )

    val SimpleListEntitlements = PlanUpgradeEntitlementsListUiModel.SimpleList(
        listOf(
            PlanUpgradeEntitlementListUiModel.Local(
                text = TextUiModel.Text("Entitlement 1"),
                localResource = R.drawable.ic_upselling_pass
            ),
            PlanUpgradeEntitlementListUiModel.Local(
                text = TextUiModel.Text("Entitlement 2"),
                localResource = R.drawable.ic_upselling_mail
            ),
            PlanUpgradeEntitlementListUiModel.Local(
                text = TextUiModel.Text("Entitlement 3"),
                localResource = R.drawable.ic_upselling_gift
            )
        )
    )

    val Base = UpsellingScreenContentState.Data(
        PlanUpgradeUiModel(
            icon = PlanUpgradeIconUiModel(R.drawable.illustration_upselling_mailbox),
            title = PlanUpgradeTitleUiModel(TextUiModel.Text("Mail Plus")),
            description = PlanUpgradeDescriptionUiModel.Simple(TextUiModel.Text("Description")),
            entitlements = SimpleListEntitlements,
            variant = PlanUpgradeVariant.Normal,
            list = NormalList
        )
    )

    val IntroductoryPrice = UpsellingScreenContentState.Data(
        PlanUpgradeUiModel(
            icon = PlanUpgradeIconUiModel(R.drawable.illustration_upselling_mailbox),
            title = PlanUpgradeTitleUiModel(TextUiModel.Text("Upgrade to Mail Plus")),
            description = PlanUpgradeDescriptionUiModel.Simple(
                TextUiModel.Text("To unlock more storage and premium features")
            ),
            entitlements = SimpleListEntitlements,
            variant = PlanUpgradeVariant.IntroductoryPrice,
            list = PromoList
        )
    )

    val SocialProof = UpsellingScreenContentState.Data(
        PlanUpgradeUiModel(
            icon = PlanUpgradeIconUiModel(R.drawable.ic_mail_social_proof),
            title = PlanUpgradeTitleUiModel(TextUiModel.Text("Upgrade to Mail Plus")),
            description = PlanUpgradeDescriptionUiModel.SocialProof,
            entitlements = SimpleListEntitlements,
            variant = PlanUpgradeVariant.SocialProof,
            list = SocialProofList
        )
    )
}
