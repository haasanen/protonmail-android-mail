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

package me.proton.android.core.devicemigration.presentation.origin.intro

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import me.proton.android.core.devicemigration.presentation.R
import me.proton.android.core.devicemigration.presentation.origin.qr.QrScanEncoding
import me.proton.android.core.devicemigration.presentation.origin.qr.rememberQrScanLauncher
import me.proton.core.biometric.presentation.rememberBiometricLauncher
import me.proton.core.compose.component.ProtonBackButton
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.effect.Effect
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.util.LaunchOnScreenView
import me.proton.core.compose.util.annotatedStringResource

private val MAX_CONTENT_WIDTH = 520.dp

@Composable
@Suppress("UseComposableActions")
internal fun OriginQrSignInScreen(
    modifier: Modifier = Modifier,
    navigateToAppSettings: () -> Unit,
    onManualCodeInput: () -> Unit,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: OriginQrSignInViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    OriginQrSignInScreen(
        state = state.state,
        effect = state.effect,
        modifier = modifier,
        navigateToAppSettings = navigateToAppSettings,
        onBiometricAuthResult = viewModel::perform,
        onCameraPermissionGranted = { viewModel.perform(OriginQrSignInAction.OnCameraPermissionGranted) },
        onFailureScreenView = viewModel::onFailureScreenView,
        onManualCodeInput = onManualCodeInput,
        onNavigateBack = onNavigateBack,
        onScreenView = viewModel::onScreenView,
        onStart = { viewModel.perform(OriginQrSignInAction.Start) },
        onQrScanResult = { viewModel.perform(it) },
        onSuccess = onSuccess
    )
}

