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

package me.proton.android.core.payment.presentation.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ExposedDropdownMenuCard
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.theme.ProtonDimens.Spacing
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.viewmodel.hiltViewModelOrNull
import me.proton.android.core.payment.presentation.R
import me.proton.android.core.payment.presentation.model.Product

@Composable
fun ProductList(
    modifier: Modifier = Modifier,
    onSuccess: (Product) -> Unit = {},
    onErrorMessage: (String) -> Unit = {},
    viewModel: ProductListViewModel? = hiltViewModelOrNull()
) {
    val state = viewModel?.state?.collectAsStateWithLifecycle()?.value

    ProductList(
        modifier = modifier,
        onSuccess = onSuccess,
        onErrorMessage = onErrorMessage,
        onRetryClicked = { viewModel?.perform(ProductListAction.Load()) },
        state = state ?: ProductListState.Loading
    )
}

@Composable
fun ProductList(
    modifier: Modifier = Modifier,
    onSuccess: (Product) -> Unit = {},
    onErrorMessage: (String) -> Unit = {},
    onRetryClicked: () -> Unit = {},
    state: ProductListState
) {
    when (state) {
        is ProductListState.Data -> ProductList(
            modifier = modifier,
            onSuccess = onSuccess,
            onErrorMessage = onErrorMessage,
            list = state.list
        )

        is ProductListState.Error -> ProtonErrorRetryLayout(
            modifier = modifier,
            description = state.message,
            onClick = { onRetryClicked() }
        )

        is ProductListState.Loading -> ProtonCenteredProgress(
            modifier = modifier
        )
    }
}

@Composable
fun ProductList(
    list: List<Product>,
    modifier: Modifier = Modifier,
    onSuccess: (Product) -> Unit = {},
    onErrorMessage: (String) -> Unit = {}
) {
    val cycles = list.rememberCyclesStringResource()
    val cycleKeys = remember(list) { cycles.keys.toTypedArray() }
    val cycleValues = remember(list) { cycles.values.toTypedArray() }
    var cycleIndex by remember { mutableIntStateOf(0) }
    val cycle = remember(cycleIndex) { cycleKeys.getOrNull(cycleIndex) }

    Column(modifier = modifier) {
        if (cycles.size > 1) {
            ExposedDropdownMenuCard(
                list = cycleValues.toList(),
                modifier = Modifier.padding(bottom = Spacing.Medium),
                onSelectedIndexChanged = { cycleIndex = it }
            ) {
                Text(text = it)
            }
        }
        Column {
            list.filter { it.cycle == cycle }.forEachIndexed { index, item ->
                ProductItem(
                    product = item,
                    modifier = Modifier.padding(bottom = Spacing.Medium),
                    onSuccess = onSuccess,
                    onErrorMessage = onErrorMessage,
                    expandedInitially = index == 0
                )
            }
        }
    }
}

@Composable
@Suppress("MagicNumber")
fun List<Product>.rememberCyclesStringResource() = remember(this) {
    fold(mutableSetOf<Int>()) { acc, product ->
        acc.apply { add(product.cycle) }
    }.sorted()
}.associateWith {
    when (it) {
        1 -> stringResource(R.string.payment_pay_monthly)
        12 -> stringResource(R.string.payment_pay_annually)
        24 -> stringResource(R.string.payment_pay_biennially)
        else -> pluralStringResource(R.plurals.payment_pay_other, it)
    }
}

@Composable
@PreviewLightDark
internal fun ProductListPreview() {
    ProtonTheme {
        ProductList(
            list = listOf(Product.test, Product.test_mail2022_1, Product.test_pass2022_1),
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}

@Composable
@PreviewLightDark
internal fun ProductListNoDropdownPreview() {
    ProtonTheme {
        ProductList(
            list = listOf(Product.test_mail2022_1, Product.test_pass2022_1),
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
internal fun ProductListErrorPreview() {
    ProtonTheme {
        ProductList(
            state = ProductListState.Error("An error occurs. Please retry."),
            modifier = Modifier.padding(Spacing.Medium)
        )
    }
}
