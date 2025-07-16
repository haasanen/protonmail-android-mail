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

package me.proton.android.core.devicemigration.presentation.target.signin

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.core.account.domain.model.CoreUserId
import me.proton.android.core.devicemigration.presentation.R
import me.proton.core.compose.component.ProtonBackButton
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.effect.Effect
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.util.LaunchOnScreenView
import me.proton.core.compose.util.annotatedStringResource
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.presentation.utils.openBrowserLink

/**
 * Sign-in on target device.
 */
@Composable
@Suppress("UseComposableActions")
internal fun TargetQrSignInScreen(
    onBackToSignIn: () -> Unit,
    onNavigateBack: () -> Unit,
    onSuccess: (userId: CoreUserId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TargetQrSignInViewModel? = hiltViewModelOrNull<TargetQrSignInViewModel>()
) {
    val state by viewModel?.state?.collectAsStateWithLifecycle()
        ?: remember { derivedStateOf { TargetQrSignInState.Loading } }
    TargetQrSignInScreen(
        state = state,
        modifier = modifier,
        onBackToSignIn = onBackToSignIn,
        onFailureScreenView = { viewModel?.onFailureScreenView() },
        onInstructionsScreenView = { viewModel?.onInstructionsScreenView() },
        onNavigateBack = onNavigateBack,
        onSuccess = onSuccess
    )
}

@Composable
@Suppress("UseComposableActions")
internal fun TargetQrSignInScreen(
    state: TargetQrSignInState,
    modifier: Modifier = Modifier,
    onBackToSignIn: () -> Unit = {},
    onFailureScreenView: () -> Unit = {},
    onInstructionsScreenView: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onSuccess: (userId: CoreUserId) -> Unit = {}
) {
    val snackbarHostState = remember { ProtonSnackbarHostState() }
    val title = when (state) {
        is TargetQrSignInState.Failure -> ""
        else -> stringResource(R.string.target_sign_in_title)
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { ProtonSnackbarHost(snackbarHostState) },
        topBar = { TargetQrSignInTopBar(onBackClicked = onNavigateBack, title = title) }
    ) { padding ->
        Box(modifier = modifier.padding(padding)) {
            if (state is TargetQrSignInState.Failure) {
                TargetQrSignInErrorContent(state, onBackToSignIn = onBackToSignIn, onScreenView = onFailureScreenView)
            } else {
                TargetQrSignInContent(state, onScreenView = onInstructionsScreenView)
            }
        }
    }

    TargetQrSignInEffects(
        effect = state.effect,
        onSuccess = onSuccess
    )

    TargetQrSignInSnackbarMessages(snackbarHostState, state)
}

@Composable
private fun TargetQrSignInTopBar(onBackClicked: () -> Unit, title: String) {
    ProtonTopAppBar(
        title = { Text(text = title) },
        navigationIcon = { ProtonBackButton(onBack = onBackClicked) }
    )
}

@Composable
private fun TargetQrSignInContent(
    state: TargetQrSignInState,
    onScreenView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qrBitmapSize = remember { 200.dp }
    val qrBoxSize = remember { qrBitmapSize + 2 * ProtonDimens.LargeSpacing }
    val emptyBitmap = remember { createBitmap(1, 1) }
    var qrBitmap: Bitmap by remember { mutableStateOf(emptyBitmap) }
    val instructions = remember {
        arrayOf(
            R.string.target_sign_in_instruction_1,
            R.string.target_sign_in_instruction_2,
            R.string.target_sign_in_instruction_3,
            R.string.target_sign_in_instruction_4
        )
    }

    LaunchedEffect(state, qrBitmapSize) {
        qrBitmap = when (state) {
            is TargetQrSignInState.Idle -> state.generateBitmap(state.qrCode, qrBitmapSize)
            else -> emptyBitmap
        }
    }

    LaunchOnScreenView(onScreenView)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ProtonDimens.DefaultSpacing)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = ProtonDimens.MediumSpacing)
                .background(
                    Color.White,
                    shape = ProtonTheme.shapes.medium
                )
                .align(Alignment.CenterHorizontally)
                .size(qrBoxSize)
        ) {
            when (state) {
                is TargetQrSignInState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is TargetQrSignInState.Idle -> Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = state.qrCode,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(qrBitmapSize)
                )

                is TargetQrSignInState.Failure -> Image(
                    painter = painterResource(R.drawable.ic_proton_cross_big),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                    colorFilter = ColorFilter.tint(Color.Black)
                )

                is TargetQrSignInState.SuccessfullySignedIn -> Image(
                    painter = painterResource(R.drawable.ic_proton_checkmark),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                    colorFilter = ColorFilter.tint(Color.Black)
                )
            }
        }

        Text(
            text = stringResource(R.string.target_sign_in_scan_code),
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            style = ProtonTheme.typography.body1Medium,
            color = ProtonTheme.colors.textWeak
        )

        instructions.forEach { stringRes ->
            Text(
                text = annotatedStringResource(stringRes),
                modifier = Modifier.padding(
                    top = ProtonDimens.SmallSpacing,
                    start = ProtonDimens.SmallSpacing,
                    end = ProtonDimens.SmallSpacing
                ),
                style = ProtonTheme.typography.body2Regular,
                color = ProtonTheme.colors.textWeak
            )
        }

        LearnMoreButton()
    }
}

