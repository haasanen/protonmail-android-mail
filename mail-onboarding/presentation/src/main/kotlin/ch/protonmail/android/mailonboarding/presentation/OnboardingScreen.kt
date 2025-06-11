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

package ch.protonmail.android.mailonboarding.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailonboarding.presentation.model.OnboardingState
import ch.protonmail.android.mailonboarding.presentation.model.OnboardingUiModel
import ch.protonmail.android.mailonboarding.presentation.ui.OnboardingButton
import ch.protonmail.android.mailonboarding.presentation.ui.OnboardingContent
import ch.protonmail.android.mailonboarding.presentation.ui.OnboardingIndexDots
import ch.protonmail.android.mailonboarding.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Composable
fun OnboardingScreen(
    exitAction: () -> Unit,
    onUpsellingNavigation: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    OnboardingScreen(state, onUpsellingNavigation, exitAction)
}

@Composable
private fun OnboardingScreen(
    state: OnboardingState,
    onUpsellingNavigation: () -> Unit,
    exitAction: () -> Unit
) {
    val isEligibleForUpselling = state is OnboardingState.ToUpsell
    val isUpsellingEligibilityPending = state is OnboardingState.Loading

    val onExitAction = if (isEligibleForUpselling || isUpsellingEligibilityPending) {
        { onUpsellingNavigation() }
    } else {
        exitAction
    }

    val contentMap = listOfNotNull(
        OnboardingUiModel(
            illustrationId = R.drawable.illustration_onboarding_welcome,
            title = R.string.onboarding_welcome_title,
            headlineId = R.string.onboarding_welcome_headline,
            descriptionId = R.string.onboarding_welcome_description
        ),
        OnboardingUiModel(
            illustrationId = R.drawable.illustration_onboarding_rebuilt,
            title = R.string.onboarding_rebuilt_title,
            headlineId = R.string.onboarding_rebuilt_headline,
            descriptionId = R.string.onboarding_rebuilt_description
        ),
        OnboardingUiModel(
            illustrationId = R.drawable.illustration_onboarding_feedback,
            title = R.string.onboarding_feedback_title,
            headlineId = R.string.onboarding_feedback_headline,
            descriptionId = R.string.onboarding_feedback_description
        ),
        if (isEligibleForUpselling) OnboardingUiModel.Empty else null
    )

    val viewCount = contentMap.size
    val pagerState = rememberPagerState(pageCount = { viewCount })

    var isSwipingToUpsellingPage by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState, viewCount) {
        snapshotFlow {
            Triple(pagerState.currentPage, pagerState.targetPage, viewCount)
        }
            .distinctUntilChanged()
            .map { (currentPage, targetPage) ->

                val fromPage = currentPage + 1
                val toPage = targetPage + 1

                // return true if we're showing upselling and are about to swipe to last page
                isEligibleForUpselling && fromPage == viewCount - 1 && toPage == viewCount
            }
            .collect { isSwipingToUpsellingPage = it }
    }

    LaunchedEffect(isSwipingToUpsellingPage) {
        if (isSwipingToUpsellingPage) {
            onUpsellingNavigation()
        }
    }

    Column(
        modifier = Modifier
            .testTag(OnboardingScreenTestTags.RootItem)
            .background(ProtonTheme.colors.backgroundInvertedNorm)
            .padding(ProtonDimens.Spacing.Large)
            .verticalScroll(rememberScrollState())
    ) {

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = pagerState.pageCount // pre-render all three pages to ensure consistent sizing.
        ) { pageIndex ->
            OnboardingContent(content = contentMap[pageIndex])
        }

        if (!isEligibleForUpselling || pagerState.currentPage != viewCount.minus(1)) {
            OnboardingButton(onExitAction, pagerState, viewCount)
            OnboardingIndexDots(pagerState, viewCount)
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    ProtonTheme {
        OnboardingScreen(state = OnboardingState.NoUpsell, onUpsellingNavigation = {}, exitAction = {})
    }
}

object OnboardingScreenTestTags {

    const val RootItem = "OnboardingScreenRootItem"
    const val TopBarRootItem = "OnboardingTopBarRootItem"
    const val CloseButton = "OnboardingScreenCloseButton"
    const val BottomButton = "OnboardingScreenBottomButton"
    const val OnboardingImage = "OnboardingScreenWelcomeImage"
}
