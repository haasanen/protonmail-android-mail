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

package me.proton.android.core.auth.presentation.passmanagement

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.auth.presentation.AnnotatedLinkText
import me.proton.android.core.auth.presentation.R
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction.SelectTab
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction.UpdateLoginPassword.SaveLoginPassword
import me.proton.android.core.auth.presentation.passmanagement.PasswordManagementAction.UserInputAction.UpdateMailboxPassword.SaveMailboxPassword
import me.proton.android.core.auth.presentation.passmanagement.ValidationError.ConfirmPasswordMissMatch
import me.proton.android.core.auth.presentation.passmanagement.ValidationError.Other
import me.proton.android.core.auth.presentation.passmanagement.ValidationError.PasswordEmpty
import me.proton.android.core.auth.presentation.passmanagement.ValidationError.PasswordInvalid
import me.proton.android.core.auth.presentation.passvalidator.PasswordValidatorViewModel
import me.proton.core.compose.component.ProtonPasswordOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextFieldError
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.passvalidator.domain.entity.PasswordValidationType
import me.proton.core.passvalidator.domain.entity.PasswordValidatorToken
import me.proton.core.passvalidator.presentation.report.PasswordPolicyReport

data class PasswordGroupConfig(
    val currentPasswordNeeded: Boolean,
    val currentPasswordLabelRes: Int,
    val newPasswordLabelRes: Int,
    val confirmPasswordLabelRes: Int,
    val buttonTextRes: Int,
    val passwordValidationType: PasswordValidationType,
    val createSaveAction: (String, String, String, PasswordValidatorToken?) -> UserInputAction
)

@Suppress("UseComposableActions")
@Composable
fun PasswordManagementScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onError: (String?) -> Unit,
    onLoginPasswordSaved: () -> Unit,
    onMailboxPasswordSaved: () -> Unit,
    viewModel: PasswordManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = true) {
        viewModel.perform(PasswordManagementAction.Close)
    }

    LaunchedEffect(state) {
        when (val currentState = state) {
            is PasswordManagementState.Closed -> onClose()
            is PasswordManagementState.Error -> {
                onError(currentState.error)
                viewModel.perform(PasswordManagementAction.ErrorShown)
            }

            is PasswordManagementState.LoginPasswordSaved -> onLoginPasswordSaved()
            is PasswordManagementState.MailboxPasswordSaved -> onMailboxPasswordSaved()
            else -> Unit
        }
    }

    PasswordManagementScreen(
        modifier = modifier,
        state = state,
        onAction = viewModel::perform,
        onBackClicked = { viewModel.perform(PasswordManagementAction.Close) }
    )
}

@Composable
fun PasswordManagementScreen(
    modifier: Modifier = Modifier,
    state: PasswordManagementState,
    onAction: (PasswordManagementAction) -> Unit = {},
    onBackClicked: () -> Unit = {}
) {
    val isLoading = state is PasswordManagementState.Loading
    val userInput = state.userInputOrNull()

    PasswordManagementScaffold(
        modifier = modifier,
        onBackClicked = onBackClicked,
        isLoading = isLoading
    ) { scaffoldModifier ->
        if (userInput != null) {
            PasswordManagementContent(
                state = userInput,
                onAction = onAction,
                modifier = scaffoldModifier
            )
        }
    }
}

