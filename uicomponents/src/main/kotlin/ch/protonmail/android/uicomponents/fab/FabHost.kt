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

package ch.protonmail.android.uicomponents.fab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

/**
 * A Fab host where we can lazily change the Fab composable allowing us to delegate the actual implementation of
 * a Fab to individual screens.  Screens wanting to show a fab should use @see LazyFab which will automatically cleanup
 * and remove the fab when the screen moves out of composition
 */
@Composable
fun FabHost(modifier: Modifier = Modifier, fabHostState: ProtonFabHostState) {
    val provider = remember { fabHostState.currentItemProvider }
    provider.value(modifier)
}

@Stable
class ProtonFabHostState {

    internal var currentItemProvider = mutableStateOf<@Composable (Modifier) -> Unit>(nopFabProvider)

    internal fun setFabProvider(fabProvider: @Composable (Modifier) -> Unit) {
        currentItemProvider.value = fabProvider
    }

    internal fun clearFabProvider() {
        currentItemProvider.value = nopFabProvider
    }

    companion object {

        val nopFabProvider = @Composable { _: Modifier -> }
    }
}

/**
 * Define a Fab that can be used by a @see FabHost in the parent screen skeleton. LazyFab will automatically clean up
 * after itself when the Screen leaves composition
 */
@Composable
fun LazyFab(fabHostState: ProtonFabHostState, fabContent: @Composable (modifier: Modifier) -> Unit) {
    // lifecycle owner is the screen that owns the lazy fab
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentLifecycleState = lifecycleOwner.lifecycle.currentStateAsState()
    val fabIsVisible = remember { derivedStateOf { currentLifecycleState.value.isAtLeast(Lifecycle.State.STARTED) } }

    fabHostState.setFabProvider { modifier: Modifier ->
        if (fabIsVisible.value) {
            // only draw the FAB if the screen is started, that way if we navigate away and the screen is stopped
            // then the fab won't be drawn
            fabContent(modifier)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // clean up the fab provider as we have left this composition
            fabHostState.clearFabProvider()
        }
    }
}



