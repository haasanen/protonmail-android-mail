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
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformationsOverride.LoadEmbeddedImages
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformationsOverride.LoadRemoteContent
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformationsOverride.LoadRemoteContentAndEmbeddedImages
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformationsOverride.ToggleQuotedText
import ch.protonmail.android.mailmessage.domain.model.MessageTheme
import ch.protonmail.android.mailmessage.domain.model.MessageThemeOptions

object MessageBodyTransformationsMapper {

    fun applyOverride(
        transformations: MessageBodyTransformations,
        override: MessageBodyTransformationsOverride?
    ): MessageBodyTransformations {
        return override?.let {
            when (override) {
                LoadEmbeddedImages -> transformations.copy(hideEmbeddedImages = false)
                LoadRemoteContent -> transformations.copy(hideRemoteContent = false)

                LoadRemoteContentAndEmbeddedImages -> transformations.copy(
                    hideRemoteContent = false,
                    hideEmbeddedImages = false
                )

                ToggleQuotedText -> transformations.copy(showQuotedText = !transformations.showQuotedText)

                is MessageBodyTransformationsOverride.ViewInLightMode -> {
                    transformations.copy(
                        messageThemeOptions = MessageThemeOptions(
                            currentTheme = override.currentTheme,
                            themeOverride = MessageTheme.Light
                        )
                    )
                }

                is MessageBodyTransformationsOverride.ViewInDarkMode -> {
                    transformations.copy(
                        messageThemeOptions = MessageThemeOptions(
                            currentTheme = override.currentTheme,
                            themeOverride = MessageTheme.Dark
                        )
                    )
                }
            }
        } ?: transformations
    }
}
