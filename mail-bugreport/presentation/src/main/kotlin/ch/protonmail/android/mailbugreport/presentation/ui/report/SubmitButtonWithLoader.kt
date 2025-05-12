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

package ch.protonmail.android.mailbugreport.presentation.ui.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailbugreport.presentation.R

@Composable
internal fun SubmitButtonWithLoader(isLoading: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(ProtonDimens.Spacing.Large)

    Row {
        if (isLoading) {
            ProtonCenteredProgress(modifier = Modifier.size(ProtonDimens.IconSize.Default))
        } else {
            Surface(
                modifier = Modifier
                    .clip(shape)
                    .clickable { onClick() },
                shape = shape,
                color = ProtonTheme.colors.interactionBrandWeakNorm
            ) {
                Text(
                    modifier = Modifier
                        .padding(ProtonDimens.Spacing.Standard)
                        .padding(horizontal = ProtonDimens.Spacing.Small),
                    style = ProtonTheme.typography.titleSmall,
                    color = ProtonTheme.colors.brandPlus30,
                    text = stringResource(R.string.report_a_problem_submit_button)
                )
            }
        }
        Spacer(modifier = Modifier.width(width = ProtonDimens.Spacing.Standard))
    }
}
