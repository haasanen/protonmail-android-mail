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

package ch.protonmail.android.design.compose.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.PositionalThreshold
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import ch.protonmail.android.design.compose.R
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtonPullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    IndicatorBox(
        modifier = modifier.size(ProtonDimens.PullToRefreshOuterCircleSize),
        state = state,
        isRefreshing = isRefreshing,
        containerColor = ProtonTheme.colors.backgroundInvertedSecondary,
        maxDistance = PositionalThreshold,
        elevation = ProtonDimens.ShadowElevation.Soft
    ) {
        Crossfade(
            targetState = isRefreshing,
            animationSpec = tween(durationMillis = 200),
            modifier = Modifier.align(Alignment.Center)
        ) { refreshing ->

            if (refreshing) {
                PullToRefreshSpinner(
                    lottieResId = R.raw.proton_spinner,
                    modifier = Modifier
                        .size(ProtonDimens.PullToRefreshInnerSpinnerSize)
                )
            } else {

                // Map distanceFraction (0..2) -> rotation degrees (0..720)
                val rotation = state.distanceFraction.coerceIn(0f, 2f) * 360f

                Image(
                    painter = painterResource(id = R.drawable.proton_spinner_stationary),
                    contentDescription = null,
                    modifier = Modifier
                        .size(ProtonDimens.PullToRefreshInnerSpinnerSize)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun PullToRefreshSpinner(lottieResId: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        alignment = Alignment.TopStart,
        modifier = modifier
    )
}

