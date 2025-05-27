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

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.AppLanguage
import ch.protonmail.android.mailsettings.presentation.R

sealed interface LanguageChoice {

    val textUiModel: TextUiModel
}

data class UserSelectedLanguage(val appLanguage: AppLanguage) : LanguageChoice {

    override val textUiModel = TextUiModel(appLanguage.langName)
}

data object SystemDefaultLanguage : LanguageChoice {

    override val textUiModel = TextUiModel(R.string.mail_settings_app_language)
}

sealed class LanguageSettingsState {

    data class Data(
        private val currentLanguage: LanguageChoice,
        val languagesChoices: List<LanguageChoice>
    ) : LanguageSettingsState() {

        val languageChoiceUiTextModels = languagesChoices.map { it.textUiModel }
        val selectedLanguageUiModel = currentLanguage.textUiModel
    }

    data object Loading : LanguageSettingsState()
}

data class LanguageSettingsEffects(
    val close: Effect<Unit> = Effect.empty()
)

internal fun LanguageSettingsEffects.onCloseEffect() = this.copy(close = Effect.of(Unit))

internal fun AppLanguage?.toUiModel() = this?.let { UserSelectedLanguage(it) } ?: SystemDefaultLanguage

internal fun LanguageSettingsState.Data.languageFor(choice: TextUiModel) =
    languagesChoices.first { it.textUiModel == choice }

