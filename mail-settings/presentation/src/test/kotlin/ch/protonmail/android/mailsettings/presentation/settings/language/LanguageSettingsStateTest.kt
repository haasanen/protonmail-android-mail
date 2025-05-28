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

package ch.protonmail.android.mailsettings.presentation.settings.language

import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageSettingsStateTest {

    @Test
    fun `Given null selected language then returned uiModel is the SystemDefaultLanguage`() {
        val appLanguage: AppLanguage? = null

        assertEquals(SystemDefaultLanguage, appLanguage.toUiModel())
    }

    @Test
    fun `Given a TextUiModel then the correct mapped language is returned`() {
        val data = LanguageSettingsState.Data(
            SystemDefaultLanguage,
            listOf(
                UserSelectedLanguage(AppLanguage.FRENCH),
                UserSelectedLanguage(AppLanguage.CATALAN),
                UserSelectedLanguage(AppLanguage.ENGLISH)
            )
        )

        assertEquals(
            UserSelectedLanguage(AppLanguage.CATALAN),
            data.languageFor(TextUiModel(AppLanguage.CATALAN.langName))
        )

        assertEquals(
            UserSelectedLanguage(AppLanguage.FRENCH),
            data.languageFor(TextUiModel(AppLanguage.FRENCH.langName))
        )

        assertEquals(
            UserSelectedLanguage(AppLanguage.ENGLISH),
            data.languageFor(TextUiModel(AppLanguage.ENGLISH.langName))
        )
    }
}
