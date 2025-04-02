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

import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonModalBottomSheetLayout
import ch.protonmail.android.design.compose.component.ProtonSnackbarHostState
import ch.protonmail.android.design.compose.component.ProtonSnackbarType
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.AdaptivePreviews
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.compose.toDp
import ch.protonmail.android.mailcommon.presentation.compose.toPx
import ch.protonmail.android.mailcommon.presentation.ui.CommonTestTags
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ComposeScreenMeasures
import ch.protonmail.android.mailcomposer.presentation.model.ComposerAction
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsStateManager
import ch.protonmail.android.mailcomposer.presentation.model.WebViewMeasures
import ch.protonmail.android.mailcomposer.presentation.ui.form.ComposerForm
import ch.protonmail.android.mailcomposer.presentation.ui.form.ComposerForm2
import ch.protonmail.android.mailcomposer.presentation.viewmodel.ComposerViewModel
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.uicomponents.bottomsheet.bottomSheetHeightConstrainedContent
import ch.protonmail.android.uicomponents.dismissKeyboard
import ch.protonmail.android.uicomponents.snackbar.DismissableSnackbarHost
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Suppress("UseComposableActions")
@Composable
fun ComposerScreen(actions: ComposerScreen.Actions) {
    val context = LocalContext.current
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val recipientsStateManager = remember { RecipientsStateManager() }
    val viewModel = hiltViewModel<ComposerViewModel, ComposerViewModel.Factory> { factory ->
        factory.create(recipientsStateManager)
    }
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { ProtonSnackbarHostState() }
    val bottomSheetType = rememberSaveable { mutableStateOf(BottomSheetType.ChangeSender) }
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val attachmentSizeDialogState = remember { mutableStateOf(false) }
    val sendingErrorDialogState = remember { mutableStateOf<String?>(null) }
    val senderChangedNoticeDialogState = remember { mutableStateOf<String?>(null) }
    val sendWithoutSubjectDialogState = remember { mutableStateOf(false) }
    val sendExpiringMessageDialogState = remember {
        mutableStateOf(SendExpiringMessageDialogState(false, emptyList()))
    }

    val featureMissingSnackbarMessage = stringResource(id = R.string.feature_coming_soon)
    val scope = rememberCoroutineScope()
    fun showFeatureMissingSnackbar() = scope.launch {
        snackbarHostState.showSnackbar(
            message = featureMissingSnackbarMessage,
            type = ProtonSnackbarType.NORM
        )
    }

    val bottomBarActions = ComposerBottomBar.Actions(
        onAddAttachmentsClick = {
            viewModel.submit(ComposerAction.OnAddAttachments)
        },
        onSetMessagePasswordClick = { _, _ ->
            showFeatureMissingSnackbar()
            // actions.onSetMessagePasswordClick()
        },
        onSetExpirationTimeClick = {
            // bottomSheetType.value = BottomSheetType.SetExpirationTime
            // viewModel.submit(ComposerAction.OnSetExpirationTimeRequested)
            showFeatureMissingSnackbar()
        },
        onDiscardDraftClicked = { showFeatureMissingSnackbar() }
    )

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            viewModel.submit(ComposerAction.AttachmentsAdded(uris))
        }
    )

    ConsumableLaunchedEffect(effect = state.openImagePicker) {
        imagePicker.launch("*/*")
    }

    ProtonModalBottomSheetLayout(
        showBottomSheet = showBottomSheet,
        onDismissed = { showBottomSheet = false },
        dismissOnBack = true,
        sheetContent = bottomSheetHeightConstrainedContent {
            when (bottomSheetType.value) {
                BottomSheetType.ChangeSender -> ChangeSenderBottomSheetContent(
                    state.senderAddresses,
                    { sender -> viewModel.submit(ComposerAction.SenderChanged(sender)) }
                )

                BottomSheetType.SetExpirationTime -> SetExpirationTimeBottomSheetContent(
                    expirationTime = state.messageExpiresIn,
                    onDoneClick = { viewModel.submit(ComposerAction.ExpirationTimeSet(it)) }
                )
            }
        },
        sheetState = bottomSheetState
    ) {

        Scaffold(
            modifier = Modifier.testTag(ComposerTestTags.RootItem),
            topBar = {
                ComposerTopBar(
                    onCloseComposerClick = {
                        viewModel.submit(ComposerAction.OnCloseComposer)
                    },
                    onSendMessageComposerClick = {
                        viewModel.submit(ComposerAction.OnSendMessage)
                    },
                    isSendMessageButtonEnabled = state.isSubmittable
                )
            },
            bottomBar = {
                ComposerBottomBar(
                    draftId = state.fields.draftId,
                    senderEmail = SenderEmail(state.fields.sender.email),
                    isMessagePasswordSet = state.isMessagePasswordSet,
                    isMessageExpirationTimeSet = state.messageExpiresIn != Duration.ZERO,
                    actions = bottomBarActions
                )
            },
            snackbarHost = {
                DismissableSnackbarHost(
                    modifier = Modifier.testTag(CommonTestTags.SnackbarHost),
                    protonSnackbarHostState = snackbarHostState
                )
            }
        ) { paddingValues ->
            if (state.isLoading) {
                @Suppress("MagicNumber")
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(.5f)
                ) { ProtonCenteredProgress() }
            } else {

                val localDensity = LocalDensity.current
                val coroutineScope = rememberCoroutineScope()
                val scrollState = rememberScrollState()
                var columnBounds by remember { mutableStateOf(Rect.Zero) }
                var visibleHeaderHeight by remember { mutableStateOf(0.dp) }
                var visibleWebViewHeight by remember { mutableStateOf(0.dp) }
                var headerHeight by remember { mutableStateOf(0.dp) }

                val scrollManager = remember {
                    EditorScrollManager(
                        onUpdateScroll = { coroutineScope.launch { scrollState.scrollTo(it.toPx(localDensity)) } }
                    )
                }

                fun getComposeScreenParams() = ComposeScreenMeasures(
                    visibleWebViewHeight,
                    visibleHeaderHeight,
                    headerHeight,
                    scrollState.value.toDp(localDensity)
                )

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .onGloballyPositioned { coordinates ->
                            columnBounds = coordinates.boundsInWindow()
                        }
                ) {
                    // Not showing the form till we're done loading ensure it does receive the
                    // right "initial values" from state when displayed
                    ComposerForm(
                        modifier = Modifier.testTag(ComposerTestTags.ComposerForm),
                        changeFocusToField = state.changeFocusToField,
                        actions = buildActions(
                            viewModel = viewModel,
                            onWebViewMeasuresChanged = { webViewParams ->
                                scrollManager.onEditorParamsChanged(
                                    getComposeScreenParams(),
                                    webViewParams
                                )
                            },
                            onHeaderPositioned = { headerBoundsInWindow, measuredHeight ->
                                val visibleBounds = headerBoundsInWindow.intersect(columnBounds)
                                visibleHeaderHeight = visibleBounds.height.coerceAtLeast(0f).toDp(localDensity)
                                headerHeight = measuredHeight.toDp(localDensity)
                            },
                            onWebViewPositioned = { boundsInWindow ->
                                val visibleBounds = boundsInWindow.intersect(columnBounds)
                                visibleWebViewHeight = visibleBounds.height.coerceAtLeast(0f).toDp(localDensity)
                            },
                            showFeatureMissingSnackbar = { showFeatureMissingSnackbar() }
                        ),
                        senderEmail = state.fields.sender.email,
                        recipientsStateManager = recipientsStateManager,
                        subjectTextField = viewModel.subjectTextField,
                        bodyInitialValue = state.fields.displayBody,
                        focusTextBody = Effect.empty()
                    )
                }
            }
        }
    }

    if (sendWithoutSubjectDialogState.value) {

        SendingWithEmptySubjectDialog(
            onConfirmClicked = {
                viewModel.submit(ComposerAction.ConfirmSendingWithoutSubject)
                sendWithoutSubjectDialogState.value = false
            },
            onDismissClicked = {
                viewModel.submit(ComposerAction.RejectSendingWithoutSubject)
                sendWithoutSubjectDialogState.value = false
            }
        )
    }

    if (sendExpiringMessageDialogState.value.isVisible) {
        SendExpiringMessageDialog(
            externalRecipients = sendExpiringMessageDialogState.value.externalParticipants,
            onConfirmClicked = {
                viewModel.submit(ComposerAction.SendExpiringMessageToExternalRecipientsConfirmed)
                sendExpiringMessageDialogState.value = sendExpiringMessageDialogState.value.copy(isVisible = false)
            },
            onDismissClicked = {
                sendExpiringMessageDialogState.value = sendExpiringMessageDialogState.value.copy(isVisible = false)
            }
        )
    }

    if (attachmentSizeDialogState.value) {
        ProtonAlertDialog(
            onDismissRequest = { attachmentSizeDialogState.value = false },
            confirmButton = {
                ProtonAlertDialogButton(R.string.composer_attachment_size_exceeded_dialog_confirm_button) {
                    attachmentSizeDialogState.value = false
                }
            },
            title = stringResource(id = R.string.composer_attachment_size_exceeded_dialog_title),
            text = {
                ProtonAlertDialogText(
                    stringResource(
                        id = R.string.composer_attachment_size_exceeded_dialog_message,
                        Formatter.formatShortFileSize(
                            LocalContext.current,
                            ComposerScreen.MAX_ATTACHMENTS_SIZE
                        )
                    )
                )
            }
        )
    }

    senderChangedNoticeDialogState.value?.run {
        ProtonAlertDialog(
            onDismissRequest = { senderChangedNoticeDialogState.value = null },
            confirmButton = {
                ProtonAlertDialogButton(R.string.composer_sender_changed_dialog_confirm_button) {
                    senderChangedNoticeDialogState.value = null
                }
            },
            title = stringResource(id = R.string.composer_sender_changed_dialog_title),
            text = { ProtonAlertDialogText(this) }
        )
    }

    sendingErrorDialogState.value?.run {
        SendingErrorDialog(
            errorMessage = this,
            onDismissClicked = {
                sendingErrorDialogState.value = null
                viewModel.clearSendingError()
            }
        )
    }

    ConsumableTextEffect(effect = state.recipientValidationError) { error ->
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

    ConsumableTextEffect(effect = state.premiumFeatureMessage) { message ->
        snackbarHostState.showSnackbar(type = ProtonSnackbarType.NORM, message = message)
    }

    ConsumableTextEffect(effect = state.error) { error ->
        snackbarHostState.showSnackbar(type = ProtonSnackbarType.ERROR, message = error)
    }

    ConsumableTextEffect(effect = state.warning) { warning ->
        snackbarHostState.showSnackbar(type = ProtonSnackbarType.WARNING, message = warning)
    }

    val errorAttachmentReEncryption = stringResource(id = R.string.composer_attachment_reencryption_failed_message)
    ConsumableLaunchedEffect(effect = state.attachmentsReEncryptionFailed) {
        snackbarHostState.showSnackbar(type = ProtonSnackbarType.ERROR, message = errorAttachmentReEncryption)
    }

    ConsumableLaunchedEffect(effect = state.changeBottomSheetVisibility) { show ->
        if (show) {
            dismissKeyboard(context, view, keyboardController)
            bottomSheetState.show()
        } else {
            bottomSheetState.hide()
        }

        showBottomSheet = show
    }

    ConsumableLaunchedEffect(effect = state.closeComposer) {
        dismissKeyboard(context, view, keyboardController)
        actions.onCloseComposerClick()
    }

    ConsumableLaunchedEffect(effect = state.closeComposerWithDraftSaved) {
        dismissKeyboard(context, view, keyboardController)
        actions.onCloseComposerClick()
        actions.showDraftSavedSnackbar(state.fields.draftId)
    }

    ConsumableLaunchedEffect(effect = state.closeComposerWithMessageSending) {
        dismissKeyboard(context, view, keyboardController)
        actions.onCloseComposerClick()
        actions.showMessageSendingSnackbar()
    }

    ConsumableLaunchedEffect(effect = state.closeComposerWithMessageSendingOffline) {
        dismissKeyboard(context, view, keyboardController)
        actions.onCloseComposerClick()
        actions.showMessageSendingOfflineSnackbar()
    }

    ConsumableTextEffect(effect = state.sendingErrorEffect) {
        sendingErrorDialogState.value = it
    }

    ConsumableTextEffect(effect = state.senderChangedNotice) {
        senderChangedNoticeDialogState.value = it
    }

    ConsumableLaunchedEffect(effect = state.attachmentsFileSizeExceeded) { attachmentSizeDialogState.value = true }

    ConsumableLaunchedEffect(effect = state.confirmSendingWithoutSubject) {
        sendWithoutSubjectDialogState.value = true
    }

    ConsumableLaunchedEffect(effect = state.confirmSendExpiringMessage) {
        sendExpiringMessageDialogState.value = SendExpiringMessageDialogState(
            isVisible = true, externalParticipants = it
        )
    }

    BackHandler(true) {
        viewModel.submit(ComposerAction.OnCloseComposer)
    }

}

