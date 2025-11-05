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

package ch.protonmail.android.maildetail.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.ProtonHorizontallyCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonSnackbarHostState
import ch.protonmail.android.design.compose.component.ProtonSnackbarType
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.domain.model.ConversationId
import ch.protonmail.android.mailcommon.presentation.ConsumableLaunchedEffect
import ch.protonmail.android.mailcommon.presentation.R
import ch.protonmail.android.mailcommon.presentation.model.ActionResult
import ch.protonmail.android.mailcommon.presentation.ui.CommonTestTags
import ch.protonmail.android.maildetail.presentation.model.DynamicViewPagerState
import ch.protonmail.android.maildetail.presentation.model.Error
import ch.protonmail.android.maildetail.presentation.model.MessageIdUiModel
import ch.protonmail.android.maildetail.presentation.model.Page
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailAction
import ch.protonmail.android.maildetail.presentation.model.PagedConversationDetailState
import ch.protonmail.android.maildetail.presentation.viewmodel.PagedConversationDetailViewModel
import ch.protonmail.android.uicomponents.snackbar.DismissableSnackbarHost

@Composable
fun PagedConversationDetailScreen(
    modifier: Modifier = Modifier,
    actions: ConversationDetail.Actions,
    viewModel: PagedConversationDetailViewModel = hiltViewModel()
) {

    val effects = viewModel.effects.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    ConsumableLaunchedEffect(effects.error) {
        when (it) {
            Error.NETWORK -> actions.showSnackbar(
                context.getString(R.string.presentation_general_connection_error),
                ProtonSnackbarType.ERROR
            )

            Error.OTHER -> actions.showSnackbar(
                context.getString(R.string.presentation_error_general),
                ProtonSnackbarType.ERROR
            )
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val currentState = state) {
        is PagedConversationDetailState.Error,
        is PagedConversationDetailState.Loading -> ProtonHorizontallyCenteredProgress()

        is PagedConversationDetailState.Ready -> {
            ConsumableLaunchedEffect(currentState.dynamicViewPagerState.exit) {
                actions.onExit(null)
            }
            PagedConversationDetailScreen(
                modifier = modifier,
                conversationDetailActions = actions,
                state = currentState,
                showUndoableOperationSnackbar = { action -> actions.showUndoableOperationSnackbar(action) },
                onPagerAction = { viewModel.submit(it) }
            )
        }
    }
}

@Composable
private fun PagedConversationDetailScreen(
    modifier: Modifier = Modifier,
    conversationDetailActions: ConversationDetail.Actions,
    state: PagedConversationDetailState.Ready,
    showUndoableOperationSnackbar: (notifyUserMessage: ActionResult?) -> Unit,
    onPagerAction: (PagedConversationDetailAction) -> Unit
) {
    val onTopbarBackClicked = { conversationDetailActions.onExit(null) }
    val actions = state.autoAdvanceEnabled.takeIf { it }?.let {
        conversationDetailActions.copy(
            onExit = {
                if (state.autoAdvanceEnabled) {
                    showUndoableOperationSnackbar(it)
                    onPagerAction(PagedConversationDetailAction.AutoAdvance)
                } else {
                    conversationDetailActions.onExit(it)
                }
            }
        )
    } ?: conversationDetailActions

    val conversationDetailScreenArgs =
        ConversationDetail.NavigationArgs(
            singleMessageMode = state.navigationArgs.singleMessageMode,
            openedFromLocation = state.navigationArgs.openedFromLocation,
            conversationEntryPoint = state.navigationArgs.conversationEntryPoint,
            initialScrollToMessageId = null
        )

    ConversationPager(
        modifier = modifier,
        conversationActions = actions,
        state = state.dynamicViewPagerState,
        conversationDetailScreenNavArgs = conversationDetailScreenArgs,
        onPagerAction = onPagerAction,
        onTopBarExit = onTopbarBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun ConversationPager(
    modifier: Modifier = Modifier,
    conversationActions: ConversationDetail.Actions,
    conversationDetailScreenNavArgs: ConversationDetail.NavigationArgs,
    state: DynamicViewPagerState,
    onPagerAction: (PagedConversationDetailAction) -> Unit,
    onTopBarExit: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentPageIndex ?: 0,
        pageCount = { state.pages.size }
    )

    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.settledPage }
            .collect {
                if (pagerState.settledPage == state.focusPageIndex) {
                    onPagerAction(PagedConversationDetailAction.ClearFocusPage)
                } else {
                    onPagerAction(PagedConversationDetailAction.SetSettledPage(it))
                }
            }
    }

    ConsumableLaunchedEffect(state.scrollToPage) {
        state.currentPageIndex?.let { pagerState.animateScrollToPage(it + 1) }
    }

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            state.focusPageIndex?.let {
                // refocusing the pager in the middle so we can always swipe left or right
                // for example if we swipe right to the next item, index is 2 and the pager thinks we are at the end
                // and we can therefore not swipe right again.  So as we move to next we shift the pages left and set
                // the focus page index to the new position of the page we are looking at. The pager instantly moves,
                // our page is now at index 1 and we can swipe right again if we want
                pagerState.scrollToPage(it)
            }
        }
    }

    val snackbarHostState = remember { ProtonSnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(snapAnimationSpec = null)
    // When SubjectHeader is first time composed, we need to get the its actual height to be able to calculate yOffset
    // for collapsing effect
    val subjectHeaderSizeCallback: (Int) -> Unit = {
        scrollBehavior.state.heightOffsetLimit = -it.toFloat()
    }

    var currentTopBarState by remember { mutableStateOf(TopBarState(scrollBehavior)) }
    val onTopBarStateUpdated = { state: TopBarState ->
        // update the state to the current visible page
        currentTopBarState = state
    }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = ProtonTheme.colors.backgroundNorm,
        snackbarHost = {
            DismissableSnackbarHost(
                modifier = Modifier.testTag(CommonTestTags.SnackbarHost),
                protonSnackbarHostState = snackbarHostState
            )
        },
        topBar = {
            DetailScreenTopBar(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = currentTopBarState.scrollBehavior.state.heightOffset / 2f
                    },
                title = currentTopBarState.title.value,
                isStarred = currentTopBarState.isStarred.value,
                messageCount = currentTopBarState.messages.value,
                actions = DetailScreenTopBar.Actions(
                    onBackClick = onTopBarExit,
                    onStarClick = { currentTopBarState.onStarClick() },
                    onUnStarClick = { currentTopBarState.onStarUnClick() }
                ),
                subjectHeaderSizeCallback = subjectHeaderSizeCallback,
                topAppBarState = currentTopBarState.scrollBehavior.state,
                isDirectionForwards = { pagerState.lastScrolledForward }
            )
        }
    ) { innerPadding ->
        // if we apply the padding in this view it breaks the layout, it must applied in the DetailsScreen
        val innerPadding = remember { mutableStateOf(innerPadding) }

        Pager(
            modifier = modifier,
            pagerState = pagerState,
            innerPadding = innerPadding.value,
            pages = state.pages,
            conversationActions = conversationActions,
            conversationDetailNavigationArgs = conversationDetailScreenNavArgs,
            onTopBarStateUpdated = onTopBarStateUpdated,
            canScroll = state.userScrollEnabled
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Pager(
    modifier: Modifier,
    pagerState: PagerState,
    innerPadding: PaddingValues,
    conversationActions: ConversationDetail.Actions,
    conversationDetailNavigationArgs: ConversationDetail.NavigationArgs,
    pages: List<Page>,
    canScroll: Boolean,
    onTopBarStateUpdated: (TopBarState) -> Unit
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondViewportPageCount = 0,
        // Disable user scrolling while the page is being updated through ViewModel methods.
        userScrollEnabled = canScroll
    ) { page ->
        val item = pages[page]

        when (item) {
            is Page.Conversation -> {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(snapAnimationSpec = null)
                val topBarHostState = remember { TopBarState(scrollBehavior) }
                if (page == pagerState.targetPage) {
                    onTopBarStateUpdated(topBarHostState)
                }
                Page(
                    innerPadding = innerPadding,
                    topBarHostState = topBarHostState,
                    conversationActions = conversationActions,
                    navigationArgs = conversationDetailNavigationArgs.copy(
                        initialScrollToMessageId = item.cursorId.messageId?.let { MessageIdUiModel(it) }
                    ),
                    conversationId = item.cursorId.conversationId
                )
            }

            Page.End -> {
                // should be handled by indexing to never reach the end page (view pager can't scroll further)
            }

            Page.Error -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.presentation_error_general),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Page.Loading -> ProtonHorizontallyCenteredProgress()
        }
    }
}

@Composable
private fun Page(
    innerPadding: PaddingValues,
    topBarHostState: TopBarState,
    conversationActions: ConversationDetail.Actions,
    navigationArgs: ConversationDetail.NavigationArgs,
    conversationId: ConversationId
) {
    ConversationDetailScreen(
        padding = innerPadding,
        actions = conversationActions,
        conversationId = conversationId,
        navigationArgs = navigationArgs,
        topBarState = topBarHostState
    )
}

object PagedConversationDetailScreen {

    const val ViewModeIsConversation = "View mode"
}

