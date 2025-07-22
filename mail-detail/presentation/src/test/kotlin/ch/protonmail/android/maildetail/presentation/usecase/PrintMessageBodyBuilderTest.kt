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

package ch.protonmail.android.maildetail.presentation.usecase

import ch.protonmail.android.maildetail.presentation.usecase.print.PrintMessageDocumentBuilder
import ch.protonmail.android.maildetail.presentation.usecase.print.PrintMessageHeaderBuilder
import ch.protonmail.android.testdata.maildetail.MessageDetailHeaderUiModelTestData
import ch.protonmail.android.testdata.message.MessageBodyUiModelTestData
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PrintMessageBodyBuilderTest {

    private val headerBuilder = mockk<PrintMessageHeaderBuilder>()
    private lateinit var builder: PrintMessageDocumentBuilder

    @BeforeTest
    fun setup() {
        builder = PrintMessageDocumentBuilder(headerBuilder)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should build the document by concatenating header and body`() {
        // Given
        val header = "Header text\n"
        val subject = "subject"
        val headerUiModel = MessageDetailHeaderUiModelTestData.messageDetailHeaderUiModel
        val bodyUiModel = MessageBodyUiModelTestData.plainTextMessageBodyUiModel
        every { headerBuilder.buildHeader(subject, headerUiModel, bodyUiModel.attachments) } returns header

        // When
        val body = builder.buildDocument(subject, headerUiModel, bodyUiModel)

        // Then
        assertEquals(header + bodyUiModel.messageBody, body)
    }
}
