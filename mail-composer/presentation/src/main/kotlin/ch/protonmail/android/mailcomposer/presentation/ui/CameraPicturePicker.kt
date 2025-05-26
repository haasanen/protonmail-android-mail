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

package ch.protonmail.android.mailcomposer.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.viewmodel.CameraPicturePickerViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import timber.log.Timber


@Composable
fun CameraPicturePicker(
    openCameraEffect: Effect<Unit>,
    onCaptured: (Uri) -> Unit,
    onError: (String) -> Unit,
    viewModel: CameraPicturePickerViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    when (val current = state.value) {
        is CameraPicturePickerViewModel.State.Initial -> Unit

        is CameraPicturePickerViewModel.State.FileInfo -> CameraPicturePicker(
            current.fileUri,
            onCaptured
        )

        is CameraPicturePickerViewModel.State.CheckPermissions -> CheckCameraPermissions(
            onPermissionGranted = { viewModel.onPermissionGranted() }
        )

        is CameraPicturePickerViewModel.State.Error.FileError ->
            onError(stringResource(R.string.composer_attach_capture_from_camera_error))
    }

    ConsumableLaunchedEffect(effect = openCameraEffect) {
        viewModel.onCapturePictureRequested()
    }

}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("PermissionLaunchedDuringComposition")
fun CheckCameraPermissions(onPermissionGranted: () -> Unit) {
    val cameraPermission = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermission.status.isGranted) {
        if (cameraPermission.status.isGranted) {
            onPermissionGranted()
        } else {
            cameraPermission.launchPermissionRequest()
        }
    }
}

@Composable
fun CameraPicturePicker(fileUri: Uri, onCaptured: (Uri) -> Unit) {

    val cameraIntent = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            Timber.v("image from camera, success: $success")
            if (success) {
                onCaptured(fileUri)
            }
        }
    )

    LaunchedEffect(fileUri) {
        cameraIntent.launch(fileUri)
    }

}
