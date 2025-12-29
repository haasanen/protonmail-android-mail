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

package ch.protonmail.android.mailspotlight.presentation.ui

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ch.protonmail.android.design.compose.component.ProtonTextButton
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcommon.presentation.model.string
import ch.protonmail.android.mailspotlight.presentation.R
import ch.protonmail.android.mailspotlight.presentation.model.AppVersionUiModel
import ch.protonmail.android.mailspotlight.presentation.model.FeatureItem
import ch.protonmail.android.mailspotlight.presentation.viewmodel.FeatureSpotlightViewModel
import ch.protonmail.android.uicomponents.BottomNavigationBarSpacer
import ch.protonmail.android.uicomponents.TopNavigationBarSpacer
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun FeatureSpotlightScreen(onDismiss: () -> Unit) {
    val viewModel = hiltViewModel<FeatureSpotlightViewModel>()
    val appVersion = viewModel.appVersion
    val features = viewModel.features
    val closeScreenEffect = viewModel.closeScreenEvent

    LaunchedEffect(Unit) {
        closeScreenEffect.collect { onDismiss() }
    }

    FeatureSpotlightScreen(
        appVersionUiModel = appVersion,
        featureItems = features,
        onButtonClick = { viewModel.saveScreenShown() }
    )
}

@Composable
private fun FeatureSpotlightScreen(
    appVersionUiModel: AppVersionUiModel,
    featureItems: ImmutableList<FeatureItem>,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    var footerHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val hazeState = rememberHazeState()

    val configuration = LocalConfiguration.current
    val topOffset = configuration.screenHeightDp.dp / 8 // (12.5%)

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(ProtonTheme.colors.brandMinus40, ProtonTheme.colors.backgroundNorm)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ProtonDimens.Spacing.Large)
                .hazeSource(hazeState)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopNavigationBarSpacer()

            Spacer(modifier = Modifier.height(topOffset))

            Image(
                painter = painterResource(id = R.drawable.spotlight_celebration),
                contentDescription = NO_CONTENT_DESCRIPTION
            )

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.ExtraLarge))

            Text(
                text = appVersionUiModel.text.string(),
                style = ProtonTheme.typography.titleMedium,
                color = ProtonTheme.colors.textWeak,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Medium))

            Text(
                text = stringResource(R.string.spotlight_screen_title),
                style = ProtonTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ProtonDimens.Spacing.Jumbo))

            Column(
                modifier = Modifier.padding(horizontal = ProtonDimens.Spacing.Large),
                verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.ExtraLarge)
            ) {
                featureItems.forEach {
                    FeatureRow(it.icon, it.title.string(), it.description.string())
                }
            }

            Spacer(Modifier.height(footerHeight + ProtonDimens.Spacing.Large))
        }

        @Suppress("MagicNumber")
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { size ->
                    footerHeight = with(density) { size.height.toDp() }
                }
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        tint = HazeTint(color = Color.Transparent),
                        fallbackTint = HazeTint(color = ProtonTheme.colors.backgroundNorm),
                        noiseFactor = 0f,
                        blurRadius = BLUR_RADIUS
                    )
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        preferPerformance = true,
                        startIntensity = 0f,
                        endIntensity = .5f
                    )
                }
        ) {
            ProtonTextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ProtonDimens.Spacing.Large)
                    .padding(bottom = ProtonDimens.Spacing.Large)
                    .background(
                        color = ProtonTheme.colors.brandNorm,
                        shape = ProtonTheme.shapes.massive
                    ),
                onClick = onButtonClick
            ) {
                Text(
                    text = stringResource(R.string.spotlight_screen_got_it),
                    style = ProtonTheme.typography.titleMedium,
                    color = ProtonTheme.colors.textInverted,
                    textAlign = TextAlign.Center
                )
            }

            BottomNavigationBarSpacer()
        }
    }
}

@Composable
private fun FeatureRow(
    @DrawableRes icon: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Large)
    ) {
        Box(
            modifier = Modifier
                .size(ProtonDimens.IconSize.Large)
                .background(
                    color = ProtonTheme.colors.brandMinus40,
                    shape = ProtonTheme.shapes.large
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = icon),
                contentDescription = null,
                Modifier.size(ProtonDimens.IconSize.Small)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(ProtonDimens.Spacing.Standard)) {
            Text(
                text = title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = ProtonTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = ProtonTheme.typography.bodyMedium
            )
        }
    }
}

private val BLUR_RADIUS = 2.dp

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun FeatureSpotlightScreenPreview() {
    val version = AppVersionUiModel(
        TextUiModel(value = R.string.spotlight_screen_version_text, formatArgs = arrayOf("7.7.0"))
    )

    val placeholderText = "Feature"
    val placeholderDescription = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."

    val features = buildList {
        repeat(15) {
            add(
                FeatureItem(
                    icon = R.drawable.ic_proton_checkmark,
                    title = TextUiModel(placeholderText),
                    description = TextUiModel(placeholderDescription)
                )
            )
        }
    }.toImmutableList()

    FeatureSpotlightScreen(version, features, {})
}
