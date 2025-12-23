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

package ch.protonmail.android.mailspotlight.presentation

import app.cash.turbine.test
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.AppInformation
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailspotlight.domain.usecase.MarkFeatureSpotlightSeen
import ch.protonmail.android.mailspotlight.presentation.viewmodel.FeatureSpotlightViewModel
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
internal class FeatureSpotlightViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appInformation = AppInformation(appVersionName = "7.7.0")
    private val markFeatureSpotlightSeen = mockk<MarkFeatureSpotlightSeen>()
    private lateinit var viewModel: FeatureSpotlightViewModel

    @BeforeTest
    fun setup() {
        viewModel = FeatureSpotlightViewModel(
            appInformation = appInformation,
            markFeatureSpotlightSeen = markFeatureSpotlightSeen
        )
    }

    @AfterTest
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `appVersion contains correct text resource with version name`() {
        // When + Then
        val appVersion = viewModel.appVersion
        val textModel = appVersion.text as TextUiModel.TextResWithArgs
        assertEquals(R.string.spotlight_screen_version_text, textModel.value)
        assertEquals(listOf(appInformation.appVersionName), textModel.formatArgs)
    }

    @Test
    fun `features list contains exactly four items`() {
        // Then
        assertEquals(4, viewModel.features.size)
    }

    @Test
    fun `features list contains discreet icon feature as first item`() {
        // When + Then
        val firstFeature = viewModel.features[0]
        assertEquals(R.drawable.ic_palette, firstFeature.icon)
        assertEquals(
            TextUiModel.TextRes(R.string.spotlight_feature_item_discreet_icon_title),
            firstFeature.title
        )
        assertEquals(
            TextUiModel.TextRes(R.string.spotlight_feature_item_discreet_icon_description),
            firstFeature.description
        )
    }

    @Test
    fun `features list contains encryption locks feature as second item`() {
        // When + Then
        val secondFeature = viewModel.features[1]
        assertEquals(R.drawable.ic_lock, secondFeature.icon)
        assertEquals(
            TextUiModel.TextRes(R.string.spotlight_feature_item_encryption_locks_title),
            secondFeature.title
        )
        assertEquals(
            TextUiModel.TextRes(R.string.spotlight_feature_item_encryption_locks_description),
            secondFeature.description
        )
    }

    @Test
    fun `features list contains tracking protection feature as third item`() {
        // When + Then
        val thirdFeature = viewModel.features[2]
        assertEquals(R.drawable.ic_shield_check, thirdFeature.icon)
        assertEquals(
            TextUiModel.TextRes(R.string.spotlight_feature_item_tracking_protection_title),
            thirdFeature.title
        )
        assertEquals(
            TextUiModel.TextRes(R.string.spotlight_feature_item_tracking_protection_description),
            thirdFeature.description
        )
    }

    @Test
    fun `features list contains headers html feature as fourth item`() {
        // When + Then
        val fourthFeature = viewModel.features[3]
        assertEquals(R.drawable.ic_code, fourthFeature.icon)
        assertEquals(
            TextUiModel.TextRes(R.string.spotlight_feature_item_headers_html_title),
            fourthFeature.title
        )
        assertEquals(
            TextUiModel.TextRes(R.string.spotlight_feature_item_headers_html_description),
            fourthFeature.description
        )
    }

    @Test
    fun `saveScreenShown calls markFeatureSpotlightSeen`() = runTest {
        // Given
        coEvery { markFeatureSpotlightSeen() } returns Unit.right()

        // When
        viewModel.saveScreenShown()

        // Then
        coVerify(exactly = 1) { markFeatureSpotlightSeen() }
    }

    @Test
    fun `saveScreenShown emits closeScreenEvent after marking seen`() = runTest {
        // Given
        coEvery { markFeatureSpotlightSeen() } returns Unit.right()

        // When + Then
        viewModel.closeScreenEvent.test {
            viewModel.saveScreenShown()
            assertEquals(Unit, awaitItem())
        }
    }
}
