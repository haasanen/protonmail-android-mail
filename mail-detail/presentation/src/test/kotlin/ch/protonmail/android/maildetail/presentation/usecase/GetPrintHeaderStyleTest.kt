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

import java.io.ByteArrayInputStream
import java.io.IOException
import android.content.Context
import android.content.res.Resources
import ch.protonmail.android.maildetail.presentation.usecase.print.GetPrintHeaderStyle
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class GetPrintHeaderStyleTest {

    private val context = mockk<Context>()

    private lateinit var getPrintHeaderStyle: GetPrintHeaderStyle


    @BeforeTest
    fun setup() {
        getPrintHeaderStyle = GetPrintHeaderStyle(context)
    }

    @AfterTest
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `should return the string upon success`() {
        // Given
        val expectedContent = "test content"
        val inputStream = ByteArrayInputStream(expectedContent.toByteArray())
        val resources = mockk<Resources>()

        every { context.resources } returns resources
        every { resources.openRawResource(any()) } returns inputStream

        // When
        val result = getPrintHeaderStyle()

        // Then
        assertTrue(result.isRight())
        assertEquals(expectedContent, result.getOrNull())
    }

    @Test
    fun `should return error when reading file fails`() {
        // Given
        val resources = mockk<Resources>()

        every { context.resources } returns resources
        every { resources.openRawResource(any()) } throws IOException("File not found")

        // When
        val result = getPrintHeaderStyle()

        // Then
        assertTrue(result.isLeft())
    }
}
