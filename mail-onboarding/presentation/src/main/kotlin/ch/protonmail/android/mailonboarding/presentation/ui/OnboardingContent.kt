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

package ch.protonmail.android.mailonboarding.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailonboarding.presentation.OnboardingScreenTestTags
import ch.protonmail.android.mailonboarding.presentation.R
import ch.protonmail.android.mailonboarding.presentation.model.OnboardingUiModel

@Composable
internal fun OnboardingContent(content: OnboardingUiModel) {
    Column {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(id = content.title),
            style = ProtonTheme.typography.titleLargeNorm.copy(textAlign = TextAlign.Center)
        )

        Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Large))

        Image(
            modifier = Modifier
                .testTag(OnboardingScreenTestTags.OnboardingImage)
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 200.dp),
            contentScale = ContentScale.Fit,
            painter = painterResource(id = content.illustrationId),
            contentDescription = stringResource(id = R.string.onboarding_illustration_content_description)
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.Spacing.Large)
                .padding(horizontal = ProtonDimens.Spacing.Large),
            text = stringResource(id = content.headlineId),
            style = ProtonTheme.typography.bodyLargeNorm.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        )

        Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                modifier = Modifier
                    .padding(ProtonDimens.Spacing.Large),
                text = stringResource(id = content.descriptionId),
                style = ProtonTheme.typography.bodyMediumWeak.copy(textAlign = TextAlign.Center)
            )
        }
    }
}
