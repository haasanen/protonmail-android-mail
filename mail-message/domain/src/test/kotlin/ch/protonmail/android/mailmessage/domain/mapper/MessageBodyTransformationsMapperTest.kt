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

package ch.protonmail.android.mailmessage.domain.mapper

import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformationsOverride
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
internal class MessageBodyTransformationsMapperTest(
    @Suppress("unused") private val testName: String,
    private val base: MessageBodyTransformations,
    private val override: MessageBodyTransformationsOverride,
    private val expected: MessageBodyTransformations
) {

    @Test
    fun `resolves the correct new transformations`() {
        val actual = MessageBodyTransformationsMapper.applyOverride(base, override)
        assertEquals(expected, actual)
    }

    companion object {

        private val baseTransformation = MessageBodyTransformations(
            showQuotedText = false,
            hideEmbeddedImages = null,
            hideRemoteContent = null
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "overrides load remote content value",
                baseTransformation,
                MessageBodyTransformationsOverride.LoadRemoteContent,
                baseTransformation.copy(hideRemoteContent = false)
            ),
            arrayOf(
                "overrides load embedded content value",
                baseTransformation,
                MessageBodyTransformationsOverride.LoadEmbeddedImages,
                baseTransformation.copy(hideEmbeddedImages = false)
            ),
            arrayOf(
                "overrides load embedded and remote content value",
                baseTransformation,
                MessageBodyTransformationsOverride.LoadRemoteContentAndEmbeddedImages,
                baseTransformation.copy(hideEmbeddedImages = false, hideRemoteContent = false)
            ),
            arrayOf(
                "toggles quoted text value",
                baseTransformation,
                MessageBodyTransformationsOverride.ToggleQuotedText,
                baseTransformation.copy(showQuotedText = !baseTransformation.showQuotedText)
            )
        )
    }
}
