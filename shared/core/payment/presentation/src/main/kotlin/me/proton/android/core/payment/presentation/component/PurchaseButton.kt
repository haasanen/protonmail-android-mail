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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonSolidButton
import ch.protonmail.android.design.compose.theme.ProtonDimens.Spacing
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.viewmodel.hiltViewModelOrNull
import me.proton.android.core.payment.presentation.R
import me.proton.android.core.payment.presentation.component.PurchaseButtonState.Error
import me.proton.android.core.payment.presentation.component.PurchaseButtonState.Idle
import me.proton.android.core.payment.presentation.component.PurchaseButtonState.Loading
import me.proton.android.core.payment.presentation.component.PurchaseButtonState.Pending
import me.proton.android.core.payment.presentation.component.PurchaseButtonState.Success
import me.proton.android.core.payment.presentation.model.Product

@Composable
fun PurchaseButton(
    product: Product,
    modifier: Modifier = Modifier,
    onSuccess: (Product) -> Unit = {},
    onErrorMessage: (String) -> Unit = {},
    viewModel: PurchaseButtonViewModel? = hiltViewModelOrNull(product.productId)
) {
    val state = viewModel?.state?.collectAsStateWithLifecycle()?.value

    LaunchedEffect(state) {
        when (state) {
            is Success -> onSuccess(state.product)
            is Error -> onErrorMessage(state.message)
            else -> Unit
        }
    }

    LaunchedEffect(product) {
        viewModel?.perform(PurchaseButtonAction.Load(product))
    }

    PurchaseButton(
        product = product,
        state = state ?: Idle,
        modifier = modifier,
        onClick = { viewModel?.perform(PurchaseButtonAction.Purchase(product)) }
    )
}

@Composable
fun PurchaseButton(
    product: Product,
    state: PurchaseButtonState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val text = when (state) {
        is Success -> stringResource(R.string.payment_purchase_button_subscribed, product.header.title)
        else -> stringResource(R.string.payment_purchase_button_get, product.header.title)
    }
    PurchaseButton(
        text = text,
        modifier = modifier,
        onClick = onClick,
        isLoading = state is Pending,
        isEnabled = state !is Loading && state !is Success && (state as? Error)?.enabled != false,
        message = (state as? Error)?.message
    )
}

@Composable
fun PurchaseButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    message: String? = null
) {
    ProtonSolidButton(
        onClick = onClick,
        loading = isLoading,
        enabled = isEnabled,
        modifier = modifier
    ) {
        Column {
            Text(text = text)
            message?.let {
                Text(
                    text = it,
                    style = ProtonTheme.typography.labelSmall,
                    maxLines = 3,
                    modifier = Modifier.padding(top = Spacing.MediumLight)
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
internal fun PurchaseButtonPreview() {
    ProtonTheme {
        PurchaseButton(
            product = Product.test_mail2022_1,
            state = Idle,
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}

@Composable
@PreviewLightDark
internal fun PurchaseButtonErrorPreview() {
    ProtonTheme {
        PurchaseButton(
            product = Product.test_mail2022_1,
            state = Error("Payment processor temporarily unavailable. Please try again in a few minutes."),
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}

@Composable
@PreviewLightDark
internal fun PurchaseButtonSuccessPreview() {
    ProtonTheme {
        PurchaseButton(
            product = Product.test_mail2022_1,
            state = Success(Product.test_mail2022_1),
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}
