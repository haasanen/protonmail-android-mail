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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.mapSaver
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
import ch.protonmail.android.mailcommon.presentation.compose.toDp
import ch.protonmail.android.mailcommon.presentation.compose.toPx
import ch.protonmail.android.mailcommon.presentation.ui.CommonTestTags
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.SenderEmail
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ComposeScreenMeasures
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState
import ch.protonmail.android.mailcomposer.presentation.model.RecipientsStateManager
import ch.protonmail.android.mailcomposer.presentation.model.WebViewMeasures
import ch.protonmail.android.mailcomposer.presentation.model.operations.ComposerAction2
import ch.protonmail.android.mailcomposer.presentation.ui.form.ComposerForm
import ch.protonmail.android.mailcomposer.presentation.viewmodel.ComposerViewModel
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.Participant
import ch.protonmail.android.uicomponents.bottomsheet.bottomSheetHeightConstrainedContent
import ch.protonmail.android.uicomponents.dismissKeyboard
import ch.protonmail.android.uicomponents.snackbar.DismissableSnackbarHost
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
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
    val composerStates by viewModel.composerStates.collectAsState()
    val mainState = composerStates.main
    val attachmentsState = composerStates.attachments
    val accessoriesState = composerStates.accessories
    val effectsState = composerStates.effects
    val isChooseAttachmentSourceEnabled by viewModel.isChooseAttachmentSourceEnabled.collectAsState()
    val isScheduleSendEnabled by viewModel.isScheduleSendEnabled.collectAsState()

    val snackbarHostState = remember { ProtonSnackbarHostState() }
    val bottomSheetType = rememberSaveable(stateSaver = BottomSheetType.Saver) {
        mutableStateOf(BottomSheetType.ChangeSender)
    }
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val attachmentSizeDialogState = remember { mutableStateOf(false) }
    val sendingErrorDialogState = remember { mutableStateOf<String?>(null) }
    val senderChangedNoticeDialogState = remember { mutableStateOf<String?>(null) }
    val sendWithoutSubjectDialogState = remember { mutableStateOf(false) }
    val sendExpiringMessageDialogState = remember {
        mutableStateOf(SendExpiringMessageDialogState(false, emptyList()))
    }
    val discardDraftDialogState = remember { mutableStateOf(false) }

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
            bottomSheetType.value = BottomSheetType.AttachmentSources
            viewModel.submit(ComposerAction2.AddAttachmentsRequested)
        },
        onSetMessagePasswordClick = {
            showFeatureMissingSnackbar()
            // actions.onSetMessagePasswordClick()
        },
        onSetExpirationTimeClick = {
            // bottomSheetType.value = BottomSheetType.SetExpirationTime
            // viewModel.submit(ComposerAction2.OnSetExpirationTimeRequested)
            showFeatureMissingSnackbar()
        },
        onDiscardDraftClicked = { viewModel.submit(ComposerAction2.DiscardDraftRequested) }
    )

    val filesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            viewModel.submit(ComposerAction2.AddFileAttachments(uris))
        }
    )

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            viewModel.submit(ComposerAction2.AddAttachments(uris))
        }
    )

    ConsumableLaunchedEffect(effect = effectsState.openFilesPicker) {
        filesPicker.launch("*/*")
    }

    ConsumableLaunchedEffect(effect = effectsState.openPhotosPicker) {
        mediaPicker.launch(PickVisualMediaRequest())
    }

    CameraPicturePicker(
        effectsState.openCamera,
        onCaptured = { uri ->
            Timber.v("camera: image from take picture composable, uri: $uri")
            viewModel.submit(ComposerAction2.AddAttachments(listOf(uri)))
        },
        onError = { localisedError ->
            scope.launch {
                snackbarHostState.showSnackbar(type = ProtonSnackbarType.ERROR, message = localisedError)
            }
        }
    )

    ProtonModalBottomSheetLayout(
        showBottomSheet = showBottomSheet,
        onDismissed = { showBottomSheet = false },
        dismissOnBack = true,
        sheetContent = bottomSheetHeightConstrainedContent {
            when (val sheetType = bottomSheetType.value) {
                BottomSheetType.ChangeSender -> ChangeSenderBottomSheetContent(
                    mainState.senderAddresses,
                    { sender -> viewModel.submit(ComposerAction2.SetSenderAddress(sender)) }
                )

                BottomSheetType.SetExpirationTime -> SetExpirationTimeBottomSheetContent(
                    expirationTime = accessoriesState.messageExpiresIn,
                    onDoneClick = { viewModel.submit(ComposerAction2.SetMessageExpiration(it)) }
                )

                is BottomSheetType.InlineImageActions -> InlineImageActionsBottomSheetContent(
                    contentId = sheetType.contentId,
                    onTransformToAttachment = {
                        Toast.makeText(context, featureMissingSnackbarMessage, Toast.LENGTH_SHORT).show()
                    },
                    onRemove = { viewModel.submit(ComposerAction2.RemoveInlineAttachment(it)) }
                )

                is BottomSheetType.AttachmentSources -> AttachmentSourceBottomSheetContent(
                    isChooseAttachmentSourceEnabled = isChooseAttachmentSourceEnabled,
                    onCamera = { viewModel.submit(ComposerAction2.OpenCameraPicker) },
                    onFiles = { viewModel.submit(ComposerAction2.OpenFilePicker) },
                    onPhotos = { viewModel.submit(ComposerAction2.OpenPhotosPicker) }
                )

                is BottomSheetType.ScheduleSendOptions -> ScheduleSendBottomSheetContent(
                    optionsUiModel = state.scheduleSendOptions,
                    onScheduleSendConfirmed = {
                        Timber.d("Schedule send confirmed, to happen at $it")
                    }
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
                        viewModel.submit(ComposerAction2.CloseComposer)
                    },
                    onSendMessageComposerClick = {
                        viewModel.submit(ComposerAction2.SendMessage)
                    },
                    onScheduleSendClick = {
                        bottomSheetType.value = BottomSheetType.ScheduleSendOptions
                        viewModel.submit(ComposerAction.OnScheduleSendRequested)
                    },
                    isSendMessageEnabled = mainState.isSubmittable,
                    isScheduleSendFeatureFlagEnabled = isScheduleSendEnabled
                )
            },
            bottomBar = {
                ComposerBottomBar(
                    isMessagePasswordSet = accessoriesState.isMessagePasswordSet,
                    isMessageExpirationTimeSet = accessoriesState.messageExpiresIn != Duration.ZERO,
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
            if (mainState.loadingType == ComposerState.LoadingType.Initial) {
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

                var formHeightPx by remember { mutableStateOf(0f) }


                if (mainState.loadingType == ComposerState.LoadingType.Save) {
                    LoadingIndicator(preventBackNavigation = true)
                }

                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .onGloballyPositioned { coordinates ->
                            columnBounds = coordinates.boundsInWindow()
                            formHeightPx = columnBounds.height
                        }
                ) {
                    // Not showing the form till we're done loading ensure it does receive the
                    // right "initial values" from state when displayed
                    ComposerForm(
                        modifier = Modifier.testTag(ComposerTestTags.ComposerForm),
                        changeFocusToField = effectsState.changeFocusToField,
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
                            onLoadEmbeddedImage = { contentId -> viewModel.loadEmbeddedImage(contentId) },
                            showFeatureMissingSnackbar = { showFeatureMissingSnackbar() },
                            onInlineImageClicked = { contentId ->
                                bottomSheetType.value = BottomSheetType.InlineImageActions(contentId)
                                viewModel.submit(ComposerAction2.InlineImageActionsRequested)
                            }
                        ),
                        senderEmail = mainState.fields.sender.email,
                        recipientsStateManager = recipientsStateManager,
                        subjectTextField = viewModel.subjectTextField,
                        bodyInitialValue = mainState.fields.displayBody,
                        attachments = attachmentsState.uiModel,
                        focusTextBody = effectsState.focusTextBody,
                        formHeightPx = formHeightPx,
                        injectInlineAttachment = effectsState.injectInlineAttachment,
                        stripInlineAttachment = effectsState.stripInlineAttachment
                    )
                }
            }
        }
    }

    if (sendWithoutSubjectDialogState.value) {

        SendingWithEmptySubjectDialog(
            onConfirmClicked = {
                viewModel.submit(ComposerAction2.ConfirmSendWithNoSubject)
                sendWithoutSubjectDialogState.value = false
            },
            onDismissClicked = {
                viewModel.submit(ComposerAction2.CancelSendWithNoSubject)
                sendWithoutSubjectDialogState.value = false
            }
        )
    }

    if (sendExpiringMessageDialogState.value.isVisible) {
        SendExpiringMessageDialog(
            externalRecipients = sendExpiringMessageDialogState.value.externalParticipants,
            onConfirmClicked = {
                viewModel.submit(ComposerAction2.ConfirmSendExpirationSetToExternal)
                sendExpiringMessageDialogState.value = sendExpiringMessageDialogState.value.copy(isVisible = false)
            },
            onDismissClicked = {
                sendExpiringMessageDialogState.value = sendExpiringMessageDialogState.value.copy(isVisible = false)
            }
        )
    }

    if (discardDraftDialogState.value) {
        ProtonAlertDialog(
            titleResId = R.string.discard_draft_dialog_title,
            text = {
                Text(text = stringResource(id = R.string.discard_draft_dialog_text))
            },
            dismissButton = {
                ProtonAlertDialogButton(
                    titleResId = R.string.discard_draft_dialog_dismiss_button
                ) { discardDraftDialogState.value = false }
            },
            confirmButton = {
                ProtonAlertDialogButton(
                    titleResId = R.string.discard_draft_dialog_confirm_button
                ) {
                    viewModel.submit(ComposerAction2.DiscardDraftConfirmed)
                    discardDraftDialogState.value = false
                    actions.showDraftDiscardedSnackbar()
                }
            },
            onDismissRequest = { discardDraftDialogState.value = false }
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
//                viewModel.clearSendingError()
            }
        )
    }

    ConsumableTextEffect(effect = effectsState.premiumFeatureMessage) { message ->
        snackbarHostState.showSnackbar(type = ProtonSnackbarType.NORM, message = message)
    }

    ConsumableTextEffect(effect = effectsState.error) { error ->
        snackbarHostState.showSnackbar(type = ProtonSnackbarType.ERROR, message = error)
    }

    ConsumableTextEffect(effect = effectsState.warning) { warning ->
        snackbarHostState.showSnackbar(type = ProtonSnackbarType.WARNING, message = warning)
    }

    val errorAttachmentEncryption = stringResource(id = R.string.composer_attachment_encryption_failed_message)
    ConsumableLaunchedEffect(effect = effectsState.attachmentsEncryptionFailed) {
        snackbarHostState.showSnackbar(type = ProtonSnackbarType.ERROR, message = errorAttachmentEncryption)
    }

    ConsumableLaunchedEffect(effect = effectsState.changeBottomSheetVisibility) { show ->
        if (show) {
            dismissKeyboard(context, view, keyboardController)
            bottomSheetState.show()
        } else {
            bottomSheetState.hide()
        }

        showBottomSheet = show
    }

    ConsumableLaunchedEffect(effect = effectsState.closeComposer) {
        dismissKeyboard(context, view, keyboardController)
        actions.onCloseComposerClick()
    }

    ConsumableLaunchedEffect(effect = effectsState.closeComposerWithDraftSaved) {
        dismissKeyboard(context, view, keyboardController)
        actions.onCloseComposerClick()
        actions.showDraftSavedSnackbar(it)
    }

    ConsumableLaunchedEffect(effect = effectsState.closeComposerWithMessageSending) {
        dismissKeyboard(context, view, keyboardController)
        actions.onCloseComposerClick()
        actions.showMessageSendingSnackbar()
    }

    ConsumableLaunchedEffect(effect = effectsState.closeComposerWithMessageSendingOffline) {
        dismissKeyboard(context, view, keyboardController)
        actions.onCloseComposerClick()
        actions.showMessageSendingOfflineSnackbar()
    }

    ConsumableTextEffect(effect = effectsState.sendingErrorEffect) {
        sendingErrorDialogState.value = it
    }

    ConsumableTextEffect(effect = effectsState.senderChangedNotice) {
        senderChangedNoticeDialogState.value = it
    }

    ConsumableLaunchedEffect(effect = effectsState.attachmentsFileSizeExceeded) {
        attachmentSizeDialogState.value = true
    }

    ConsumableLaunchedEffect(effect = effectsState.confirmSendingWithoutSubject) {
        sendWithoutSubjectDialogState.value = true
    }

    ConsumableLaunchedEffect(effect = effectsState.confirmSendExpiringMessage) {
        sendExpiringMessageDialogState.value = SendExpiringMessageDialogState(
            isVisible = true, externalParticipants = it
        )
    }

    ConsumableLaunchedEffect(effect = effectsState.confirmDiscardDraft) {
        discardDraftDialogState.value = true
    }

    BackHandler(true) {
        viewModel.submit(ComposerAction2.CloseComposer)
    }

}

