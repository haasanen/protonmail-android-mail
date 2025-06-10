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

package protonmail.android.mailpinlock.presentation.autolock.mapper

import ch.protonmail.android.mailpinlock.model.AutoLockBiometricsState
import ch.protonmail.android.mailpinlock.presentation.autolock.mapper.AutoLockBiometricsUiModelMapper
import ch.protonmail.android.mailpinlock.presentation.autolock.model.AutoLockBiometricsUiModel
import org.junit.Test
import kotlin.test.assertEquals

class BiometricsUiModelMapperTest {

    val sut = AutoLockBiometricsUiModelMapper()

    @Test
    fun `map Biometrics Available AND Enrolled And Enabled`() {
        assertEquals(
            sut.toUiModel(AutoLockBiometricsState.BiometricsAvailable.BiometricsEnrolled),
            AutoLockBiometricsUiModel(
                enabled = true,
                biometricsEnrolled = true,
                biometricsHwAvailable = true
            )
        )
    }

    @Test
    fun `map Biometrics Available AND Enrolled And NOT Enabled`() {
        assertEquals(
            sut.toUiModel(AutoLockBiometricsState.BiometricsAvailable.BiometricsEnrolled),
            AutoLockBiometricsUiModel(
                enabled = true,
                biometricsEnrolled = true,
                biometricsHwAvailable = true
            )
        )
    }

    @Test
    fun `map Biometrics Available NOT Enrolled`() {
        assertEquals(
            sut.toUiModel(AutoLockBiometricsState.BiometricsAvailable.BiometricsNotEnrolled),
            AutoLockBiometricsUiModel(
                enabled = false,
                biometricsEnrolled = false,
                biometricsHwAvailable = true
            )
        )
    }

    @Test
    fun `map Biometrics NOT Available`() {
        assertEquals(
            sut.toUiModel(AutoLockBiometricsState.BiometricsNotAvailable),
            AutoLockBiometricsUiModel(
                enabled = false,
                biometricsEnrolled = false,
                biometricsHwAvailable = false
            )
        )
    }
}
