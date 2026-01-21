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

package ch.protonmail.android.uitest.screen.sidebar

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.model.CappedNumberUiModel
import ch.protonmail.android.maillabel.presentation.MailLabelUiModel
import ch.protonmail.android.mailsidebar.presentation.common.ProtonSidebarLazy
import ch.protonmail.android.mailsidebar.presentation.label.SidebarItemWithCounterTestTags
import ch.protonmail.android.mailsidebar.presentation.label.sidebarLabelItems
import ch.protonmail.android.mailsidebar.presentation.label.sidebarSystemLabelItems
import ch.protonmail.android.test.annotations.suite.SmokeTest
import ch.protonmail.android.testdata.maillabel.MailLabelUiModelTestData
import ch.protonmail.android.uitest.util.HiltInstrumentedTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@SmokeTest
@HiltAndroidTest
internal class SidebarWithCounterItemTest : HiltInstrumentedTest() {

    private val countersNode by lazy {
        composeTestRule.onNodeWithTag(SidebarItemWithCounterTestTags.Counter, useUnmergedTree = true)
    }

    @Test
    fun sidebarSystemLabelCounterDisplaysValueWhenAvailable() {
        // Given
        val systemFolder = MailLabelUiModelTestData.spamFolder.copy(count = counterValue)

        // When
        setupLabelItem(systemFolder)

        // Then
        countersNode.assertTextEquals(counterValue.value.toString())
    }

    @Test
    fun sidebarSystemLabelItemDisplaysNoCounterValueWhenEmpty() {
        // Given
        val systemFolder = MailLabelUiModelTestData.spamFolder.copy(count = emptyValue)

        // When
        setupLabelItem(systemFolder)

        // Then
        countersNode.assertDoesNotExist()
    }

    @Test
    fun sidebarSystemLabelItemDisplaysCappedCounterValueWhenAboveThreshold() {
        // Given
        val systemFolder = MailLabelUiModelTestData.spamFolder.copy(count = counterValueCapped)

        // When
        setupLabelItem(systemFolder)

        // Then
        countersNode.assertTextEquals(counterValueCapped.cap.toString())
    }

    @Test
    fun sidebarCustomLabelCounterDisplaysValueWhenAvailable() {
        // Given
        val customLabel = MailLabelUiModelTestData.customLabelList.first().copy(count = counterValue)

        // When
        setupLabelItem(customLabel)

        // Then
        countersNode.assertTextEquals(counterValue.value.toString())
    }


    private fun setupLabelItem(item: MailLabelUiModel) {
        composeTestRule.setContent {
            ProtonTheme {
                ProtonSidebarLazy {
                    when (item) {
                        is MailLabelUiModel.Custom -> sidebarLabelItems(listOf(item)) {}
                        is MailLabelUiModel.System -> sidebarSystemLabelItems(listOf(item)) {}
                    }
                }
            }
        }
    }

    private companion object {

        val emptyValue = CappedNumberUiModel.Empty
        val counterValue = CappedNumberUiModel.Exact(10)
        val counterValueCapped = CappedNumberUiModel.Capped(9999)
    }
}