@Composable
private fun LearnMoreButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val url = stringResource(R.string.qr_login_learn_more_link)

    ProtonTextButton(
        onClick = { context.openBrowserLink(url) },
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.target_sign_in_learn_more),
            style = ProtonTheme.typography.body2Medium
        )
    }
}

@Composable
private fun TargetQrSignInErrorContent(
    state: TargetQrSignInState.Failure,
    onBackToSignIn: () -> Unit,
    onScreenView: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchOnScreenView(onScreenView)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ProtonDimens.DefaultSpacing)
    ) {
        Image(
            painter = painterResource(R.drawable.edm_target_error_icon),
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(R.string.target_sign_in_error_title),
            style = LocalTypography.current.headline,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = state.message,
            style = LocalTypography.current.body2Regular,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.weight(1.0f))

        if (state.onRetry != null) {
            ProtonSolidButton(
                onClick = state.onRetry,
                contained = false,
                modifier = Modifier.heightIn(min = ProtonDimens.DefaultButtonMinHeight)
            ) {
                Text(text = stringResource(R.string.target_sign_in_error_new_qr))
            }
        }

        ProtonTextButton(
            onClick = onBackToSignIn,
            contained = false,
            modifier = Modifier
                .padding(top = ProtonDimens.SmallSpacing)
                .heightIn(min = ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.target_sign_in_error_back_to_signin))
        }
    }
}

@Composable
private fun TargetQrSignInEffects(effect: Effect<TargetQrSignInEvent>?, onSuccess: (userId: CoreUserId) -> Unit) {
    LaunchedEffect(effect) {
        effect?.consume { event ->
            when (event) {
                is TargetQrSignInEvent.SignedIn -> onSuccess(event.userId)
            }
        }
    }
}

@Composable
private fun TargetQrSignInSnackbarMessages(snackbarHostState: ProtonSnackbarHostState, state: TargetQrSignInState) {
    LaunchedEffect(state) {
        if (state is TargetQrSignInState.Idle) {
            state.errorMessage?.let { msg ->
                snackbarHostState.showSnackbar(
                    ProtonSnackbarType.ERROR,
                    message = msg,
                    duration = SnackbarDuration.Indefinite
                )
            }
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun TargetQrSignInScreenPreview() {
    ProtonTheme {
        TargetQrSignInScreen(
            state = TargetQrSignInState.Idle(
                errorMessage = null,
                qrCode = "qr-code",
                generateBitmap = { _, size ->
                    createBitmap(
                        size.value.toInt(),
                        size.value.toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                }
            )
        )
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun TargetQrSignInScreenErrorPreview() {
    ProtonTheme {
        TargetQrSignInScreen(
            state = TargetQrSignInState.Failure(message = "Error", onRetry = {})
        )
    }
}
