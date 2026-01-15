/*
 * Copyright (c) 2026 Proton Technologies AG
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

package ch.protonmail.android.mailpadlocks.presentation.mapper

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import ch.protonmail.android.mailpadlocks.domain.PrivacyLock
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockColor
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockIcon
import ch.protonmail.android.mailpadlocks.domain.PrivacyLockTooltip
import ch.protonmail.android.mailpadlocks.presentation.R
import ch.protonmail.android.mailpadlocks.presentation.model.EncryptionInfoUiModel
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class EncryptionInfoUiModelMapperTest {

    @Test
    fun `returns NoLock when PrivacyLock is None`() {
        // Given
        val privacyLock = PrivacyLock.None

        // When
        val result = EncryptionInfoUiModelMapper.fromPrivacyLock(privacyLock)

        // Then
        assertTrue(result is EncryptionInfoUiModel.NoLock)
    }
}

@RunWith(Parameterized::class)
internal class EncryptionInfoUiModelMapperIconTest(
    @Suppress("unused") private val testName: String,
    private val domainIcon: PrivacyLockIcon,
    @DrawableRes private val expectedDrawableRes: Int
) {

    @Test
    fun `maps icon correctly`() {
        // Given
        val privacyLock = PrivacyLock.Value(
            icon = domainIcon,
            color = PrivacyLockColor.Black,
            tooltip = PrivacyLockTooltip.ZeroAccess
        )

        // When
        val result = EncryptionInfoUiModelMapper.fromPrivacyLock(privacyLock)

        // Then
        assertTrue(result is EncryptionInfoUiModel.WithLock)
        assertEquals(expectedDrawableRes, result.icon)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "ClosedLock",
                PrivacyLockIcon.ClosedLock,
                R.drawable.ic_lock_filled
            ),
            arrayOf(
                "ClosedLockWithTick",
                PrivacyLockIcon.ClosedLockWithTick,
                R.drawable.ic_lock_check_filled
            ),
            arrayOf(
                "ClosedLockWithPen",
                PrivacyLockIcon.ClosedLockWithPen,
                R.drawable.ic_lock_pen_filled
            ),
            arrayOf(
                "ClosedLockWarning",
                PrivacyLockIcon.ClosedLockWarning,
                R.drawable.ic_lock_exclamation_filled
            ),
            arrayOf(
                "OpenLockWithPen",
                PrivacyLockIcon.OpenLockWithPen,
                R.drawable.ic_lock_open_pen_filled
            ),
            arrayOf(
                "OpenLockWithTick",
                PrivacyLockIcon.OpenLockWithTick,
                R.drawable.ic_lock_open_check_filled
            ),
            arrayOf(
                "OpenLockWarning",
                PrivacyLockIcon.OpenLockWarning,
                R.drawable.ic_lock_open_exclamation_filled
            )
        )
    }
}

@RunWith(Parameterized::class)
internal class EncryptionInfoUiModelMapperColorTest(
    @Suppress("unused") private val testName: String,
    private val domainColor: PrivacyLockColor,
    @ColorRes private val expectedColorRes: Int
) {

    @Test
    fun `maps color correctly`() {
        // Given
        val privacyLock = PrivacyLock.Value(
            icon = PrivacyLockIcon.ClosedLock,
            color = domainColor,
            tooltip = PrivacyLockTooltip.ZeroAccess
        )

        // When
        val result = EncryptionInfoUiModelMapper.fromPrivacyLock(privacyLock)

        // Then
        assertTrue(result is EncryptionInfoUiModel.WithLock)
        assertEquals(expectedColorRes, result.color)
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(
                "Black",
                PrivacyLockColor.Black,
                R.color.padlock_black
            ),
            arrayOf(
                "Green",
                PrivacyLockColor.Green,
                R.color.padlock_green
            ),
            arrayOf(
                "Blue",
                PrivacyLockColor.Blue,
                R.color.padlock_blue
            )
        )
    }
}