@Suppress("LongParameterList")
private fun buildActions(
    viewModel: ComposerViewModel,
    onWebViewMeasuresChanged: (WebViewMeasures) -> Unit,
    onHeaderPositioned: (Rect, Float) -> Unit,
    onWebViewPositioned: (Rect) -> Unit,
    onLoadEmbeddedImage: (String) -> EmbeddedImage?,
    showFeatureMissingSnackbar: () -> Unit,
    onInlineImageClicked: (String) -> Unit
): ComposerForm.Actions = ComposerForm.Actions(
    onBodyChanged = {
        viewModel.submit(ComposerAction2.DraftBodyChanged(DraftBody(it)))
    },
    onChangeSender = {
        showFeatureMissingSnackbar()
        // setBottomSheetType(BottomSheetType.ChangeSender)
        // viewModel.submit(ComposerAction2.ChangeSenderRequested)
    },
    onWebViewMeasuresChanged = onWebViewMeasuresChanged,
    onHeaderPositioned = onHeaderPositioned,
    onWebViewPositioned = onWebViewPositioned,
    loadEmbeddedImage = onLoadEmbeddedImage,
    onAttachmentRemoveRequested = { viewModel.submit(ComposerAction2.RemoveAttachment(it)) },
    onInlineImageRemoved = { viewModel.submit(ComposerAction2.RemoveInlineAttachment(it)) },
    onInlineImageClicked = onInlineImageClicked
)

