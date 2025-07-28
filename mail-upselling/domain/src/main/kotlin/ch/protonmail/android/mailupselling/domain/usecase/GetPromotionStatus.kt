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

package ch.protonmail.android.mailupselling.domain.usecase

import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.domain.usecase.GetAvailableUpgrades
import javax.inject.Inject

class GetPromotionStatus @Inject constructor(
    private val getAvailableUpgrades: GetAvailableUpgrades
) {

    suspend operator fun invoke(): PromoStatus {
        val plans = runCatching {
            getAvailableUpgrades()
        }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
            ?: return PromoStatus.NO_PLANS
        return invoke(plans)
    }

    operator fun invoke(plans: List<ProductDetail>): PromoStatus {
        val hasPromo = plans.takeIf { it.isNotEmpty() }?.any { it.hasPromotion() } ?: return PromoStatus.NO_PLANS
        return if (hasPromo) {
            PromoStatus.PROMO
        } else {
            PromoStatus.NORMAL
        }
    }
}

private fun ProductDetail.hasPromotion() = this.price != this.renew && this.price.amount < this.renew.amount

enum class PromoStatus {
    NO_PLANS, NORMAL, PROMO
}
