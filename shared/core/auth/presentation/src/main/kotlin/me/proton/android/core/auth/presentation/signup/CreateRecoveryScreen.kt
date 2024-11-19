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

@file:Suppress("UseComposableActions")

package me.proton.android.core.auth.presentation.signup

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.addaccount.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonDimens.LargeSpacing
import me.proton.core.compose.theme.ProtonDimens.MediumSpacing
import me.proton.core.compose.theme.ProtonDimens.SmallSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.theme.defaultStrongNorm

@Composable
fun CreateRecoveryScreen(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onSkipClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: (RecoveryMethod, String) -> Unit = { method, value -> },
    viewModel: CreateRecoveryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CreateRecoveryScreen(
        modifier = modifier,
        onBackClicked = onBackClicked,
        onSkipClicked = onSkipClicked,
        onTabSelected = { viewModel.submit(CreateRecoveryAction.SelectCreateRecovery(it)) },
        onRecoverySubmitted = { viewModel.submit(it) },
        onErrorMessage = onErrorMessage,
        onSuccess = { recoveryMethod, value ->
            onSuccess(recoveryMethod, value)
            viewModel.submit(CreateRecoveryAction.SetNavigationDone)
        },
        state = state
    )
}

@Composable
fun CreateRecoveryScreen(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onSkipClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onTabSelected: (RecoveryMethod) -> Unit = {},
    onRecoverySubmitted: (CreateRecoveryAction) -> Unit = {},
    onSuccess: (RecoveryMethod, String) -> Unit = { recovery, method -> },
    state: CreateRecoveryState
) {
    LaunchedEffect(state) {
        when (state) {
            is CreateRecoveryState.FormError -> onErrorMessage(state.message)
            is CreateRecoveryState.Success -> onSuccess(state.recoveryMethod, state.value)
            else -> Unit
        }
    }

    RecoveryMethodScaffold(
        modifier = modifier,
        onBackClicked = onBackClicked,
        onSkipClicked = onSkipClicked,
        onRecoverySubmitted = onRecoverySubmitted,
        onTabSelected = onTabSelected,
        state = state
    )
}

@Composable
fun RecoveryMethodScaffold(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onSkipClicked: () -> Unit = {},
    onTabSelected: (RecoveryMethod) -> Unit = {},
    onRecoverySubmitted: (CreateRecoveryAction) -> Unit = {},
    state: CreateRecoveryState
) {
    val isLoading = state is CreateRecoveryState.Loading
    val emailError = (state as? CreateRecoveryState.FormError.Email)?.message
    val phoneError = (state as? CreateRecoveryState.FormError.Phone)?.message

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painterResource(id = me.proton.core.presentation.R.drawable.ic_proton_close),
                            contentDescription = stringResource(id = R.string.auth_login_close)
                        )
                    }
                },
                actions = {
                    ProtonTextButton(
                        onClick = onSkipClicked
                    ) {
                        Text(
                            text = stringResource(id = R.string.auth_signup_recovery_skip),
                            color = ProtonTheme.colors.textAccent,
                            style = ProtonTheme.typography.defaultStrongNorm
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(DefaultSpacing)
            ) {
                Text(
                    style = LocalTypography.current.headline,
                    text = stringResource(id = R.string.auth_signup_recovery_title)
                )

                Text(
                    text = stringResource(id = R.string.auth_signup_recovery_subtitle),
                    style = ProtonTypography.Default.defaultSmallWeak,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = SmallSpacing)
                )

                RecoveryTabs(
                    modifier = modifier,
                    tabs = listOf("Email", "Phone"),
                    onTabSelected = {
                        onTabSelected(RecoveryMethod.enumOf(it))
                    }
                )

                when (state.recoveryMethod) {
                    RecoveryMethod.Email -> RecoveryMethodFormEmail(
                        loading = isLoading,
                        emailError = emailError,
                        onEmailSubmitted = onRecoverySubmitted
                    )

                    RecoveryMethod.Phone -> RecoveryMethodFormPhone(
                        loading = isLoading,
                        data = state.countries ?: emptyList(),
                        emailError = phoneError,
                        onPhoneSubmitted = onRecoverySubmitted
                    )
                }
            }
        }
    }
}

