/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailsettings.presentation.settings.toolbar.ui

// To be re-enabled once ET-4241 is complete.

// @Composable
// fun CustomizeToolbarEditScreen(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
//    val viewModel = hiltViewModel<CustomizeToolbarEditViewModel>()
//    val state by viewModel.state.collectAsStateWithLifecycle()
//
//    CustomizeToolbarEditScreen(
//        state,
//        onAction = viewModel::submit,
//        onBackClick = onBackClick,
//        modifier = modifier
//    )
// }
//
// @OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
// @Composable
// private fun CustomizeToolbarEditScreen(
//    state: CustomizeToolbarEditState,
//    onAction: (CustomizeToolbarEditOperation) -> Unit,
//    onBackClick: () -> Unit,
//    modifier: Modifier = Modifier
// ) {
//    val snackbarHostState = remember { ProtonSnackbarHostState() }
//
//    val errorText = stringResource(R.string.mail_settings_custom_toolbar_save_error)
//
//    val actions = ToolbarActions.UiActions(
//        onOperation = onAction,
//        onClose = onBackClick,
//        onError = {
//            snackbarHostState.showSnackbar(type = ProtonSnackbarType.ERROR, errorText)
//        }
//    )
//
//    Scaffold(
//        modifier = modifier.fillMaxSize(),
//        topBar = { EditToolbarTopBar(onBackClick, state, onAction) },
//        snackbarHost = { ProtonSnackbarHost(snackbarHostState) },
//        content = { paddingValues ->
//            when (state) {
//                is CustomizeToolbarEditState.Data -> ToolbarActions(
//                    state = state,
//                    disclaimer = TextUiModel(R.string.mail_settings_custom_toolbar_header),
//                    actions = actions,
//                    modifier = Modifier.padding(paddingValues)
//                )
//
//                CustomizeToolbarEditState.Loading -> LoadingIndicator()
//                CustomizeToolbarEditState.Error -> CustomizeToolbarEditScreenError(onBackClick)
//            }
//        }
//    )
// }
//
// @Composable
// private fun CustomizeToolbarEditScreenError(onBack: () -> Unit) {
//    val context = LocalContext.current
//
//    val errorMessage = stringResource(R.string.mail_settings_custom_toolbar_read_error)
//    LaunchedEffect(Unit) {
//        context.showToast(errorMessage)
//    }
//
//    onBack()
// }

object CustomizeToolbarEditScreen {

    const val OpenMode = "CustomizeEditScreenMode"
}
//
// @Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
// @Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
// @Composable
// private fun CustomizeToolbarContentPreview(
//    @PreviewParameter(CustomizeToolbarPreviewProvider::class) preview: CustomizeToolbarPreview
// ) {
//    ProtonTheme {
//        CustomizeToolbarEditScreen(
//            state = preview.uiModel,
//            modifier = Modifier,
//            onAction = {},
//            onBackClick = {}
//        )
//    }
// }
