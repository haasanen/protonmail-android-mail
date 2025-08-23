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

package ch.protonmail.android.mailbugreport.presentation.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonSnackbarHostState
import ch.protonmail.android.design.compose.component.ProtonSnackbarType
import ch.protonmail.android.design.compose.component.appbar.ProtonMediumTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailbugreport.presentation.R
import ch.protonmail.android.mailbugreport.presentation.viewmodel.report.BugReportViewModel
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.uicomponents.snackbar.DismissableSnackbarHost

@Composable
fun BugReportScreen(onBack: () -> Unit, onSuccess: (String) -> Unit) {
    BugReportScreenImpl(onBack, onSuccess)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BugReportScreenImpl(
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    viewModel: BugReportViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { ProtonSnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val states by viewModel.states.collectAsStateWithLifecycle()
    var shouldIncludeLogs by remember { mutableStateOf(true) }
    var shouldShowExitDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val actions = BugReportScreen.Actions(
        onBack = { viewModel.close() },
        onSubmit = { includeLogs -> viewModel.submit(includeLogs) }
    )

    ConsumableTextEffect(states.effects.closeWithMessage) {
        onSuccess(it)
    }

    ConsumableLaunchedEffect(states.effects.close) {
        onBack()
    }

    ConsumableLaunchedEffect(states.effects.closeWithPendingData) {
        shouldShowExitDialog = true
    }

    ConsumableTextEffect(states.effects.submissionError) {
        snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, it)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ProtonMediumTopAppBar(
                title = { Text(text = stringResource(R.string.report_a_problem_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_proton_close_filled),
                            contentDescription = stringResource(id = R.string.presentation_close)
                        )
                    }
                },
                actions = {
                    SubmitButtonWithLoader(
                        isLoading = states.main.isLoading,
                        onClick = {
                            keyboardController?.hide()
                            actions.onSubmit(shouldIncludeLogs)
                        }
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { DismissableSnackbarHost(protonSnackbarHostState = snackbarHostState) },
        content = { paddingValues ->
            BugReportScreenContent(
                fields = states.main.fields,
                includeLogs = shouldIncludeLogs,
                onLogsToggled = { shouldIncludeLogs = it },
                validationErrors = states.main.validationErrors,
                forceFocusRequest = states.effects.forceFocusField,
                modifier = Modifier.padding(paddingValues)
            )
        }
    )

    if (shouldShowExitDialog) {
        ExitConfirmationDialog(
            onConfirmClicked = {
                shouldShowExitDialog = false
                onBack()
            },
            onDismissClicked = { shouldShowExitDialog = false }
        )
    }
}

internal object BugReportScreen {
    data class Actions(
        val onBack: () -> Unit,
        val onSubmit: (includesLogs: Boolean) -> Unit
    )
}

@Preview
@Composable
private fun BugReportPreview() {
    ProtonTheme {
        Surface(modifier = Modifier.background(Color.White)) {
            BugReportScreen(onBack = {}, onSuccess = {})
        }
    }
}
