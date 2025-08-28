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

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.protonmail.android.design.compose.R

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ProtonNavigationIcon(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    isMenuVisible: Boolean,
    onClick: () -> Unit
) {
    val sharedIconState = rememberSharedContentState(key = "nav_icon")
    val animatedVector = AnimatedImageVector.animatedVectorResource(id = R.drawable.ic_menu_to_back)

    var rememberEnd by rememberSaveable { mutableStateOf(true) }
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            modifier = Modifier.sharedElement(
                sharedContentState = sharedIconState,
                animatedVisibilityScope = animatedVisibilityScope
            ),
            painter = rememberAnimatedVectorPainter(animatedImageVector = animatedVector, atEnd = !rememberEnd),
            contentDescription = if (isMenuVisible) stringResource(R.string.drawer_menu_description) else {
                stringResource(R.string.drawer_menu_description)
            }
        )
    }

    LaunchedEffect(isMenuVisible) {
        rememberEnd = isMenuVisible
    }
}
