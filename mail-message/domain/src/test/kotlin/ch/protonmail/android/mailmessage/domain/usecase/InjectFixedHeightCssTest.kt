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

package ch.protonmail.android.mailmessage.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InjectHeightFixCssTest {

    private val injectHeightFixCss = InjectFixedHeightCss()

    @Test
    fun `should inject CSS before closing head tag`() {
        // Given
        val html = """
            <html>
            <head>
                <title>Test</title>
            </head>
            <body>Content</body>
            </html>
        """.trimIndent()

        // When
        val result = injectHeightFixCss(html)

        // Then
        assertTrue(result.contains("html, body { height: auto !important; }"))
        assertTrue(result.indexOf("height: auto") < result.indexOf("</head>"))
    }

    @Test
    fun `should inject CSS after opening head tag when no closing tag exists`() {
        // Given
        val html = """
            <html>
            <head>
            <body>Content</body>
            </html>
        """.trimIndent()

        // When
        val result = injectHeightFixCss(html)

        // Then
        assertTrue(result.contains("html, body { height: auto !important; }"))
        assertTrue(result.indexOf("height: auto") > result.indexOf("<head>"))
    }

    @Test
    fun `should prepend CSS when no head tag exists`() {
        // Given
        val html = """
            <html>
            <body>Content</body>
            </html>
        """.trimIndent()

        // When
        val result = injectHeightFixCss(html)

        // Then
        assertTrue(result.startsWith("<style>"))
        assertTrue(result.contains("html, body { height: auto !important; }"))
    }

    @Test
    fun `should handle case-insensitive head tags`() {
        // Given
        val html = """
            <html>
            <HEAD>
                <title>Test</title>
            </HEAD>
            <body>Content</body>
            </html>
        """.trimIndent()

        // When
        val result = injectHeightFixCss(html)

        // Then
        assertTrue(result.contains("html, body { height: auto !important; }"))
        assertTrue(result.indexOf("height: auto") < result.indexOf("</HEAD>"))
    }

    @Test
    fun `should only inject CSS once even with multiple head tags`() {
        // Given
        val html = """
            <html>
            <head></head>
            <head></head>
            <body>Content</body>
            </html>
        """.trimIndent()

        // When
        val result = injectHeightFixCss(html)

        // Then
        val occurrences = result.split("height: auto !important").size - 1
        assertEquals(1, occurrences, "CSS should only be injected once")
    }

    @Test
    fun `should handle empty HTML`() {
        // Given
        val html = ""

        // When
        val result = injectHeightFixCss(html)

        // Then
        assertTrue(result.startsWith("<style>"))
        assertTrue(result.contains("html, body { height: auto !important; }"))
    }
}
