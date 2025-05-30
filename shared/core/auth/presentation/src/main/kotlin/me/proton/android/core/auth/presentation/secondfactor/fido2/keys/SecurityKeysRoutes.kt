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

package me.proton.android.core.auth.presentation.secondfactor.fido2.keys

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.proton.core.domain.entity.UserId

object SecurityKeysRoutes {

    internal object Arg {

        const val KEY_USER_ID = "userId"

        fun SavedStateHandle.getUserId(): UserId = UserId(
            checkNotNull(get<String>(KEY_USER_ID)) { "Missing '$KEY_USER_ID' key in SavedStateHandle" }
        )
    }

    object Route {
        object SecurityKeys {

            const val Deeplink: String = "auth/{${Arg.KEY_USER_ID}}/settings/keys"
            fun get(userId: UserId): String = "auth/${userId.id}/settings/keys"
        }
    }

    fun NavGraphBuilder.addSecurityKeysScreen(
        userId: UserId,
        onClose: () -> Unit,
        onAddSecurityKeyClicked: () -> Unit,
        onManageSecurityKeysClicked: () -> Unit
    ) {
        composable(
            route = Route.SecurityKeys.Deeplink,
            arguments = listOf(
                navArgument(Arg.KEY_USER_ID) {
                    type = NavType.StringType
                    defaultValue = userId.id
                }
            )
        ) {
            SecurityKeysScreen(
                onAddSecurityKeyClicked = onAddSecurityKeyClicked,
                onManageSecurityKeysClicked = onManageSecurityKeysClicked,
                onBackClick = onClose
            )
        }
    }
}