@Composable
fun PasswordManagementScaffold(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    isLoading: Boolean = false,
    content: @Composable (Modifier) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        backgroundColor = LocalColors.current.backgroundNorm,
        topBar = {
            ProtonTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_password_management_title),
                        style = LocalTypography.current.headline
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            painter = painterResource(R.drawable.ic_proton_arrow_back),
                            contentDescription = stringResource(id = R.string.presentation_back)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = LocalColors.current.brandNorm
                )
            }
        } else {
            content(
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun PasswordManagementContent(
    state: PasswordManagementState.UserInput,
    onAction: (PasswordManagementAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val showTabs = state.loginPassword.isAvailable && state.mailboxPassword.isAvailable

    Column(
        modifier = modifier
            .padding(DefaultSpacing)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DefaultSpacing)
    ) {
        if (showTabs) {
            PasswordManagementTabs(
                selectedTab = state.selectedTab,
                onTabSelected = { onAction(SelectTab(it)) }
            )

            when (state.selectedTab) {
                PasswordManagementState.Tab.LOGIN -> LoginPasswordGroup(state, onAction)
                PasswordManagementState.Tab.MAILBOX -> MailboxPasswordGroup(state, onAction)
            }
        } else {
            if (state.loginPassword.isAvailable) {
                LoginPasswordGroup(state, onAction)
            }
            if (state.mailboxPassword.isAvailable) {
                MailboxPasswordGroup(state, onAction)
            }
        }
    }
}

@Composable
private fun PasswordManagementTabs(
    selectedTab: PasswordManagementState.Tab,
    onTabSelected: (PasswordManagementState.Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            PasswordManagementState.Tab.LOGIN to R.string.settings_password_management_tab_main_password,
            PasswordManagementState.Tab.MAILBOX to R.string.settings_password_management_tab_second_password
        )
    }


    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.takeIf { it >= 0 } ?: 0

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = DefaultSpacing),
        backgroundColor = Color.Transparent,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                color = ProtonTheme.colors.textNorm
            )
        }
    ) {
        tabs.forEachIndexed { index, (tab, stringRes) ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = stringResource(stringRes),
                        color = if (selectedIndex == index) {
                            ProtonTheme.colors.textNorm
                        } else {
                            ProtonTheme.colors.textWeak
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun CurrentPasswordField(
    modifier: Modifier = Modifier,
    currentPassword: String,
    onCurrentPasswordChanged: (String) -> Unit,
    labelRes: Int,
    validationError: ValidationError?
) {
    val currentPasswordError = when (validationError) {
        is ValidationError.CurrentPasswordEmpty -> stringResource(R.string.auth_signup_validation_password)
        else -> null
    }
    ProtonPasswordOutlinedTextFieldWithError(
        text = currentPassword,
        onValueChanged = onCurrentPasswordChanged,
        label = { Text(text = stringResource(labelRes)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Password
        ),
        errorText = currentPasswordError,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun ForgotPasswordLink(modifier: Modifier = Modifier) {
    AnnotatedLinkText(
        fullText = stringResource(R.string.settings_password_management_change_password_note),
        linkText = stringResource(R.string.settings_password_management_forgot_main_password),
        url = stringResource(R.string.forgot_password_link),
        modifier = modifier
    )
}

@Suppress("UseComposableActions")
@Composable
private fun PasswordFieldPair(
    newPassword: String,
    confirmNewPassword: String,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    passwordValidationType: PasswordValidationType,
    validationError: ValidationError?,
    onPasswordValidatorToken: (PasswordValidatorToken?) -> Unit,
    newPasswordLabelRes: Int,
    confirmPasswordLabelRes: Int,
    modifier: Modifier = Modifier
) {
    val passwordError = passwordErrorMessage(validationError)
    val confirmPasswordError = confirmPasswordErrorMessage(validationError)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DefaultSpacing)
    ) {
        ProtonPasswordOutlinedTextFieldWithError(
            text = newPassword,
            onValueChanged = { onNewPasswordChanged(it) },
            label = { Text(text = stringResource(newPasswordLabelRes)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth(),
            errorText = passwordError,
            errorContent = { errorMsg ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ProtonDimens.ExtraSmallSpacing)
                ) {
                    if (errorMsg != null) {
                        ProtonTextFieldError(errorText = errorMsg)
                    }

                    PasswordPolicyReport(
                        passwordValidationType = passwordValidationType,
                        password = newPassword,
                        userId = null,
                        onResult = onPasswordValidatorToken,
                        viewModel = hiltViewModelOrNull<PasswordValidatorViewModel>()
                    )
                }
            }
        )

        ProtonPasswordOutlinedTextFieldWithError(
            text = confirmNewPassword,
            onValueChanged = { onConfirmPasswordChanged(it) },
            label = { Text(text = stringResource(confirmPasswordLabelRes)) },
            errorText = confirmPasswordError,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PasswordSaveButton(
    loading: Boolean,
    onSaveClick: () -> Unit,
    buttonTextRes: Int,
    modifier: Modifier = Modifier
) {
    ProtonSolidButton(
        contained = false,
        loading = loading,
        onClick = onSaveClick,
        modifier = modifier
            .padding(top = ProtonDimens.MediumSpacing)
            .height(ProtonDimens.DefaultButtonMinHeight)
    ) {
        Text(text = stringResource(buttonTextRes))
    }
}

@Composable
private fun PasswordGroup(
    config: PasswordGroupConfig,
    validationError: ValidationError?,
    loading: Boolean,
    showForgotPasswordLink: Boolean,
    onAction: (UserInputAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordValidatorToken by remember { mutableStateOf<PasswordValidatorToken?>(null) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DefaultSpacing)
    ) {
        if (config.currentPasswordNeeded) {
            CurrentPasswordField(
                currentPassword = currentPassword,
                onCurrentPasswordChanged = { currentPassword = it },
                labelRes = config.currentPasswordLabelRes,
                validationError = validationError
            )
        }

        if (showForgotPasswordLink) {
            ForgotPasswordLink()
        }

        PasswordFieldPair(
            newPassword = newPassword,
            confirmNewPassword = confirmNewPassword,
            passwordValidationType = config.passwordValidationType,
            validationError = validationError,
            onNewPasswordChanged = { newPassword = it },
            onConfirmPasswordChanged = { confirmNewPassword = it },
            onPasswordValidatorToken = { passwordValidatorToken = it },
            newPasswordLabelRes = config.newPasswordLabelRes,
            confirmPasswordLabelRes = config.confirmPasswordLabelRes
        )

        PasswordSaveButton(
            loading = loading,
            onSaveClick = {
                onAction(
                    config.createSaveAction(currentPassword, newPassword, confirmNewPassword, passwordValidatorToken)
                )
            },
            buttonTextRes = config.buttonTextRes
        )
    }
}

@Composable
private fun LoginPasswordGroup(
    state: PasswordManagementState.UserInput,
    onAction: (UserInputAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = PasswordGroupConfig(
        currentPasswordNeeded = state.loginPassword.currentPasswordNeeded,
        currentPasswordLabelRes = R.string.settings_main_password_hint_current,
        newPasswordLabelRes = R.string.settings_main_password_hint_new,
        confirmPasswordLabelRes = R.string.settings_main_password_hint_new_confirm,
        buttonTextRes = R.string.settings_save_main_password,
        passwordValidationType = PasswordValidationType.Main,
        createSaveAction = { current, new, confirm, token ->
            SaveLoginPassword(current, new, confirm, token)
        }
    )

    PasswordGroup(
        config = config,
        validationError = state.loginPassword.validationError,
        loading = state.loginPassword.loading,
        showForgotPasswordLink = state.loginPassword.isAvailable && state.mailboxPassword.isAvailable,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
private fun MailboxPasswordGroup(
    state: PasswordManagementState.UserInput,
    onAction: (UserInputAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = PasswordGroupConfig(
        currentPasswordNeeded = true, // it is always needed for mailbox password
        currentPasswordLabelRes = R.string.settings_second_password_hint_current_main_password,
        newPasswordLabelRes = R.string.settings_second_password_hint_new,
        confirmPasswordLabelRes = R.string.settings_second_password_hint_new_confirm,
        buttonTextRes = R.string.settings_save_second_password,
        passwordValidationType = PasswordValidationType.Secondary,
        createSaveAction = { current, new, confirm, token ->
            SaveMailboxPassword(current, new, confirm, token)
        }
    )

    PasswordGroup(
        config = config,
        validationError = state.mailboxPassword.validationError,
        loading = state.mailboxPassword.loading,
        showForgotPasswordLink = state.loginPassword.isAvailable && state.mailboxPassword.isAvailable,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
private fun passwordErrorMessage(validationError: ValidationError?): String? = when (validationError) {
    is PasswordEmpty -> stringResource(R.string.auth_signup_validation_password)
    is PasswordInvalid -> stringResource(R.string.auth_signup_validation_password_input_invalid)
    is Other -> validationError.message ?: stringResource(R.string.auth_signup_validation_password_input_invalid)
    else -> null
}

@Composable
private fun confirmPasswordErrorMessage(validationError: ValidationError?): String? = when (validationError) {
    is ConfirmPasswordMissMatch -> stringResource(R.string.auth_signup_validation_passwords_do_not_match)
    is Other -> validationError.message ?: stringResource(R.string.auth_signup_validation_password_input_invalid)
    else -> null
}

@Preview
@Composable
private fun PassManagementScreenMailboxTabPreview(
    @PreviewParameter(PasswordManagementScreenPreviewProvider::class)
    state: PasswordManagementState
) {
    ProtonTheme {
        PasswordManagementScreen(
            state = state,
            onAction = {}
        )
    }
}

