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

package ch.protonmail.android.maillabel.presentation.bottomsheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize

@Composable
fun <T> BottomSheetAnimatedContent(
    state: T,
    modifier: Modifier = Modifier,
    loadingState: T,
    errorStates: Set<T> = emptySet(),
    expandOnTransition: (initialState: T, targetState: T) -> Boolean = { initial, target ->
        initial == loadingState && target != loadingState && target !in errorStates
    },
    animationSpec: FiniteAnimationSpec<IntSize> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    ),
    fadeInDelayMillis: Int = 300, // Expansion animation needs to happen so we're not really delaying the UX
    fadeInDurationMillis: Int = 300,
    fadeOutDurationMillis: Int = 150,
    label: String = "StateTransition",
    content: @Composable AnimatedContentScope.(T) -> Unit
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            when {
                expandOnTransition(initialState, targetState) -> {
                    expandVertically(animationSpec = animationSpec) +
                        fadeIn(
                            animationSpec = tween(fadeInDurationMillis, delayMillis = fadeInDelayMillis)
                        ) togetherWith
                        fadeOut(animationSpec = tween(fadeOutDurationMillis))
                }

                else -> {
                    EnterTransition.None togetherWith ExitTransition.None // No animation
                }
            }
        },
        modifier = modifier,
        label = label,
        content = content
    )
}
