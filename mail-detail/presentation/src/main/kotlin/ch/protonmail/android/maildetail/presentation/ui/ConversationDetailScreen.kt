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
package ch.protonmail.android.maildetail.presentation.ui

import android.net.Uri
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonErrorMessage
import ch.protonmail.android.design.compose.component.ProtonModalBottomSheetLayout
import ch.protonmail.android.design.compose.component.ProtonSnackbarHostState
import ch.protonmail.android.design.compose.component.ProtonSnackbarType
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailattachments.domain.model.AttachmentId
import ch.protonmail.android.mailattachments.domain.model.AttachmentOpenMode
import ch.protonmail.android.mailattachments.presentation.IntentHelper
import ch.protonmail.android.mailattachments.presentation.model.FileContent
import ch.protonmail.android.mailattachments.presentation.ui.OpenAttachmentInput
import ch.protonmail.android.mailattachments.presentation.ui.fileOpener
import ch.protonmail.android.mailattachments.presentation.ui.fileSaver
import ch.protonmail.android.mailcommon.domain.model.BasicContactInfo
import ch.protonmail.android.mailcommon.presentation.AdaptivePreviews
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.ConsumableTextEffect
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailcommon.presentation.compose.UndoableOperationSnackbar
import ch.protonmail.android.mailcommon.presentation.compose.dpToPx
import ch.protonmail.android.mailcommon.presentation.compose.pxToDp
import ch.protonmail.android.mailcommon.presentation.extension.copyTextToClipboard
import ch.protonmail.android.mailcommon.presentation.model.ActionResult
import ch.protonmail.android.mailcommon.presentation.model.AvatarUiModel
import ch.protonmail.android.mailcommon.presentation.model.BottomSheetVisibilityEffect
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailcommon.presentation.ui.BottomActionBar
import ch.protonmail.android.mailcommon.presentation.ui.CommonTestTags
import ch.protonmail.android.mailcommon.presentation.ui.delete.DeleteDialog
import ch.protonmail.android.mailcontact.domain.model.ContactId
import ch.protonmail.android.maildetail.domain.model.OpenProtonCalendarIntentValues
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMessageUiModel
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailMetadataState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailState
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailViewAction
import ch.protonmail.android.maildetail.presentation.model.ConversationDetailsMessagesState
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel
import ch.protonmail.android.maildetail.presentation.model.MoreActionsBottomSheetEntryPoint
import ch.protonmail.android.maildetail.presentation.model.ParticipantUiModel
import ch.protonmail.android.maildetail.presentation.model.HiddenMessagesBannerState
import ch.protonmail.android.maildetail.presentation.previewdata.ConversationDetailsPreviewProvider
import ch.protonmail.android.maildetail.presentation.ui.dialog.EditScheduleSendDialog
import ch.protonmail.android.maildetail.presentation.ui.dialog.MarkAsLegitimateDialog
import ch.protonmail.android.maildetail.presentation.ui.dialog.ReportPhishingDialog
import ch.protonmail.android.maildetail.presentation.viewmodel.ConversationDetailViewModel
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsBottomSheet
import ch.protonmail.android.maillabel.presentation.bottomsheet.LabelAsBottomSheetScreen
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToBottomSheet
import ch.protonmail.android.maillabel.presentation.bottomsheet.moveto.MoveToBottomSheetScreen
import ch.protonmail.android.mailmessage.domain.model.MessageBodyImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.model.MessageTheme
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions
import ch.protonmail.android.mailmessage.domain.model.RsvpAnswer
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.ContactActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.DetailMoreActionsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.LabelAsBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.MoveToBottomSheetState
import ch.protonmail.android.mailmessage.presentation.model.bottomsheet.SnoozeSheetState
import ch.protonmail.android.mailmessage.presentation.ui.bottomsheet.ContactActionsBottomSheetContent
import ch.protonmail.android.mailmessage.presentation.ui.bottomsheet.DetailMoreActionsBottomSheetContent
import ch.protonmail.android.mailsnooze.presentation.SnoozeBottomSheet
import ch.protonmail.android.mailsnooze.presentation.SnoozeBottomSheetScreen
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility
import ch.protonmail.android.uicomponents.snackbar.DismissableSnackbarHost
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    modifier: Modifier = Modifier,
    actions: ConversationDetail.Actions,
    viewModel: ConversationDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isSystemBackButtonClickEnabled = remember { mutableStateOf(true) }
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val isSingleMessageMode = remember { viewModel.isSingleMessageModeEnabled }

    var showBottomSheet by remember { mutableStateOf(false) }
    val currentAppTheme = if (isSystemInDarkTheme())
        MessageTheme.Dark
    else
        MessageTheme.Light

    state.bottomSheetState?.let {
        ConsumableLaunchedEffect(effect = it.bottomSheetVisibilityEffect) { bottomSheetEffect ->
            when (bottomSheetEffect) {
                BottomSheetVisibilityEffect.Hide -> {
                    scope
                        .launch { bottomSheetState.hide() }
                        .invokeOnCompletion {
                            if (!bottomSheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                }

                BottomSheetVisibilityEffect.Show -> showBottomSheet = true
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (bottomSheetState.currentValue != SheetValue.Hidden) {
                viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
            }
        }
    }

    BackHandler(bottomSheetState.isVisible) {
        viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
    }

    BackHandler(!bottomSheetState.isVisible && isSystemBackButtonClickEnabled.value) {
        actions.recordMailboxScreenView()
        isSystemBackButtonClickEnabled.value = false
        scope.launch {
            awaitFrame()
            onBackPressedDispatcher?.onBackPressed()
        }
    }

    DeleteDialog(
        state = state.conversationDeleteState.deleteDialogState,
        confirm = {
            when (val messageId = state.conversationDeleteState.messageIdInConversation) {
                null -> viewModel.submit(ConversationDetailViewAction.DeleteConfirmed)
                else -> viewModel.submit(ConversationDetailViewAction.DeleteMessageConfirmed(messageId))
            }
        },
        dismiss = { viewModel.submit(ConversationDetailViewAction.DeleteDialogDismissed) }
    )

    ReportPhishingDialog(
        state = state.reportPhishingDialogState,
        onConfirm = { viewModel.submit(ConversationDetailViewAction.ReportPhishingConfirmed(it)) },
        onDismiss = { viewModel.submit(ConversationDetailViewAction.ReportPhishingDismissed) }
    )

    MarkAsLegitimateDialog(
        state = state.markAsLegitimateDialogState,
        onConfirm = { viewModel.submit(ConversationDetailViewAction.MarkMessageAsLegitimateConfirmed(it)) },
        onDismiss = { viewModel.submit(ConversationDetailViewAction.MarkMessageAsLegitimateDismissed) }
    )

    EditScheduleSendDialog(
        state = state.editScheduledMessageDialogState,
        onConfirm = { viewModel.submit(ConversationDetailViewAction.EditScheduleSendMessageConfirmed(it)) },
        onDismiss = { viewModel.submit(ConversationDetailViewAction.EditScheduleSendMessageDismissed) }
    )


    ProtonModalBottomSheetLayout(
        showBottomSheet = showBottomSheet,
        sheetState = bottomSheetState,
        onDismissed = { showBottomSheet = false },
        dismissOnBack = true,
        sheetContent = {
            when (val bottomSheetContentState = state.bottomSheetState?.contentState) {
                is MoveToBottomSheetState.Requested -> {
                    val initialData = MoveToBottomSheet.InitialData(
                        userId = bottomSheetContentState.userId,
                        currentLocationLabelId = bottomSheetContentState.currentLabel,
                        items = bottomSheetContentState.itemIds,
                        entryPoint = bottomSheetContentState.entryPoint
                    )

                    val actions = MoveToBottomSheet.Actions(
                        onCreateNewFolderClick = actions.onAddLabel,
                        onError = { actions.showSnackbar(it, ProtonSnackbarType.ERROR) },
                        onMoveToComplete = { mailLabelText, entryPoint ->
                            val action = ConversationDetailViewAction.MoveToCompleted(mailLabelText, entryPoint)
                            viewModel.submit(action)
                        },
                        onDismiss = { viewModel.submit(ConversationDetailViewAction.DismissBottomSheet) }
                    )

                    MoveToBottomSheetScreen(providedData = initialData, actions = actions)
                }

                is LabelAsBottomSheetState.Requested -> {
                    val initialData = LabelAsBottomSheet.InitialData(
                        userId = bottomSheetContentState.userId,
                        currentLocationLabelId = bottomSheetContentState.currentLocationLabelId,
                        items = bottomSheetContentState.itemIds,
                        entryPoint = bottomSheetContentState.entryPoint
                    )

                    val actions = LabelAsBottomSheet.Actions(
                        onCreateNewLabelClick = actions.onAddLabel,
                        onError = { actions.showSnackbar(it, ProtonSnackbarType.ERROR) },
                        onLabelAsComplete = { wasArchived, entryPoint ->
                            viewModel.submit(
                                ConversationDetailViewAction.LabelAsCompleted(wasArchived, entryPoint)
                            )
                        },
                        onDismiss = { viewModel.submit(ConversationDetailViewAction.DismissBottomSheet) }
                    )

                    LabelAsBottomSheetScreen(providedData = initialData, actions = actions)
                }

                is SnoozeSheetState.Requested -> {
                    val initialData = SnoozeBottomSheet.InitialData(
                        bottomSheetContentState.userId,
                        bottomSheetContentState.labelId,
                        items = bottomSheetContentState.itemIds
                    )
                    SnoozeBottomSheetScreen(
                        initialData = initialData,
                        actions = SnoozeBottomSheet.Actions(
                            onShowSuccess = {
                                viewModel.submit(ConversationDetailViewAction.SnoozeCompleted(it))
                            },
                            onShowError = {
                                actions.showSnackbar(it, ProtonSnackbarType.ERROR)
                                viewModel.submit(ConversationDetailViewAction.SnoozeDismissed)
                            },
                            onNavigateToUpsell = { type ->
                                actions.onNavigateToUpselling(UpsellingEntryPoint.Feature.Snooze, type)
                                viewModel.submit(ConversationDetailViewAction.SnoozeDismissed)
                            }
                        )
                    )
                }

                is DetailMoreActionsBottomSheetState -> DetailMoreActionsBottomSheetContent(
                    state = bottomSheetContentState,
                    actions = DetailMoreActionsBottomSheetContent.Actions(
                        onReply = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.onReply(it)
                        },
                        onReplyAll = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.onReplyAll(it)
                        },
                        onForward = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.onForward(it)
                        },
                        onMarkUnread = { viewModel.submit(ConversationDetailViewAction.MarkMessageUnread(it)) },
                        onStarMessage = { viewModel.submit(ConversationDetailViewAction.StarMessage(it)) },
                        onUnStarMessage = { viewModel.submit(ConversationDetailViewAction.UnStarMessage(it)) },
                        onMoveToInbox = { viewModel.submit(ConversationDetailViewAction.MoveMessage.System.Inbox(it)) },
                        onSaveMessageAsPdf = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.showFeatureMissingSnackbar()
                        },
                        onLabel = {
                            viewModel.submit(ConversationDetailViewAction.RequestMessageLabelAsBottomSheet(it))
                        },
                        onViewInLightMode = {
                            viewModel.submit(
                                ConversationDetailViewAction.SwitchViewMode(
                                    messageId = it,
                                    currentTheme = currentAppTheme,
                                    overrideTheme = MessageTheme.Light
                                )
                            )
                        },
                        onViewInDarkMode = {
                            viewModel.submit(
                                ConversationDetailViewAction.SwitchViewMode(
                                    messageId = it,
                                    currentTheme = currentAppTheme,
                                    overrideTheme = MessageTheme.Dark
                                )
                            )
                        },
                        onMoveToTrash = { viewModel.submit(ConversationDetailViewAction.MoveMessage.System.Trash(it)) },
                        onMoveToArchive = {
                            viewModel.submit(ConversationDetailViewAction.MoveMessage.System.Archive(it))
                        },
                        onDelete = { viewModel.submit(ConversationDetailViewAction.DeleteMessageRequested(it)) },
                        onMoveToSpam = { viewModel.submit(ConversationDetailViewAction.MoveMessage.System.Spam(it)) },
                        onMove = { viewModel.submit(ConversationDetailViewAction.RequestMessageMoveToBottomSheet(it)) },
                        onPrint = { viewModel.submit(ConversationDetailViewAction.PrintMessage(context, it)) },
                        onReportPhishing = { viewModel.submit(ConversationDetailViewAction.ReportPhishing(it)) },

                        onMarkReadConversation = { viewModel.submit(ConversationDetailViewAction.MarkRead) },
                        onMarkUnreadConversation = { viewModel.submit(ConversationDetailViewAction.MarkUnread) },
                        onLabelConversation = {
                            viewModel.submit(ConversationDetailViewAction.RequestConversationLabelAsBottomSheet)
                        },
                        onMoveToTrashConversation = { viewModel.submit(ConversationDetailViewAction.MoveToTrash) },
                        onMoveConversation = {
                            viewModel.submit(ConversationDetailViewAction.RequestConversationMoveToBottomSheet)
                        },
                        onDeleteConversation = { viewModel.submit(ConversationDetailViewAction.DeleteRequested) },

                        onMoveToInboxConversation = { viewModel.submit(ConversationDetailViewAction.MoveToInbox) },
                        onMoveToArchiveConversation = { viewModel.submit(ConversationDetailViewAction.MoveToArchive) },
                        onMoveToSpamConversation = { viewModel.submit(ConversationDetailViewAction.MoveToSpam) },
                        onStarConversation = { viewModel.submit(ConversationDetailViewAction.Star) },
                        onUnStarConversation = { viewModel.submit(ConversationDetailViewAction.UnStar) },
                        onPrintConversation = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.showFeatureMissingSnackbar()
                        },
                        onCloseSheet = { viewModel.submit(ConversationDetailViewAction.DismissBottomSheet) },
                        onCustomizeToolbar = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.onCustomizeToolbar()
                        },
                        onCustomizeMessageToolbar = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.onCustomizeMessageToolbar()
                        },
                        onSaveConversationAsPdf = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.showFeatureMissingSnackbar()
                        },
                        onSnooze = {
                            viewModel.submit(ConversationDetailViewAction.RequestSnoozeBottomSheet)
                        }
                    )
                )

                is ContactActionsBottomSheetState -> ContactActionsBottomSheetContent(
                    state = bottomSheetContentState,
                    actions = ContactActionsBottomSheetContent.Actions(
                        onCopyAddressClicked = {
                            val message = context.getString(R.string.contact_actions_copy_address_performed)
                            context.copyTextToClipboard(label = message, text = it)

                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.showSnackbar(message, ProtonSnackbarType.NORM)
                        },
                        onCopyNameClicked = {
                            val message = context.getString(R.string.contact_actions_copy_name_performed)
                            context.copyTextToClipboard(label = message, text = it)

                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.showSnackbar(message, ProtonSnackbarType.NORM)
                        },
                        onAddContactClicked = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.onAddContact(BasicContactInfo(it.name, it.address))
                        },
                        onNewMessageClicked = {
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.onComposeNewMessage(it.address)
                        },
                        onBlockClicked = { participant, messageId ->
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.showFeatureMissingSnackbar() // ET-5092
                        },
                        onUnblockClicked = { participant, messageId ->
                            viewModel.submit(ConversationDetailViewAction.DismissBottomSheet)
                            actions.showFeatureMissingSnackbar() // ET-5092
                        }
                    )
                )

                else -> Unit
            }
        }
    ) {
        ConversationDetailScreen(
            modifier = modifier,
            state = state,
            actions = ConversationDetailScreen.Actions(
                onExit = actions.onExit,
                onExitWithError = {
                    actions.showSnackbar(it, ProtonSnackbarType.ERROR)
                    actions.onExit(null)
                },
                onStarClick = { viewModel.submit(ConversationDetailViewAction.Star) },
                onTrashClick = { viewModel.submit(ConversationDetailViewAction.MoveToTrash) },
                onDeleteClick = { viewModel.submit(ConversationDetailViewAction.DeleteRequested) },
                onArchiveClick = { viewModel.submit(ConversationDetailViewAction.MoveToArchive) },
                onSpamClick = { viewModel.submit(ConversationDetailViewAction.MoveToSpam) },
                onUnStarClick = { viewModel.submit(ConversationDetailViewAction.UnStar) },
                onReadClick = { viewModel.submit(ConversationDetailViewAction.MarkRead) },
                onUnreadClick = { viewModel.submit(ConversationDetailViewAction.MarkUnread) },
                onMoveToClick = { viewModel.submit(ConversationDetailViewAction.RequestConversationMoveToBottomSheet) },
                onMoveToInboxClick = { viewModel.submit(ConversationDetailViewAction.MoveToInbox) },
                onLabelAsClick = {
                    viewModel.submit(ConversationDetailViewAction.RequestConversationLabelAsBottomSheet)
                },
                onMoreActionsClick = {
                    viewModel.submit(
                        ConversationDetailViewAction.RequestConversationMoreActionsBottomSheet(
                            MoreActionsBottomSheetEntryPoint.BottomBar
                        )
                    )
                },
                onExpandMessage = { viewModel.submit(ConversationDetailViewAction.ExpandMessage(it)) },
                onCollapseMessage = { viewModel.submit(ConversationDetailViewAction.CollapseMessage(it)) },
                onMessageBodyLinkClicked = { messageId, uri ->
                    viewModel.submit(ConversationDetailViewAction.MessageBodyLinkClicked(messageId, uri))
                },
                onOpenMessageBodyLink = actions.openMessageBodyLink,
                onRequestScrollTo = { viewModel.submit(ConversationDetailViewAction.RequestScrollTo(it)) },
                onShowAllAttachmentsForMessage = {
                    viewModel.submit(ConversationDetailViewAction.ShowAllAttachmentsForMessage(it))
                },
                onAttachmentClicked = { openMode, messageId, attachmentId ->
                    viewModel.submit(
                        ConversationDetailViewAction.OnAttachmentClicked(openMode, messageId, attachmentId)
                    )
                },
                onToggleAttachmentsExpandCollapseMode = {
                    viewModel.submit(ConversationDetailViewAction.ExpandOrCollapseAttachmentList(it))
                },
                handleProtonCalendarRequest = actions.handleProtonCalendarRequest,
                showFeatureMissingSnackbar = actions.showFeatureMissingSnackbar,
                loadImage = { messageId, url -> viewModel.loadImage(messageId, url) },
                onReply = {
                    actions.onReply(it)
                },
                onReplyAll = {
                    actions.onReplyAll(it)
                },
                onForward = {
                    actions.onForward(it)
                },
                onScrollRequestCompleted = { viewModel.submit(ConversationDetailViewAction.ScrollRequestCompleted) },
                onDoNotAskLinkConfirmationAgain = {
                    viewModel.submit(ConversationDetailViewAction.DoNotAskLinkConfirmationAgain)
                },
                onBodyExpandCollapseButtonClicked = {
                    viewModel.submit(ConversationDetailViewAction.ExpandOrCollapseMessageBody(it))
                },
                onMoreMessageActionsClick = { messageId, themeOptions ->
                    viewModel.submit(
                        action = ConversationDetailViewAction.RequestMessageMoreActionsBottomSheet(
                            messageId = messageId,
                            themeOptions = themeOptions,
                            entryPoint = MoreActionsBottomSheetEntryPoint.MessageHeader
                        )
                    )
                },
                onLoadRemoteContent = {
                    viewModel.submit(ConversationDetailViewAction.LoadRemoteContent(MessageIdUiModel(it.id)))
                },
                onLoadEmbeddedImages = {
                    viewModel.submit(ConversationDetailViewAction.ShowEmbeddedImages(MessageIdUiModel(it.id)))
                },
                onLoadRemoteAndEmbeddedContent = {
                    viewModel.submit(ConversationDetailViewAction.LoadRemoteAndEmbeddedContent(MessageIdUiModel(it.id)))
                },
                onOpenInProtonCalendar = {
                    viewModel.submit(ConversationDetailViewAction.OpenInProtonCalendar(MessageId(it.id)))
                },
                onPrint = { viewModel.submit(ConversationDetailViewAction.PrintMessage(context, it)) },
                onAvatarClicked = { participantUiModel, avatarUiModel, messageIdUiModel ->
                    viewModel.submit(
                        ConversationDetailViewAction.RequestContactActionsBottomSheet(
                            participantUiModel,
                            avatarUiModel,
                            messageIdUiModel
                        )
                    )
                },
                onAvatarImageLoadRequested = { avatarUiModel ->
                    viewModel.submit(ConversationDetailViewAction.OnAvatarImageLoadRequested(avatarUiModel))
                },
                onOpenComposer = { actions.openComposerForDraftMessage(MessageId(it.id)) },
                onParticipantClicked = { participantUiModel, avatarUiModel, messageIdUiModel ->
                    viewModel.submit(
                        ConversationDetailViewAction.RequestContactActionsBottomSheet(
                            participantUiModel,
                            avatarUiModel,
                            messageIdUiModel
                        )
                    )
                },
                onHiddenMessagesBannerClick = {
                    viewModel.submit(ConversationDetailViewAction.ChangeVisibilityOfMessages)
                },
                onMarkMessageAsLegitimate = { messageId, isPhishing ->
                    viewModel.submit(
                        ConversationDetailViewAction.MarkMessageAsLegitimate(MessageId(messageId.id), isPhishing)
                    )
                },
                onUnblockSender = { messageId, email ->
                    viewModel.submit(ConversationDetailViewAction.UnblockSender(messageId, email))
                },
                onEditScheduleSendMessage = { messageId ->
                    viewModel.submit(ConversationDetailViewAction.EditScheduleSendMessageRequested(messageId))
                },
                onExitWithOpenInComposer = {
                    actions.onExitWithOpenInComposer(MessageId(it.id))
                },
                onRetryRsvpEventLoading = {
                    viewModel.submit(ConversationDetailViewAction.RetryRsvpEventLoading(MessageId(it.id)))
                },
                onAnswerRsvpEvent = { messageId, answer ->
                    viewModel.submit(ConversationDetailViewAction.AnswerRsvpEvent(MessageId(messageId.id), answer))
                },
                onMessage = actions.onComposeNewMessage,
                onUnsnoozeMessage = {
                    viewModel.submit(ConversationDetailViewAction.OnUnsnoozeConversationRequested)
                },
                onSnooze = { viewModel.submit(ConversationDetailViewAction.RequestSnoozeBottomSheet) },
                onActionBarVisibilityChanged = actions.onActionBarVisibilityChanged,
                onUnsubscribeFromNewsletter = {
                    viewModel.submit(ConversationDetailViewAction.UnsubscribeFromNewsletter(MessageId(it.id)))
                },
                onReportPhishing = { messageId ->
                    viewModel.submit(ConversationDetailViewAction.ReportPhishing(messageId))
                },
                onDownloadImage = { messageId, imageUrl ->
                    Toast.makeText(context, context.getString(R.string.feature_coming_soon), Toast.LENGTH_SHORT).show()
                }
            ),
            scrollToMessageId = state.scrollToMessage?.id,
            isSingleMessageMode = isSingleMessageMode
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod", "ComplexMethod")
@Composable
fun ConversationDetailScreen(
    state: ConversationDetailState,
    actions: ConversationDetailScreen.Actions,
    modifier: Modifier = Modifier,
    scrollToMessageId: String?,
    isSingleMessageMode: Boolean
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(snapAnimationSpec = null)
    val snackbarHostState = remember { ProtonSnackbarHostState() }
    val linkConfirmationDialogState = remember { mutableStateOf<Uri?>(null) }
    val phishingLinkConfirmationDialogState = remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val fileSaver = fileSaver(
        onFileSaved = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
        onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    )

    val openAttachment = fileOpener()

    state.loadingErrorEffect.consume()?.let { errorMessage ->
        actions.onExitWithError(errorMessage.string())
        return
    }

    ConsumableLaunchedEffect(state.exitScreenEffect) { actions.onExit(null) }
    state.exitScreenActionResult.consume()?.let { actionResult ->
        actions.onExit(actionResult)
    }
    ConsumableTextEffect(state.error) { string ->
        snackbarHostState.showSnackbar(ProtonSnackbarType.ERROR, message = string)
    }

    UndoableOperationSnackbar(snackbarHostState = snackbarHostState, actionEffect = state.actionResult)

    ConsumableLaunchedEffect(effect = state.openMessageBodyLinkEffect) { messageBodyLink ->
        val message = when (state.messagesState) {
            is ConversationDetailsMessagesState.Data -> state.messagesState.messages.find {
                it.messageId == messageBodyLink.messageId
            }

            else -> null
        }
        val requestPhishingLinkConfirmation = when (message) {
            is ConversationDetailMessageUiModel.Expanded -> message.requestPhishingLinkConfirmation
            else -> false
        }
        if (requestPhishingLinkConfirmation) {
            phishingLinkConfirmationDialogState.value = messageBodyLink.uri
        } else if (state.requestLinkConfirmation) {
            linkConfirmationDialogState.value = messageBodyLink.uri
        } else {
            actions.onOpenMessageBodyLink(messageBodyLink.uri)
        }
    }
    ConsumableLaunchedEffect(effect = state.openAttachmentEffect) {
        when (it.openMode) {
            AttachmentOpenMode.Download -> fileSaver(FileContent(it.name, it.uri, it.mimeType))
            AttachmentOpenMode.Open -> {
                if (IntentHelper.canOpenFile(context, OpenAttachmentInput(it.uri, it.mimeType))) {
                    openAttachment(OpenAttachmentInput(it.uri, it.mimeType))
                } else {
                    fileSaver(FileContent(it.name, it.uri, it.mimeType))
                }
            }
        }
    }

    ConsumableLaunchedEffect(effect = state.openProtonCalendarIntent) {
        actions.handleProtonCalendarRequest(it)
    }

    ConsumableLaunchedEffect(effect = state.onExitWithNavigateToComposer) {
        actions.onExitWithOpenInComposer(it)
    }

    if (linkConfirmationDialogState.value != null) {
        ExternalLinkConfirmationDialog(
            onCancelClicked = {
                linkConfirmationDialogState.value = null
            },
            onContinueClicked = { doNotShowAgain ->
                linkConfirmationDialogState.value?.let { actions.onOpenMessageBodyLink(it) }
                linkConfirmationDialogState.value = null
                if (doNotShowAgain) {
                    actions.onDoNotAskLinkConfirmationAgain()
                }
            },
            linkUri = linkConfirmationDialogState.value
        )
    }

    if (phishingLinkConfirmationDialogState.value != null) {
        PhishingLinkConfirmationDialog(
            onCancelClicked = { phishingLinkConfirmationDialogState.value = null },
            onContinueClicked = {
                phishingLinkConfirmationDialogState.value?.let { actions.onOpenMessageBodyLink(it) }
            },
            linkUri = phishingLinkConfirmationDialogState.value
        )
    }

    if (state.conversationState is ConversationDetailMetadataState.Error) {
        val message = state.conversationState.message.string()
        LaunchedEffect(state.conversationState) {
            snackbarHostState.showSnackbar(
                type = ProtonSnackbarType.ERROR,
                message = message
            )
        }
    }

    // When SubjectHeader is first time composed, we need to get the its actual height to be able to calculate yOffset
    // for collapsing effect
    val subjectHeaderSizeCallback: (Int) -> Unit = {
        scrollBehavior.state.heightOffsetLimit = -it.toFloat()
    }

    Scaffold(
        modifier = modifier
            .testTag(ConversationDetailScreenTestTags.RootItem)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = ProtonTheme.colors.backgroundNorm,
        snackbarHost = {
            DismissableSnackbarHost(
                modifier = Modifier.testTag(CommonTestTags.SnackbarHost),
                protonSnackbarHostState = snackbarHostState
            )
        },
        topBar = {
            val uiModel = (state.conversationState as? ConversationDetailMetadataState.Data)?.conversationUiModel
            val messageCount = if (isSingleMessageMode) null else uiModel?.messageCount
            DetailScreenTopBar(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = scrollBehavior.state.heightOffset / 2f
                    },
                title = uiModel?.subject ?: DetailScreenTopBar.NoTitle,
                isStarred = uiModel?.isStarred,
                messageCount = messageCount,
                actions = DetailScreenTopBar.Actions(
                    onBackClick = { actions.onExit(null) },
                    onStarClick = actions.onStarClick,
                    onUnStarClick = actions.onUnStarClick
                ),
                subjectHeaderSizeCallback = subjectHeaderSizeCallback,
                topAppBarState = scrollBehavior.state
            )
        },
        bottomBar = {
            BottomActionBar(
                state = state.bottomBarState,
                viewActionCallbacks = BottomActionBar.Actions(
                    onMarkRead = actions.onReadClick,
                    onMarkUnread = actions.onUnreadClick,
                    onStar = actions.onStarClick,
                    onUnstar = actions.onUnStarClick,
                    onMove = actions.onMoveToClick,
                    onLabel = actions.onLabelAsClick,
                    onTrash = actions.onTrashClick,
                    onDelete = actions.onDeleteClick,
                    onArchive = actions.onArchiveClick,
                    onSpam = actions.onSpamClick,
                    onMoveToInbox = actions.onMoveToInboxClick,
                    onViewInLightMode = { Timber.d("conversation onViewInLightMode clicked") },
                    onViewInDarkMode = { Timber.d("conversation onViewInDarkMode clicked") },
                    onPrint = { rawId -> actions.onPrint(MessageId(rawId)) },
                    onViewHeaders = actions.showFeatureMissingSnackbar,
                    onViewHtml = actions.showFeatureMissingSnackbar,
                    onReportPhishing = { rawId -> actions.onReportPhishing(MessageId(rawId)) },
                    onRemind = { Timber.d("conversation onRemind clicked") },
                    onSavePdf = { Timber.d("conversation onSavePdf clicked") },
                    onSenderEmail = { Timber.d("conversation onSenderEmail clicked") },
                    onSaveAttachments = { Timber.d("conversation onSaveAttachments clicked") },
                    onMore = actions.onMoreActionsClick,
                    onCustomizeToolbar = { Timber.d("conversation onCustomizeToolbar clicked") },
                    onSnooze = actions.onSnooze,
                    onActionBarVisibilityChanged = actions.onActionBarVisibilityChanged,
                    onReply = { rawId -> actions.onReply(MessageId(rawId)) },
                    onReplyAll = { rawId -> actions.onReplyAll(MessageId(rawId)) },
                    onForward = { rawId -> actions.onForward(MessageId(rawId)) }
                )
            )
        }
    ) { innerPadding ->
        when (state.messagesState) {
            is ConversationDetailsMessagesState.Data -> {
                val conversationDetailItemActions = ConversationDetailItem.Actions(
                    onExpand = actions.onExpandMessage,
                    onCollapse = actions.onCollapseMessage,
                    onOpenComposer = actions.onOpenComposer,
                    onMessageBodyLinkClicked = actions.onMessageBodyLinkClicked,
                    onOpenMessageBodyLink = actions.onOpenMessageBodyLink,
                    onShowAllAttachmentsForMessage = actions.onShowAllAttachmentsForMessage,
                    onAttachmentClicked = actions.onAttachmentClicked,
                    onToggleAttachmentsExpandCollapseMode = actions.onToggleAttachmentsExpandCollapseMode,
                    showFeatureMissingSnackbar = actions.showFeatureMissingSnackbar,
                    loadImage = actions.loadImage,
                    onReply = actions.onReply,
                    onReplyAll = actions.onReplyAll,
                    onForward = actions.onForward,
                    onScrollRequestCompleted = actions.onScrollRequestCompleted,
                    onBodyExpandCollapseButtonClicked = actions.onBodyExpandCollapseButtonClicked,
                    onMoreMessageActionsClick = actions.onMoreMessageActionsClick,
                    onLoadRemoteContent = actions.onLoadRemoteContent,
                    onLoadEmbeddedImages = actions.onLoadEmbeddedImages,
                    onLoadRemoteAndEmbeddedContent = { actions.onLoadRemoteAndEmbeddedContent(it) },
                    onOpenInProtonCalendar = { actions.onOpenInProtonCalendar(it) },
                    onPrint = actions.onPrint,
                    onAvatarClicked = actions.onAvatarClicked,
                    onAvatarImageLoadRequested = actions.onAvatarImageLoadRequested,
                    onParticipantClicked = actions.onParticipantClicked,
                    onMarkMessageAsLegitimate = actions.onMarkMessageAsLegitimate,
                    onUnblockSender = actions.onUnblockSender,
                    onEditScheduleSendMessage = actions.onEditScheduleSendMessage,
                    onRetryRsvpEventLoading = actions.onRetryRsvpEventLoading,
                    onAnswerRsvpEvent = actions.onAnswerRsvpEvent,
                    onMessage = actions.onMessage,
                    onUnsnoozeMessage = actions.onUnsnoozeMessage,
                    onUnsubscribeFromNewsletter = actions.onUnsubscribeFromNewsletter,
                    onDownloadImage = actions.onDownloadImage
                )
                MessagesContentWithHiddenEdges(
                    uiModels = state.messagesState.messages,
                    hiddenMessagesBannerState = state.hiddenMessagesBannerState,
                    padding = innerPadding,
                    scrollToMessageId = scrollToMessageId,
                    actions = conversationDetailItemActions,
                    onHiddenMessagesBannerClick = actions.onHiddenMessagesBannerClick,
                    paddingOffsetDp = scrollBehavior.state.heightOffset.pxToDp(),
                    conversationKey = state.conversationId()
                )
            }

            is ConversationDetailsMessagesState.Error -> ProtonErrorMessage(
                modifier = Modifier.padding(innerPadding),
                errorMessage = state.messagesState.message.string()
            )

            is ConversationDetailsMessagesState.Loading,
            is ConversationDetailsMessagesState.Offline -> ProtonCenteredProgress(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun MessagesContentWithHiddenEdges(
    uiModels: ImmutableList<ConversationDetailMessageUiModel>,
    hiddenMessagesBannerState: HiddenMessagesBannerState,
    padding: PaddingValues,
    scrollToMessageId: String?,
    modifier: Modifier = Modifier,
    actions: ConversationDetailItem.Actions,
    onHiddenMessagesBannerClick: () -> Unit,
    paddingOffsetDp: Dp = 0f.dp,
    conversationKey: String
) {

    Box(modifier = Modifier.fillMaxWidth()) {
        MessagesContent(
            modifier = modifier,
            uiModels = uiModels,
            hiddenMessagesBannerState = hiddenMessagesBannerState,
            padding = padding,
            scrollToMessageId = scrollToMessageId,
            actions = actions,
            onHiddenMessagesBannerClick = onHiddenMessagesBannerClick,
            paddingOffsetDp = paddingOffsetDp,
            conversationId = conversationKey
        )

        // Cover left and right edges
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(MailDimens.MediumBorder)
                .align(Alignment.CenterStart)
                .background(ProtonTheme.colors.backgroundNorm)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(MailDimens.MediumBorder)
                .align(Alignment.CenterEnd)
                .background(ProtonTheme.colors.backgroundNorm)
        )
    }
}

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, FlowPreview::class,
    ExperimentalAnimationApi::class
)
@Composable
@Suppress("LongParameterList", "ComplexMethod")
private fun MessagesContent(
    uiModels: ImmutableList<ConversationDetailMessageUiModel>,
    hiddenMessagesBannerState: HiddenMessagesBannerState,
    padding: PaddingValues,
    scrollToMessageId: String?,
    modifier: Modifier = Modifier,
    actions: ConversationDetailItem.Actions,
    onHiddenMessagesBannerClick: () -> Unit,
    paddingOffsetDp: Dp = 0f.dp,
    conversationId: String
) {
    val listState = rememberLazyListState()
    var webContentLoaded by remember(conversationId) { mutableIntStateOf(0) }
    val loadedItemsHeight = remember(conversationId) { mutableStateMapOf<String, Int>() }

    val layoutDirection = LocalLayoutDirection.current
    val contentPadding =
        PaddingValues(
            start = padding.calculateStartPadding(layoutDirection),
            end = padding.calculateEndPadding(layoutDirection),
            top = (
                padding.calculateTopPadding() + ProtonDimens.Spacing.Standard + paddingOffsetDp
                ).coerceAtLeast(0f.dp),
            bottom = (padding.calculateBottomPadding() - ProtonDimens.Spacing.Tiny).coerceAtLeast(0f.dp)
        )

    // Map of item heights in LazyColumn (Row index -> height)
    // We will use this map to calculate total height of first non-draft message + any draft messages below it
    val itemsHeight = remember(conversationId) { mutableStateMapOf<Int, Int>() }
    var scrollCount by remember(conversationId) { mutableIntStateOf(0) }

    var scrollToIndex = remember(scrollToMessageId, uiModels) {
        if (scrollToMessageId == null) return@remember null
        else uiModels.indexOfFirst { uiModel -> uiModel.messageId.id == scrollToMessageId }
    }

    LaunchedEffect(key1 = scrollToIndex, key2 = webContentLoaded) {
        if (scrollToIndex != null) {

            // We are having frequent state updates at the beginning which are causing recompositions and
            // animateScrollToItem to be cancelled or delayed. Therefore we use scrollToItem for
            // the first scroll action.
            if (webContentLoaded == 0) {

                // When try to perform both scrolling and expanding at the same time, the above scrollToItem
                // suspend function is paused during WebView initialization. Therefore we notify the view model
                // after the completion of the first scrolling to start expanding the message.
                if (scrollCount == 0) {
                    scrollToMessageId?.let { actions.onExpand(MessageIdUiModel(it)) }
                }

                scrollCount++

            } else {

                // Scrolled message expanded, so we can conclude that scrolling is completed
                actions.onScrollRequestCompleted()
                scrollToIndex = null
            }
        }
    }

    // height calculated based on whether there is space to fill (in the case where we have not many messages
    // then the message should take up the rest of the screen space
    var scrollToMessageMinimumHeightPx by remember { mutableIntStateOf(0) }

    // Detect if user manually scrolled the list
    var userScrolled by remember(conversationId) { mutableStateOf(false) }
    var userTapped by remember(conversationId) { mutableStateOf(false) }
    LaunchedEffect(key1 = listState.isScrollInProgress) {
        if (!userScrolled && userTapped && listState.isScrollInProgress) {
            userScrolled = true
        }
    }

    val isAllItemsMeasured = remember(conversationId) {
        derivedStateOf { itemsHeight.size >= listState.layoutInfo.visibleItemsInfo.size }
    }

    // The webview for the message that we will scroll to has loaded
    // this is important as the listview will need its final height
    var isScrollToMessageWebViewLoaded by rememberSaveable(conversationId) { mutableStateOf(false) }
    val viewHasFinishedScrollingAndMeasuring = remember(conversationId) {
        derivedStateOf {
            itemsHeight.isNotEmpty() &&
                isScrollToMessageWebViewLoaded &&
                isAllItemsMeasured.value
        }
    }
    val headerOverlapHeightPx = MailDimens.ConversationCollapseHeaderOverlapHeight.dpToPx()
    var finishedResizingOperations by rememberSaveable(conversationId) { mutableStateOf(false) }
    LaunchedEffect(listState, conversationId) {
        snapshotFlow { viewHasFinishedScrollingAndMeasuring.value }
            .filter { it }
            .collectLatest {
                val sumOfHeights = itemsHeight.entries.sumOf { it.value } - itemsHeight.entries.last().value

                // there is a designed overlap / stacking of conversation cards, so we need to take this into
                // account when calculating heights
                val sumOfCardOverlap = (itemsHeight.size - 1) * headerOverlapHeightPx

                val listHeight = listState.layoutInfo.viewportSize.height -
                    listState.layoutInfo.afterContentPadding -
                    listState.layoutInfo.beforeContentPadding
                val availableSpace = listHeight - sumOfHeights + sumOfCardOverlap
                if (itemsHeight.entries.last().value < availableSpace) {
                    // then we should expand to fit space
                    scrollToMessageMinimumHeightPx = availableSpace
                }
                finishedResizingOperations = true
            }

    }

    // The webview for the message that we will scroll to has loaded
    // this is important as the listview will need its final height
    LaunchedEffect(conversationId) {
        scrollToIndex?.let { listState.scrollToItem(it) }
        // wait for the final height of our target expanded message before scrolling
        snapshotFlow { finishedResizingOperations }
            .filter {
                it && !userScrolled && scrollToIndex != null
            }
            .distinctUntilChanged()
            // creates a delay to wait for item to finish expanding animations
            .debounce(AnimationConstants.DefaultDurationMillis.toLong())
            .collectLatest {
                scrollToIndex?.let {
                    listState.animateScrollToItem((it - 1).coerceAtLeast(0))
                }
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .testTag(ConversationDetailScreenTestTags.MessagesList)
            .pointerInteropFilter { event ->
                if (!userTapped && event.action == MotionEvent.ACTION_DOWN) {
                    userTapped = true
                }
                false // Allow the event to propagate
            },
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(-MailDimens.ConversationCollapseHeaderOverlapHeight),
        state = listState
    ) {

        when (hiddenMessagesBannerState) {
            is HiddenMessagesBannerState.Shown -> {
                item {
                    HiddenMessagesBanner(
                        modifier = Modifier.onSizeChanged {
                            itemsHeight[-1] = it.height
                        },
                        state = hiddenMessagesBannerState,
                        onCheckedChange = onHiddenMessagesBannerClick
                    )
                }
            }

            is HiddenMessagesBannerState.Hidden -> Unit
        }

        itemsIndexed(uiModels) { index, uiModel ->
            val isLastItem = index == uiModels.size - 1
            val rememberCachedHeight = remember { loadedItemsHeight[uiModel.messageId.id] }
            val itemFinishedResizing = finishedResizingOperations && loadedItemsHeight.contains(uiModel.messageId.id)

            ConversationDetailItem(
                uiModel = uiModel,
                actions = actions,
                modifier = when (uiModel) {
                    is ConversationDetailMessageUiModel.Collapsed,
                    is ConversationDetailMessageUiModel.Expanding -> {
                        loadedItemsHeight.remove(uiModel.messageId.id)
                        Modifier.animateItem()
                    }

                    is ConversationDetailMessageUiModel.Expanded -> {
                        if (isLastItem) {
                            if (scrollToMessageMinimumHeightPx > 0) {
                                Modifier.heightIn(min = scrollToMessageMinimumHeightPx.pxToDp())
                            } else Modifier
                        } else {
                            Modifier.padding(bottom = MailDimens.ConversationItemBottomPadding)
                        }
                    }
                }.onSizeChanged {
                    itemsHeight[index] = it.height
                },
                onMessageBodyLoadFinished = { messageId, height ->
                    loadedItemsHeight[messageId.id] = height
                    if (messageId.id == scrollToMessageId || scrollToMessageId == null) {
                        isScrollToMessageWebViewLoaded = true
                    }
                    webContentLoaded++
                },
                previouslyLoadedHeight = rememberCachedHeight,
                finishedResizing = itemFinishedResizing

            )
        }
    }
}

object ConversationDetail {

    data class Actions(
        val onExit: (notifyUserMessage: ActionResult?) -> Unit,
        val openMessageBodyLink: (uri: Uri) -> Unit,
        val handleProtonCalendarRequest: (values: OpenProtonCalendarIntentValues) -> Unit,
        val onAddLabel: () -> Unit,
        val onAddFolder: () -> Unit,
        val showFeatureMissingSnackbar: () -> Unit,
        val onCustomizeToolbar: () -> Unit,
        val onCustomizeMessageToolbar: () -> Unit,
        val onReply: (MessageId) -> Unit,
        val onReplyAll: (MessageId) -> Unit,
        val onForward: (MessageId) -> Unit,
        val onViewContactDetails: (ContactId) -> Unit,
        val onAddContact: (basicContactInfo: BasicContactInfo) -> Unit,
        val onComposeNewMessage: (recipientAddress: String) -> Unit,
        val openComposerForDraftMessage: (messageId: MessageId) -> Unit,
        val showSnackbar: (message: String, type: ProtonSnackbarType) -> Unit,
        val recordMailboxScreenView: () -> Unit,
        val onExitWithOpenInComposer: (MessageId) -> Unit,
        val onNavigateToUpselling: (entryPoint: UpsellingEntryPoint.Feature, type: UpsellingVisibility) -> Unit,
        val onActionBarVisibilityChanged: (Boolean) -> Unit
    )
}

private fun ConversationDetailState.conversationId() =
    (conversationState as? ConversationDetailMetadataState.Data)?.conversationUiModel?.conversationId?.id.orEmpty()


object ConversationDetailScreen {

    const val ConversationIdKey = "conversation id"
    const val ScrollToMessageIdKey = "scroll to message id"
    const val IsSingleMessageMode = "is showing single message"
    const val OpenedFromLocationKey = "opened from location"
    const val ConversationDetailEntryPointNameKey = "detail origin entry point"

    data class Actions(
        val onExit: (notifyUserMessage: ActionResult?) -> Unit,
        val onExitWithError: (errorMessage: String) -> Unit,
        val onStarClick: () -> Unit,
        val onTrashClick: () -> Unit,
        val onDeleteClick: () -> Unit,
        val onUnStarClick: () -> Unit,
        val onArchiveClick: () -> Unit,
        val onSpamClick: () -> Unit,
        val onReadClick: () -> Unit,
        val onUnreadClick: () -> Unit,
        val onMoveToClick: () -> Unit,
        val onMoveToInboxClick: () -> Unit,
        val onLabelAsClick: () -> Unit,
        val onMoreActionsClick: () -> Unit,
        val onExpandMessage: (MessageIdUiModel) -> Unit,
        val onCollapseMessage: (MessageIdUiModel) -> Unit,
        val onMessageBodyLinkClicked: (messageId: MessageIdUiModel, uri: Uri) -> Unit,
        val onOpenMessageBodyLink: (uri: Uri) -> Unit,
        val onDoNotAskLinkConfirmationAgain: () -> Unit,
        val onRequestScrollTo: (MessageIdUiModel) -> Unit,
        val onScrollRequestCompleted: () -> Unit,
        val onShowAllAttachmentsForMessage: (MessageIdUiModel) -> Unit,
        val onToggleAttachmentsExpandCollapseMode: (MessageIdUiModel) -> Unit,
        val onAttachmentClicked: (AttachmentOpenMode, MessageIdUiModel, AttachmentId) -> Unit,
        val handleProtonCalendarRequest: (values: OpenProtonCalendarIntentValues) -> Unit,
        val showFeatureMissingSnackbar: () -> Unit,
        val loadImage: (messageId: MessageId?, url: String) -> MessageBodyImage?,
        val onReply: (MessageId) -> Unit,
        val onReplyAll: (MessageId) -> Unit,
        val onForward: (MessageId) -> Unit,
        val onBodyExpandCollapseButtonClicked: (MessageIdUiModel) -> Unit,
        val onMoreMessageActionsClick: (MessageId, MessageThemeOptions) -> Unit,
        val onLoadRemoteContent: (MessageId) -> Unit,
        val onLoadEmbeddedImages: (MessageId) -> Unit,
        val onLoadRemoteAndEmbeddedContent: (MessageId) -> Unit,
        val onOpenInProtonCalendar: (MessageIdUiModel) -> Unit,
        val onOpenComposer: (MessageIdUiModel) -> Unit,
        val onPrint: (MessageId) -> Unit,
        val onAvatarClicked: (ParticipantUiModel, AvatarUiModel, MessageIdUiModel?) -> Unit,
        val onAvatarImageLoadRequested: (AvatarUiModel) -> Unit,
        val onParticipantClicked: (ParticipantUiModel, AvatarUiModel?, MessageIdUiModel?) -> Unit,
        val onHiddenMessagesBannerClick: () -> Unit,
        val onMarkMessageAsLegitimate: (MessageIdUiModel, Boolean) -> Unit,
        val onUnblockSender: (MessageIdUiModel, String) -> Unit,
        val onEditScheduleSendMessage: (MessageIdUiModel) -> Unit,
        val onExitWithOpenInComposer: (MessageIdUiModel) -> Unit,
        val onRetryRsvpEventLoading: (MessageIdUiModel) -> Unit,
        val onAnswerRsvpEvent: (MessageIdUiModel, RsvpAnswer) -> Unit,
        val onMessage: (String) -> Unit,
        val onUnsnoozeMessage: () -> Unit,
        val onSnooze: () -> Unit,
        val onActionBarVisibilityChanged: (Boolean) -> Unit,
        val onUnsubscribeFromNewsletter: (MessageIdUiModel) -> Unit,
        val onReportPhishing: (MessageId) -> Unit,
        val onDownloadImage: (MessageId, String) -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onExit = {},
                onExitWithError = {},
                onStarClick = {},
                onTrashClick = {},
                onDeleteClick = {},
                onSpamClick = {},
                onArchiveClick = {},
                onUnStarClick = {},
                onUnreadClick = {},
                onReadClick = {},
                onMoveToClick = {},
                onMoveToInboxClick = {},
                onLabelAsClick = {},
                onMoreActionsClick = {},
                onExpandMessage = {},
                onCollapseMessage = {},
                onMessageBodyLinkClicked = { _, _ -> },
                onOpenMessageBodyLink = {},
                onDoNotAskLinkConfirmationAgain = {},
                onRequestScrollTo = {},
                onScrollRequestCompleted = {},
                onShowAllAttachmentsForMessage = {},
                onAttachmentClicked = { _, _, _ -> },
                onToggleAttachmentsExpandCollapseMode = {},
                handleProtonCalendarRequest = {},
                showFeatureMissingSnackbar = {},
                loadImage = { _, _ -> null },
                onReply = {},
                onReplyAll = {},
                onForward = {},
                onBodyExpandCollapseButtonClicked = {},
                onMoreMessageActionsClick = { _, _ -> },
                onLoadRemoteContent = {},
                onLoadEmbeddedImages = {},
                onLoadRemoteAndEmbeddedContent = {},
                onOpenInProtonCalendar = {},
                onOpenComposer = {},
                onPrint = { _ -> },
                onAvatarClicked = { _, _, _ -> },
                onAvatarImageLoadRequested = {},
                onParticipantClicked = { _, _, _ -> },
                onHiddenMessagesBannerClick = {},
                onMarkMessageAsLegitimate = { _, _ -> },
                onUnblockSender = { _, _ -> },
                onEditScheduleSendMessage = {},
                onExitWithOpenInComposer = {},
                onRetryRsvpEventLoading = {},
                onAnswerRsvpEvent = { _, _ -> },
                onMessage = {},
                onUnsnoozeMessage = {},
                onSnooze = {},
                onActionBarVisibilityChanged = {},
                onUnsubscribeFromNewsletter = {},
                onReportPhishing = {},
                onDownloadImage = { _, _ -> }
            )
        }
    }
}

@Composable
@AdaptivePreviews
private fun ConversationDetailScreenPreview(
    @PreviewParameter(ConversationDetailsPreviewProvider::class) state: ConversationDetailState
) {
    ProtonTheme {
        ProtonTheme {
            ConversationDetailScreen(
                state = state,
                actions = ConversationDetailScreen.Actions.Empty,
                scrollToMessageId = null,
                isSingleMessageMode = false
            )
        }
    }
}

object ConversationDetailScreenTestTags {

    const val RootItem = "ConversationDetailScreenRootItem"
    const val MessagesList = "ConversationDetailMessagesList"
}