@Composable
@Suppress("LongParameterList", "UseComposableActions")
internal fun OriginQrSignInScreen(
    state: OriginQrSignInState,
    effect: Effect<OriginQrSignInEvent>?,
    modifier: Modifier = Modifier,
    navigateToAppSettings: () -> Unit = {},
    onBiometricAuthResult: (OriginQrSignInAction.OnBiometricAuthResult) -> Unit = {},
    onCameraPermissionGranted: () -> Unit = {},
    onFailureScreenView: () -> Unit = {},
    onManualCodeInput: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onScreenView: (OriginQrSignInState) -> Unit = {},
    onStart: () -> Unit = {},
    onQrScanResult: (OriginQrSignInAction.OnQrScanResult) -> Unit = {},
    onSuccess: () -> Unit = {}
) {
    val snackbarHostState = remember { ProtonSnackbarHostState() }

    OriginQrSignInEvents(
        effect = effect,
        onBiometricAuthResult = onBiometricAuthResult,
        onFailureScreenView = onFailureScreenView,
        onManualCodeInput = onManualCodeInput,
        onQrScanResult = onQrScanResult,
        onSuccess = onSuccess,
        snackbarHostState = snackbarHostState
    )

    Scaffold(
        modifier = modifier,
        snackbarHost = { ProtonSnackbarHost(snackbarHostState) },
        topBar = {
            ProtonTopAppBar(
                title = { Text(text = stringResource(R.string.intro_origin_sign_in_title)) },
                navigationIcon = { ProtonBackButton(onBack = onNavigateBack) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            when (state) {
                is OriginQrSignInState.MissingCameraPermission -> OriginQrSignInMissingCameraPermission(
                    productName = state.productName,
                    navigateToAppSettings = navigateToAppSettings,
                    onCameraPermissionGranted = onCameraPermissionGranted,
                    onScreenView = { onScreenView(state) },
                    modifier = Modifier
                        .padding(ProtonDimens.DefaultSpacing)
                        .widthIn(max = MAX_CONTENT_WIDTH)
                        .fillMaxHeight()
                        .align(Alignment.Center)
                        .verticalScroll(rememberScrollState())
                )

                is OriginQrSignInState.SignedInSuccessfully,
                is OriginQrSignInState.Verifying -> OriginQrSignInVerifying(
                    onScreenView = { onScreenView(state) },
                    modifier = Modifier.fillMaxSize()
                )

                else -> OriginQrSignInContent(
                    isInteractionDisabled = state.shouldDisableInteraction(),
                    onScreenView = { onScreenView(state) },
                    onStart = onStart,
                    modifier = Modifier
                        .padding(ProtonDimens.MediumSpacing)
                        .widthIn(max = MAX_CONTENT_WIDTH)
                        .fillMaxHeight()
                        .align(Alignment.Center)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
@Suppress("UseComposableActions")
private fun OriginQrSignInEvents(
    effect: Effect<OriginQrSignInEvent>?,
    onBiometricAuthResult: (OriginQrSignInAction.OnBiometricAuthResult) -> Unit,
    onFailureScreenView: () -> Unit,
    onManualCodeInput: () -> Unit,
    onQrScanResult: (OriginQrSignInAction.OnQrScanResult) -> Unit,
    onSuccess: () -> Unit,
    snackbarHostState: ProtonSnackbarHostState
) {
    val biometricsLauncher = rememberBiometricLauncher { result ->
        onBiometricAuthResult(OriginQrSignInAction.OnBiometricAuthResult(result))
    }
    val biometricsTitle = stringResource(R.string.intro_origin_biometrics_title)
    val biometricsCancelButton = stringResource(R.string.presentation_alert_cancel)

    val retryLabel = stringResource(R.string.presentation_retry)
    val qrScanLauncher = rememberQrScanLauncher(QrScanEncoding.default) { result ->
        onQrScanResult(OriginQrSignInAction.OnQrScanResult(result))
    }

    LaunchedEffect(effect) {
        effect?.consume { event ->
            when (event) {
                is OriginQrSignInEvent.ErrorMessage -> {
                    onFailureScreenView()

                    val result = snackbarHostState.showSnackbar(
                        ProtonSnackbarType.ERROR,
                        message = event.message,
                        duration = SnackbarDuration.Long,
                        actionLabel = when {
                            event.onRetry != null -> retryLabel
                            else -> null
                        }
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onRetry?.invoke()
                    }
                }

                is OriginQrSignInEvent.LaunchBiometricsCheck -> biometricsLauncher.launch(
                    title = biometricsTitle,
                    subtitle = null,
                    cancelButton = biometricsCancelButton,
                    authenticatorsResolver = event.resolver
                )

                is OriginQrSignInEvent.LaunchManualCodeInput -> onManualCodeInput()
                is OriginQrSignInEvent.LaunchQrScanner -> qrScanLauncher.launch()
                is OriginQrSignInEvent.SignedInSuccessfully -> onSuccess()
            }
        }
    }
}

@Composable
private fun OriginQrSignInContent(
    isInteractionDisabled: Boolean,
    onScreenView: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchOnScreenView(enqueue = onScreenView)

    val hints = remember {
        arrayOf(
            R.string.intro_origin_sign_in_hint_1,
            R.string.intro_origin_sign_in_hint_2,
            R.string.intro_origin_sign_in_hint_3,
            R.string.intro_origin_sign_in_hint_4
        )
    }
    val tips = remember {
        arrayOf(
            R.string.intro_origin_sign_in_tip_1,
            R.string.intro_origin_sign_in_tip_2,
            R.string.intro_origin_sign_in_tip_3
        )
    }
    Column(
        modifier = modifier
    ) {
        Image(
            painter = painterResource(R.drawable.edm_intro_qr_scan_icon),
            contentDescription = null,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = stringResource(R.string.intro_origin_sign_in_subtitle),
            style = LocalTypography.current.headline,
            color = LocalColors.current.textNorm,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = ProtonDimens.LargerSpacing)
        )

        hints.forEach { hintResId ->
            Text(
                text = annotatedStringResource(hintResId),
                style = LocalTypography.current.body2Regular,
                color = LocalColors.current.textNorm,
                modifier = Modifier.padding(bottom = ProtonDimens.SmallSpacing)
            )
        }

        Spacer(modifier = Modifier.weight(1.0f))

        TipsBox(tips)

        ProtonSolidButton(
            onClick = onStart,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
            contained = false,
            loading = isInteractionDisabled
        ) {
            Text(text = stringResource(R.string.intro_origin_sign_in_begin))
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
@Suppress("UseComposableActions")
private fun OriginQrSignInMissingCameraPermission(
    productName: String,
    navigateToAppSettings: () -> Unit,
    onCameraPermissionGranted: () -> Unit,
    onScreenView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status.isGranted) {
            onCameraPermissionGranted()
        }
    }

    LaunchOnScreenView(enqueue = onScreenView)

    Column(
        modifier = modifier
    ) {
        Image(
            painterResource(R.drawable.edm_missing_camera_permission),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(ProtonDimens.MediumSpacing)
        )
        Text(
            text = stringResource(R.string.edm_missing_camera_permission_headline),
            style = LocalTypography.current.headline,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.DefaultSpacing)
        )
        Text(
            text = stringResource(R.string.edm_missing_camera_permission_body, productName),
            style = LocalTypography.current.body1Regular,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = ProtonDimens.DefaultSpacing)
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Text(
            text = stringResource(R.string.edm_missing_camera_permission_footer),
            style = LocalTypography.current.body2Regular,
            textAlign = TextAlign.Center,
            color = LocalColors.current.textWeak,
            modifier = Modifier.padding(top = ProtonDimens.DefaultSpacing)
        )
        ProtonSolidButton(
            onClick = navigateToAppSettings,
            contained = false,
            modifier = Modifier
                .padding(top = ProtonDimens.DefaultSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.edm_missing_camera_permission_settings),
                    modifier = Modifier.align(Alignment.Center)
                )
                Icon(
                    painter = painterResource(R.drawable.ic_proton_arrow_out_square),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun OriginQrSignInVerifying(onScreenView: () -> Unit, modifier: Modifier = Modifier) {
    LaunchOnScreenView(enqueue = onScreenView)

    Box(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.edm_code_verifying),
            style = LocalTypography.current.headline,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(ProtonDimens.MediumSpacing)
        )
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(R.drawable.edm_qr_square),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.align(Alignment.Center)
            )
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
@Suppress("LongParameterList")
private fun TipsBox(
    tips: Array<Int>,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int = R.drawable.ic_proton_lightbulb,
    bgColor: Color = LocalColors.current.backgroundSecondary,
    textColor: Color = LocalColors.current.textWeak,
    textStyle: TextStyle = LocalTypography.current.overlineRegular.copy(fontSize = 11.sp)
) {
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(ProtonDimens.ExtraLargeCornerRadius))
            .padding(ProtonDimens.DefaultSpacing)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = ProtonDimens.ExtraSmallSpacing)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(ProtonDimens.SmallIconSize)
            )
            Text(
                text = stringResource(R.string.intro_origin_sign_in_tips),
                modifier = Modifier
                    .padding(start = ProtonDimens.SmallSpacing)
                    .align(Alignment.CenterVertically),
                color = textColor,
                style = textStyle,
                fontWeight = FontWeight.SemiBold
            )
        }

        tips.forEach { tipRes ->
            Text(
                text = annotatedStringResource(tipRes),
                modifier = Modifier.padding(top = ProtonDimens.SmallSpacing),
                color = textColor,
                style = textStyle
            )
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = Devices.PIXEL_FOLD)
private fun OriginQrSignInScreenPreview() {
    ProtonTheme {
        OriginQrSignInScreen(
            state = OriginQrSignInState.Idle,
            effect = null
        )
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = Devices.PIXEL_FOLD)
private fun OriginQrSignInNoCameraPermissionPreview() {
    ProtonTheme {
        OriginQrSignInScreen(
            state = OriginQrSignInState.MissingCameraPermission("Proton Mail"),
            effect = null
        )
    }
}


@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = Devices.PIXEL_FOLD)
private fun OriginQrSignInVerifyingScreenPreview() {
    ProtonTheme {
        OriginQrSignInScreen(
            state = OriginQrSignInState.Verifying,
            effect = null
        )
    }
}