@Suppress("LongParameterList")
private fun buildActions(
    viewModel: ComposerViewModel,
    onWebViewMeasuresChanged: (WebViewMeasures) -> Unit,
    onHeaderPositioned: (Rect, Float) -> Unit,
    onWebViewPositioned: (Rect) -> Unit,
    showFeatureMissingSnackbar: () -> Unit
): ComposerForm2.Actions = ComposerForm2.Actions(
    onBodyChanged = {
        viewModel.submit(ComposerAction.DraftBodyChanged(DraftBody(it)))
    },
    onChangeSender = {
        showFeatureMissingSnackbar()
        // setBottomSheetType(BottomSheetType.ChangeSender)
        // viewModel.submit(ComposerAction.ChangeSenderRequested)
    },
    onWebViewMeasuresChanged = onWebViewMeasuresChanged,
    onHeaderPositioned = onHeaderPositioned,
    onWebViewPositioned = onWebViewPositioned,
    onAttachmentRemoveRequested = { viewModel.submit(ComposerAction.RemoveAttachment(it)) }
)

object ComposerScreen {

    const val MAX_ATTACHMENTS_SIZE = 25 * 1000 * 1000L

    const val DraftMessageIdKey = "draft_message_id"
    const val SerializedDraftActionKey = "serialized_draft_action_key"
    const val DraftActionForShareKey = "draft_action_for_share_key"

    data class Actions(
        val onCloseComposerClick: () -> Unit,
        val onSetMessagePasswordClick: (MessageId, SenderEmail) -> Unit,
        val showDraftSavedSnackbar: (MessageId) -> Unit,
        val showMessageSendingSnackbar: () -> Unit,
        val showMessageSendingOfflineSnackbar: () -> Unit
    ) {

        companion object {

            val Empty = Actions(

                onCloseComposerClick = {},
                onSetMessagePasswordClick = { _, _ -> },
                showDraftSavedSnackbar = {},
                showMessageSendingSnackbar = {},
                showMessageSendingOfflineSnackbar = {}
            )
        }
    }
}

private enum class BottomSheetType { ChangeSender, SetExpirationTime }

private data class SendExpiringMessageDialogState(
    val isVisible: Boolean,
    val externalParticipants: List<Participant>
)

@Composable
@AdaptivePreviews
private fun MessageDetailScreenPreview() {
    ProtonTheme {
        ComposerScreen(ComposerScreen.Actions.Empty)
    }
}
