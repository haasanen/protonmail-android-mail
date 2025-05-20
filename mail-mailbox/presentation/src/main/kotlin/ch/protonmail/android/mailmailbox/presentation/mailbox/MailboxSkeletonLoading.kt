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

package ch.protonmail.android.mailmailbox.presentation.mailbox


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens.MailboxSkeletonRowHeight
import ch.protonmail.android.mailmailbox.presentation.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun MailboxSkeletonLoading(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val rowHeight = MailboxSkeletonRowHeight
    val rowCount = remember(screenHeight, rowHeight) {
        (screenHeight / rowHeight).toInt() + 1
    }

    val lottieResId = if (isDark) R.raw.skeleton_list_item_dark else R.raw.skeleton_list_item_light

    Column(
        modifier = modifier
            .padding(top = ProtonDimens.Spacing.Medium)
            .fillMaxSize(),
        verticalArrangement = spacedBy(ProtonDimens.Spacing.Huge),
        horizontalAlignment = Alignment.Start
    ) {
        repeat(rowCount) {
            LottieSkeletonRow(
                lottieResId = lottieResId,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .padding(start = ProtonDimens.Spacing.Large)
            )
        }
    }
}

@Composable
private fun LottieSkeletonRow(lottieResId: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        alignment = Alignment.TopStart,
        modifier = modifier
    )
}
