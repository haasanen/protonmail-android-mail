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

package ch.protonmail.android.mailsettings.presentation.settings.theme

import androidx.compose.runtime.Immutable
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailsettings.domain.model.Theme
import ch.protonmail.android.mailsettings.domain.model.Theme.DARK
import ch.protonmail.android.mailsettings.domain.model.Theme.LIGHT
import ch.protonmail.android.mailsettings.domain.model.Theme.SYSTEM_DEFAULT
import ch.protonmail.android.mailsettings.presentation.R

@Immutable
sealed class ThemeSettingsState {

    data class Data(
        private val currentTheme: Theme,
        val themesToChoices: Map<Theme, TextUiModel>
    ) : ThemeSettingsState() {
        val themeChoices = themesToChoices.values.toList()
        val selectedTheme = themesToChoices.getValue(currentTheme)
    }

    data object Loading : ThemeSettingsState()
}

data class ThemeSettingsEffects(
    val close: Effect<Unit> = Effect.empty()
)

internal fun ThemeSettingsEffects.onCloseEffect() = this.copy(close = Effect.of(Unit))

internal fun Theme.toUiModel() = TextUiModel(nameStringResourceBy(this))

internal fun ThemeSettingsState.Data.themeFor(choice: TextUiModel) =
    themesToChoices.entries.first { it.value == choice }.key

private fun nameStringResourceBy(theme: Theme) = when (theme) {
    SYSTEM_DEFAULT -> R.string.mail_settings_system_default
    LIGHT -> R.string.mail_settings_theme_light
    DARK -> R.string.mail_settings_theme_dark
}
