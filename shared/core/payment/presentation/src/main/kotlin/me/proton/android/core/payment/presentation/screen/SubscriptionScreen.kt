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

@file:Suppress("UseComposableActions")

package me.proton.android.core.payment.presentation.screen

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.appbar.ProtonNavigationIcon
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens.Spacing
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.design.compose.theme.titleMediumNorm
import ch.protonmail.android.design.compose.viewmodel.hiltViewModelOrNull
import me.proton.android.core.payment.presentation.R
import me.proton.android.core.payment.presentation.component.ProductList
import me.proton.android.core.payment.presentation.component.ProductListAction
import me.proton.android.core.payment.presentation.component.ProductListState
import me.proton.android.core.payment.presentation.component.ProductListViewModel
import me.proton.android.core.payment.presentation.component.SubscriptionList
import me.proton.android.core.payment.presentation.component.SubscriptionListAction
import me.proton.android.core.payment.presentation.component.SubscriptionListState
import me.proton.android.core.payment.presentation.component.SubscriptionListViewModel
import me.proton.android.core.payment.presentation.model.LocalUpsellEnabled
import me.proton.android.core.payment.presentation.model.Product
import me.proton.android.core.payment.presentation.model.Subscription

@Composable
fun SubscriptionScreen(
    onClose: () -> Unit = {},
    onSuccess: (Product) -> Unit = {},
    onErrorMessage: (String) -> Unit = {},
    subscriptionViewModel: SubscriptionListViewModel? = hiltViewModelOrNull(),
    productViewModel: ProductListViewModel? = if (LocalUpsellEnabled.current) hiltViewModelOrNull() else null
) {
    val subscriptionState = subscriptionViewModel?.state?.collectAsStateWithLifecycle()?.value
        ?: SubscriptionListState.Data(listOf(Subscription.test_free))

    val productState = productViewModel?.state?.collectAsStateWithLifecycle()?.value
        ?: ProductListState.Data(emptyList())

    SubscriptionScreen(
        onClose = onClose,
        onSuccess = onSuccess,
        onErrorMessage = onErrorMessage,
        onRetryClicked = {
            subscriptionViewModel?.perform(SubscriptionListAction.Load())
            productViewModel?.perform(ProductListAction.Load())
        },
        subscriptionState = subscriptionState,
        productState = productState
    )
}

@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun SubscriptionScreen(
    onClose: () -> Unit = {},
    onSuccess: (Product) -> Unit = {},
    onErrorMessage: (String) -> Unit = {},
    onRetryClicked: () -> Unit = {},
    subscriptionState: SubscriptionListState,
    productState: ProductListState
) {
    val subscriptionList = remember(subscriptionState) { (subscriptionState as? SubscriptionListState.Data)?.list }
    val productList = remember(productState) { (productState as? ProductListState.Data)?.list }
    val areUpgradeAvailable = remember(productList) { productList?.isNotEmpty() ?: false }

    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.payment_subscription_title),
                        style = ProtonTheme.typography.titleMediumNorm
                    )
                },
                navigationIcon = { ProtonNavigationIcon(onClick = onClose) },
                backgroundColor = ProtonTheme.colors.backgroundNorm
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {

            // Force SubscriptionList to fill maxHeight on loading/error.
            val subscriptionHeight = when {
                subscriptionList == null -> maxHeight
                else -> Dp.Unspecified
            }

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                SubscriptionList(
                    modifier = Modifier
                        .padding(Spacing.Medium)
                        .height(subscriptionHeight),
                    onRetryClicked = onRetryClicked,
                    state = subscriptionState
                )

                if (areUpgradeAvailable) {
                    Text(
                        text = stringResource(R.string.payment_subscription_upgrade_your_plan),
                        modifier = Modifier
                            .padding(bottom = Spacing.Medium)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = ProtonTheme.typography.titleLargeNorm
                    )
                }

                ProductList(
                    modifier = Modifier.padding(Spacing.Medium),
                    onSuccess = {
                        onRetryClicked()
                        onSuccess(it)
                    },
                    onErrorMessage = { onErrorMessage(it) },
                    onRetryClicked = onRetryClicked,
                    state = productState
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
internal fun SubscriptionScreenPreview() {
    ProtonTheme { SubscriptionScreen() }
}

@Composable
@PreviewLightDark
internal fun SubscriptionScreenErrorPreview() {
    ProtonTheme {
        SubscriptionScreen(
            subscriptionState = SubscriptionListState.Error("An error occurs. Please retry."),
            productState = ProductListState.Error("An error occurs. Please retry.")
        )
    }
}

@Composable
@PreviewScreenSizes
internal fun SubscriptionScreenSizePreview() {
    ProtonTheme { SubscriptionScreen() }
}
