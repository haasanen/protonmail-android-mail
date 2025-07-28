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

package ch.protonmail.android.mailupselling.presentation.extension

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailupselling.presentation.R
import me.proton.android.core.payment.domain.model.ProductDetail
import me.proton.android.core.payment.domain.model.ProductPrice

internal fun ProductPrice.normalizedPrice(cycle: Int): TextUiModel {
    val actualPrice = this.amount.normalized(cycle)
    return TextUiModel.Text(actualPrice.toDecimalString())
}

internal fun ProductPrice.promoPrice(cycle: Int): TextUiModel {
    val actualPrice = this.amount.normalized(cycle)
    return TextUiModel
        .TextResWithArgs(R.string.upselling_get_button_promotional, listOf(currency, actualPrice.toDecimalString()))
}

internal fun ProductDetail.totalPrice(): Float = price.amount.toActualPrice()
internal fun ProductDetail.totalDefaultPrice(): Float = renew.amount.toActualPrice()

@Suppress("MagicNumber")
internal fun Int.toActualPrice() = (this / (1000 * 1000f)).takeIf {
    it != Float.POSITIVE_INFINITY && it != Float.NEGATIVE_INFINITY
} ?: 0f

@Suppress("MagicNumber")
internal fun Int.normalized(cycle: Int) = (this / (1000 * 1000f) / cycle).takeIf {
    it != Float.POSITIVE_INFINITY && it != Float.NEGATIVE_INFINITY
} ?: 0f