object ComposerScreen {

    const val MAX_ATTACHMENTS_SIZE = 25 * 1000 * 1000L

    const val DraftMessageIdKey = "draft_message_id"
    const val SerializedDraftActionKey = "serialized_draft_action_key"
    const val DraftActionForShareKey = "draft_action_for_share_key"
    const val HasSavedDraftKey = "draft_action_for_saved_draft_key"

    data class Actions(
        val onCloseComposerClick: () -> Unit,
        val onSetMessagePasswordClick: (MessageId, SenderEmail) -> Unit,
        val showDraftSavedSnackbar: (MessageId) -> Unit,
        val showMessageSendingSnackbar: () -> Unit,
        val showMessageSendingOfflineSnackbar: () -> Unit,
        val showDraftDiscardedSnackbar: () -> Unit
    ) {

        companion object {

            val Empty = Actions(

                onCloseComposerClick = {},
                onSetMessagePasswordClick = { _, _ -> },
                showDraftSavedSnackbar = {},
                showMessageSendingSnackbar = {},
                showMessageSendingOfflineSnackbar = {},
                showDraftDiscardedSnackbar = {}
            )
        }
    }
}

private sealed interface BottomSheetType {
    data object ChangeSender : BottomSheetType
    data object SetExpirationTime : BottomSheetType
    data object AttachmentSources : BottomSheetType
    data object ScheduleSendOptions : BottomSheetType
    data class InlineImageActions(val contentId: String) : BottomSheetType

