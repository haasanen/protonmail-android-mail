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

package ch.protonmail.android.feature.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.R
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.feature.account.SignOutAccountViewModel.State
import me.proton.core.domain.entity.UserId

@Composable
fun SignOutAccountDialog(
    modifier: Modifier = Modifier,
    userId: UserId? = null,
    actions: SignOutAccountDialog.Actions,
    viewModel: SignOutAccountViewModel = hiltViewModel()
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    when (viewState) {
        State.SignedOut -> actions.onSignedOut()
        State.Removed -> actions.onRemoved()
        else -> Unit
    }

    SignOutDialog(
        modifier = modifier,
        onDismiss = actions.onCancelled,
        onDisableAccount = { viewModel.signOut(userId, removeAccount = false) },
        onRemoveAccount = { viewModel.signOut(userId, removeAccount = true) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UseComposableActions")
@Composable
private fun SignOutDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onDisableAccount: () -> Unit,
    onRemoveAccount: () -> Unit
) {
    var removeAccount by remember { mutableStateOf(false) }
    var signingOut by remember { mutableStateOf(false) }

    ProtonAlertDialog(
        modifier = modifier,
        title = stringResource(R.string.dialog_sign_out_account_title),
        text = {
            Column {
                ProtonAlertDialogText(R.string.dialog_sign_out_account_description)
                Spacer(Modifier.size(ProtonDimens.Spacing.Standard))
                Row(modifier = Modifier.clickable { removeAccount = !removeAccount }) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        Checkbox(
                            modifier = Modifier
                                .align(Alignment.CenterVertically),
                            checked = removeAccount,
                            onCheckedChange = { removeAccount = !removeAccount }
                        )
                    }
                    Spacer(Modifier.size(ProtonDimens.Spacing.Standard))
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = stringResource(R.string.dialog_remove_account_description),
                        style = ProtonTheme.typography.bodyMediumNorm
                    )
                }
            }
        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            ProtonAlertDialogButton(
                titleResId = R.string.dialog_sign_out_account_confirm,
                loading = signingOut
            ) {
                signingOut = true
                if (removeAccount) onRemoveAccount() else onDisableAccount()
            }
        },
        dismissButton = {
            ProtonAlertDialogButton(
                R.string.dialog_sign_out_account_cancel
            ) {
                onDismiss()
            }
        }
    )
}


object SignOutAccountDialog {

    const val USER_ID_KEY = "user id"

    data class Actions(
        val onSignedOut: () -> Unit,
        val onRemoved: () -> Unit,
        val onCancelled: () -> Unit
    )
}

object SignOutAccountDialogTestTags {

    const val RootItem = "SignOutAccountDialogRootItem"
    const val YesButton = "YesButton"
    const val NoButton = "NoButton"
}
