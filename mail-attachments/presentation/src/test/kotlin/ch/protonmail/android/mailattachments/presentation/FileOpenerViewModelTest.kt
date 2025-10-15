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

package ch.protonmail.android.mailattachments.presentation

import android.net.Uri
import app.cash.turbine.test
import ch.protonmail.android.mailattachments.presentation.ui.OpenAttachmentInput
import ch.protonmail.android.mailattachments.presentation.viewmodel.FileOpenerViewModel
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FileOpenerViewModelTest {

    private lateinit var viewModel: FileOpenerViewModel

    @BeforeTest
    fun setup() {
        viewModel = FileOpenerViewModel()
    }

    @Test
    fun `requestOpen should emit event to openEvent flow`() = runTest {
        // Given
        val input = OpenAttachmentInput(mockk<Uri>(), mimeType = "image/png")

        // When + Then
        viewModel.openEvent.test {
            viewModel.requestOpen(input)

            assertEquals(input, awaitItem())
        }
    }

    @Test
    fun `requestOpen should emit multiple events sequentially`() = runTest {
        // Given
        val input1 = OpenAttachmentInput(mockk<Uri>(), mimeType = "image/png")
        val input2 = OpenAttachmentInput(mockk<Uri>(), mimeType = "image/webp")

        // When + Then
        viewModel.openEvent.test {
            viewModel.requestOpen(input1)
            assertEquals(input1, awaitItem())

            viewModel.requestOpen(input2)
            assertEquals(input2, awaitItem())
        }
    }

    @Test
    fun `openEvent should not emit any events initially`() = runTest {
        // When + Then
        viewModel.openEvent.test {
            expectNoEvents()
        }
    }
}
