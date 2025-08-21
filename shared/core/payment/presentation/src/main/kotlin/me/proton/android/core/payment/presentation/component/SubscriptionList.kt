/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.payment.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.theme.ProtonDimens.Spacing
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.viewmodel.hiltViewModelOrNull
import me.proton.android.core.payment.presentation.model.Subscription

@Composable
fun SubscriptionList(modifier: Modifier = Modifier, viewModel: SubscriptionListViewModel? = hiltViewModelOrNull()) {
    val state = viewModel?.state?.collectAsStateWithLifecycle()?.value

    SubscriptionList(
        modifier = modifier,
        onRetryClicked = { viewModel?.perform(SubscriptionListAction.Load()) },
        state = state ?: SubscriptionListState.Loading
    )
}

@Composable
fun SubscriptionList(
    modifier: Modifier = Modifier,
    onRetryClicked: () -> Unit = {},
    state: SubscriptionListState
) {
    when (state) {
        is SubscriptionListState.Data -> SubscriptionList(
            modifier = modifier,
            list = state.list
        )

        is SubscriptionListState.Failure.Error -> ProtonErrorRetryLayout(
            modifier = modifier,
            description = state.message,
            onClick = { onRetryClicked() }
        )

        is SubscriptionListState.Loading -> ProtonCenteredProgress(
            modifier = modifier
        )

        is SubscriptionListState.Failure.Forbidden -> Unit
    }
}

@Composable
fun SubscriptionList(list: List<Subscription>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        list.forEachIndexed { index, item ->
            SubscriptionItem(
                subscription = item,
                modifier = Modifier.padding(bottom = Spacing.Medium),
                expandedInitially = index == 0
            )
        }
    }
}

@Composable
@PreviewLightDark
internal fun SubscriptionListPreview() {
    ProtonTheme {
        SubscriptionList(
            list = listOf(Subscription.test_free, Subscription.test, Subscription.test_pass2022),
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
private fun SubscriptionListErrorPreview() {
    ProtonTheme {
        SubscriptionList(
            state = SubscriptionListState.Failure.Error("An error occurs. Please retry."),
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}
