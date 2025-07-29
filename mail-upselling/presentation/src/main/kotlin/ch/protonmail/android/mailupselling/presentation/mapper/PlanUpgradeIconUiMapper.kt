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

import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeIconUiModel
import ch.protonmail.android.mailupselling.presentation.model.planupgrades.PlanUpgradeVariant
import javax.inject.Inject

internal class PlanUpgradeIconUiMapper @Inject constructor() {

    @Suppress("MaxLineLength")
    fun toUiModel(
        upsellingEntryPoint: UpsellingEntryPoint.Feature,
        variant: PlanUpgradeVariant
    ): PlanUpgradeIconUiModel {
        if (variant == PlanUpgradeVariant.SocialProof) {
            return PlanUpgradeIconUiModel(R.drawable.ic_mail_social_proof)
        }

        val drawableRes = when (upsellingEntryPoint) {
            UpsellingEntryPoint.Feature.AutoDelete -> R.drawable.illustration_upselling_auto_delete
            UpsellingEntryPoint.Feature.ContactGroups -> R.drawable.illustration_upselling_contact_groups
            UpsellingEntryPoint.Feature.Folders -> R.drawable.illustration_upselling_labels
            UpsellingEntryPoint.Feature.Labels -> R.drawable.illustration_upselling_labels
            UpsellingEntryPoint.Feature.MobileSignature -> R.drawable.illustration_upselling_mobile_signature
            UpsellingEntryPoint.Feature.MailboxPromo -> R.drawable.illustration_upselling_mailbox_promo
            UpsellingEntryPoint.Feature.ScheduleSend -> R.drawable.illustration_upselling_schedule_send
            UpsellingEntryPoint.Feature.Snooze -> R.drawable.illustration_upselling_snooze

            UpsellingEntryPoint.Feature.Mailbox,
            UpsellingEntryPoint.Feature.Navbar -> R.drawable.illustration_upselling_mailbox
        }

        return PlanUpgradeIconUiModel(drawableRes)
    }
}
