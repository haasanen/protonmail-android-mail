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

package me.proton.android.core.auth.presentation.signup.ui

import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.signup.CreateRecoveryAction
import me.proton.android.core.auth.presentation.signup.CreateRecoveryState.WantCountryPicker
import me.proton.android.core.auth.presentation.signup.CreateRecoveryState.CountryPickerFailed
import me.proton.android.core.auth.presentation.signup.CreateRecoveryState.OnCountryPicked
import me.proton.android.core.auth.presentation.signup.viewmodel.SignUpViewModel
import me.proton.core.compose.component.ProtonCloseButton
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallWeak

@Composable
fun CountryPickerScreen(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentState = state
    LaunchedEffect(currentState) {
        when (currentState) {
            is OnCountryPicked,
            is CountryPickerFailed -> onCloseClick()
            else -> Unit
        }
    }

    when (currentState) {
        is WantCountryPicker -> CountryPickerContentScreen(
            modifier = modifier,
            onCloseClick = {
                viewModel.perform(CreateRecoveryAction.DialogAction.CountryPickerClosed)
            },
            onCountrySelected = {
                viewModel.perform(CreateRecoveryAction.DialogAction.CountryPicked(country = it))
            },
            state = currentState
        )

        else -> Unit
    }
}

@Composable
fun CountryPickerContentScreen(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit = {},
    onCountrySelected: (Country) -> Unit = {},
    state: WantCountryPicker
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredCountries by remember(searchQuery, state.countries) {
        derivedStateOf {
            if (searchQuery.isEmpty()) {
                state.countries
            } else {
                state.countries.filter { country ->
                    country.name.contains(searchQuery, ignoreCase = true) || country.countryCode.contains(
                        searchQuery,
                        ignoreCase = true
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(ProtonDimens.DefaultSpacing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = ProtonDimens.DefaultSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProtonCloseButton(onCloseClicked = onCloseClick)

                ProtonSearchBar(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = ProtonDimens.DefaultSpacing),
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = stringResource(id = R.string.auth_signup_countries_search)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredCountries) { country ->
                    CountryItem(
                        country = country,
                        onClick = { onCountrySelected(country) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProtonSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(8.dp)
            ),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_magnifier),
                contentDescription = null,
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun CountryItem(country: Country, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            contentDescription = null,
            alignment = Alignment.Center,
            painter = painterResource(id = country.getFlagDrawable())
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = country.name,
                style = ProtonTypography.Default.defaultNorm
            )
            Text(
                text = country.countryCode,
                style = ProtonTypography.Default.defaultSmallWeak
            )
        }
        Text(
            text = "+${country.callingCode}",
            style = ProtonTypography.Default.defaultNorm
        )
    }
}

@Composable
fun Country.getFlagDrawable(): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(
        "flag_${countryCode.lowercase(Locale.ROOT)}",
        "drawable",
        context.packageName
    )
}