    companion object {

        private const val TYPE_KEY = "sheetTypeKey"
        private const val CONTENT_ID_KEY = "inlineImageContentId"

        val Saver = mapSaver(
            save = { state: BottomSheetType ->
                when (state) {
                    is ChangeSender -> mapOf(TYPE_KEY to ChangeSender::class.simpleName)
                    is InlineImageActions -> {
                        mapOf(
                            TYPE_KEY to InlineImageActions::class.simpleName,
                            CONTENT_ID_KEY to state.contentId
                        )
                    }

                    is SetExpirationTime -> mapOf(TYPE_KEY to SetExpirationTime::class.simpleName)
                    is AttachmentSources -> mapOf(TYPE_KEY to AttachmentSources::class.simpleName)
                    is ScheduleSendOptions -> mapOf(TYPE_KEY to ScheduleSendOptions::class.simpleName)
                }
            },
            restore = { map ->
                when (map[TYPE_KEY]) {
                    ChangeSender::class.simpleName -> ChangeSender
                    InlineImageActions::class.simpleName -> InlineImageActions(map[CONTENT_ID_KEY].toString())
                    SetExpirationTime::class.simpleName -> SetExpirationTime
                    AttachmentSources::class.simpleName -> AttachmentSources
                    ScheduleSendOptions::class.simpleName -> ScheduleSendOptions
                    else -> throw IllegalStateException("Attempting to restore invalid bottom sheet type")
                }
            }
        )
    }
}

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