@Composable
fun RecoveryMethodFormEmail(
    loading: Boolean = false,
    emailError: String? = null,
    onEmailSubmitted: (CreateRecoveryAction) -> Unit = {}
) {
    var email by rememberSaveable { mutableStateOf("") }

    ProtonOutlinedTextFieldWithError(
        text = email,
        onValueChanged = { email = it },
        errorText = emailError,
        label = { Text(text = stringResource(id = R.string.auth_email)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DefaultSpacing)
            .testTag(EMAIL_FIELD_TAG)
    )

    ProtonSolidButton(
        contained = false,
        loading = loading,
        modifier = Modifier
            .padding(top = MediumSpacing)
            .height(ProtonDimens.DefaultButtonMinHeight),
        onClick = { onEmailSubmitted(CreateRecoveryAction.SubmitEmail(email)) }
    ) {
        Text(
            text = stringResource(id = R.string.auth_signup_next)
        )
    }
}

@Composable
fun RecoveryMethodFormPhone(
    emailError: String? = null,
    loading: Boolean = false,
    data: List<Country> = emptyList(),
    onPhoneSubmitted: (CreateRecoveryAction) -> Unit
) {
    var callingCode by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            CountryCodeDropDown(
                modifier = Modifier
                    .padding(top = LargeSpacing)
                    .height(TextFieldDefaults.MinHeight),
                isLoading = loading,
                data = data,
                onInputChanged = { callingCode = it.callingCode.toString() } // int?
            )
        }

        Spacer(modifier = Modifier.width(SmallSpacing))

        Column(
            modifier = Modifier.weight(2f)
        ) {
            ProtonOutlinedTextFieldWithError(
                text = phoneNumber,
                onValueChanged = { phoneNumber = it },
                errorText = emailError,
                label = { Text(text = stringResource(id = R.string.auth_signup_phone_placeholder)) },
                singleLine = true,
                modifier = Modifier
                    .padding(top = MediumSpacing)
                    .testTag(PHONE_FIELD_TAG)
            )
        }
    }

    ProtonSolidButton(
        contained = false,
        loading = loading,
        modifier = Modifier
            .padding(top = MediumSpacing)
            .height(ProtonDimens.DefaultButtonMinHeight),
        onClick = { onPhoneSubmitted(CreateRecoveryAction.SubmitPhone(callingCode, phoneNumber)) }
    ) {
        Text(
            text = stringResource(id = R.string.auth_signup_next)
        )
    }
}

@Composable
fun RecoveryTabs(
    modifier: Modifier = Modifier,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit = {}
) {
    var selectedIndex by remember { mutableIntStateOf(0) }

    TabRow(
        modifier = modifier
            .fillMaxSize()
            .padding(top = DefaultSpacing),
        selectedTabIndex = selectedIndex,
        contentColor = ProtonTheme.colors.textNorm,
        backgroundColor = Color.Transparent,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex])
            )
        }
    ) {
        tabs.forEachIndexed { index, tabTitle ->
            Tab(
                modifier = Modifier.heightIn(min = LocalViewConfiguration.current.minimumTouchTargetSize.height),
                selected = selectedIndex == index,
                onClick = {
                    selectedIndex = index
                    onTabSelected(selectedIndex)
                }
            ) {
                Text(
                    style = ProtonTheme.typography.defaultStrongNorm,
                    color = if (selectedIndex == index) ProtonTheme.colors.textNorm else ProtonTheme.colors.textWeak,
                    text = tabTitle.uppercase()
                )
            }
        }
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.PIXEL_FOLD)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun RecoveryMethodScreenPreview() {
    ProtonTheme {
        CreateRecoveryScreen()
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.PIXEL_FOLD)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
@Suppress("MagicNumber")
internal fun CreateRecoveryPhoneScreenPreview() {
    ProtonTheme {
        CreateRecoveryScreen(
            state = CreateRecoveryState.Idle(
                RecoveryMethod.Phone,
                listOf(
                    Country("CH", 41, "Switzerland"),
                    Country("US", 1, "US")
                )
            )
        )
    }
}
