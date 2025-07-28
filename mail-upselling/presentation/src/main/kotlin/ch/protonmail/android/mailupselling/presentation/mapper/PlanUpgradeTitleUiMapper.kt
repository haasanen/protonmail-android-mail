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
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeTitleUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeVariant
import javax.inject.Inject

internal class PlanUpgradeTitleUiMapper @Inject constructor() {

    fun toUiModel(
        upsellingEntryPoint: UpsellingEntryPoint.Feature,
        variant: PlanUpgradeVariant
    ): PlanUpgradeTitleUiModel {

        if (variant == PlanUpgradeVariant.SocialProof)
            return PlanUpgradeTitleUiModel(TextUiModel(R.string.upselling_mailbox_plus_title_social_proof))

        return when (upsellingEntryPoint) {
            UpsellingEntryPoint.Feature.ContactGroups ->
                PlanUpgradeTitleUiModel(TextUiModel(R.string.upselling_contact_groups_plus_title))

            UpsellingEntryPoint.Feature.Folders ->
                PlanUpgradeTitleUiModel(TextUiModel(R.string.upselling_folders_plus_title))

            UpsellingEntryPoint.Feature.Labels ->
                PlanUpgradeTitleUiModel(TextUiModel(R.string.upselling_labels_plus_title))

            UpsellingEntryPoint.Feature.MobileSignature ->
                PlanUpgradeTitleUiModel(TextUiModel(R.string.upselling_mobile_signature_plus_title))

            UpsellingEntryPoint.Feature.MailboxPromo -> {
                PlanUpgradeTitleUiModel(TextUiModel(R.string.upselling_mailbox_plus_title))
            }

            UpsellingEntryPoint.Feature.Mailbox,
            UpsellingEntryPoint.Feature.Navbar ->
                PlanUpgradeTitleUiModel(TextUiModel(R.string.upselling_mailbox_plus_title))

            UpsellingEntryPoint.Feature.AutoDelete ->
                PlanUpgradeTitleUiModel(TextUiModel(R.string.upselling_auto_delete_plus_title))
        }
    }
}
