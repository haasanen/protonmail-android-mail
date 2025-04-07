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

package ch.protonmail.android.mailmailbox.presentation.mailbox.model

import androidx.annotation.DrawableRes
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel

sealed interface ClearAllStateUiModel {
    data object Hidden : ClearAllStateUiModel
    sealed interface Visible : ClearAllStateUiModel {
        data class UpsellBannerWithLink(
            val bannerText: TextUiModel,
            val linkText: TextUiModel,
            @DrawableRes val icon: Int
        ) : Visible

        data class ClearAllBannerWithButton(
            val bannerText: TextUiModel,
            val buttonText: TextUiModel,
            @DrawableRes val icon: Int
        ) : Visible

        data class InfoBanner(val text: TextUiModel) : Visible
    }
}
