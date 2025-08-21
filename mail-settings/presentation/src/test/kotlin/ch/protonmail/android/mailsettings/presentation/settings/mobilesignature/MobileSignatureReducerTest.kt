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

package ch.protonmail.android.mailsettings.presentation.settings.mobilesignature

import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureEvent
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureSettingsUiModel
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureState
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.model.MobileSignatureViewAction
import ch.protonmail.android.mailsettings.presentation.settings.mobilesignature.reducer.MobileSignatureReducer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class MobileSignatureReducerTest {

    private val reducer = MobileSignatureReducer()

    private val signatureSettings = MobileSignatureSettingsUiModel(
        enabled = true,
        signatureValue = "Test Signature",
        editSignatureEffect = Effect.empty()
    )

    @Test
    fun `SignatureLoaded transitions to Data with provided settings when current is Loading`() {
        // Given
        val current: MobileSignatureState = MobileSignatureState.Loading

        // When
        val next = reducer.newStateFrom(
            current,
            MobileSignatureEvent.SignatureLoaded(signatureSettings)
        )

        // Then
        assertEquals(MobileSignatureState.Data(signatureSettings), next)
    }

    @Test
    fun `SignatureLoaded replaces existing Data state`() {
        // Given
        val current: MobileSignatureState = MobileSignatureState.Data(signatureSettings)
        val updatedSettings = signatureSettings.copy(
            signatureValue = "Updated Signature"
        )

        // When
        val newState = reducer.newStateFrom(
            current,
            MobileSignatureEvent.SignatureLoaded(updatedSettings)
        )

        // Then
        assertEquals(MobileSignatureState.Data(updatedSettings), newState)
    }

    @Test
    fun `EditSignatureValue sets editSignatureEffect when current state is Data`() {
        // Given
        val initial = signatureSettings
        val current: MobileSignatureState = MobileSignatureState.Data(initial)

        // When
        val next = reducer.newStateFrom(current, MobileSignatureViewAction.EditSignatureValue)

        // Then
        val expected = MobileSignatureState.Data(
            initial.copy(editSignatureEffect = Effect.of(Unit))
        )
        assertEquals(expected, next)
    }

    @Test
    fun `EditSignatureValue action is no-op when current is Loading`() {
        // Given
        val current: MobileSignatureState = MobileSignatureState.Loading

        // When
        val next = reducer.newStateFrom(current, MobileSignatureViewAction.EditSignatureValue)

        // Then
        assertSame(current, next)
    }

}
