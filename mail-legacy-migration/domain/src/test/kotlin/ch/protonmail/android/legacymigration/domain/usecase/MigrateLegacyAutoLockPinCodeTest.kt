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

package ch.protonmail.android.legacymigration.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.legacymigration.domain.model.LegacyAutoLockPin
import ch.protonmail.android.legacymigration.domain.model.MigrationError
import ch.protonmail.android.legacymigration.domain.model.toAutoLockPin
import io.mockk.coEvery
import io.mockk.every
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import ch.protonmail.android.legacymigration.domain.repository.LegacyAutoLockRepository
import ch.protonmail.android.mailcommon.domain.model.autolock.SetAutoLockPinError
import ch.protonmail.android.mailsession.domain.repository.UserSessionRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf

class MigrateLegacyAutoLockPinCodeTest {

    private val legacyAutoLockRepository = mockk<LegacyAutoLockRepository>()
    private val userSessionRepository = mockk<UserSessionRepository>()

    private val migrateLegacyAutoLockPinCode = MigrateLegacyAutoLockPinCode(
        legacyAutoLockRepository = legacyAutoLockRepository,
        userSessionRepository = userSessionRepository
    )

    private val dummyLegacyPin = LegacyAutoLockPin("1234")


    @Test
    fun `Migrates auto-lock pin successfully when legacy pin exists`() = runTest {
        coEvery { legacyAutoLockRepository.hasAutoLockPinCode() } returns true
        every { legacyAutoLockRepository.observeAutoLockPinCode() } returns flowOf(dummyLegacyPin.right())
        coEvery { userSessionRepository.setAutoLockPinCode(any()) } returns Unit.right()

        val result = migrateLegacyAutoLockPinCode()

        assertEquals(Unit.right(), result)
        coVerify { userSessionRepository.setAutoLockPinCode(dummyLegacyPin.toAutoLockPin()) }
    }

    @Test
    fun `Skips migration when no legacy auto-lock pin is found`() = runTest {
        coEvery { legacyAutoLockRepository.hasAutoLockPinCode() } returns false

        val result = migrateLegacyAutoLockPinCode()

        assertEquals(Unit.right(), result)
        coVerify(exactly = 0) { legacyAutoLockRepository.observeAutoLockPinCode() }
    }

    @Test
    fun `Fails migration when auto-lock pin cannot be retrieved from legacy repository`() = runTest {
        coEvery { legacyAutoLockRepository.hasAutoLockPinCode() } returns true
        every {
            legacyAutoLockRepository.observeAutoLockPinCode()
        } returns flowOf(MigrationError.AutoLockFailure.FailedToReadAutoLockPin.left())

        val result = migrateLegacyAutoLockPinCode()

        assertEquals(MigrationError.AutoLockFailure.FailedToReadAutoLockPin.left(), result)
    }

    @Test
    fun `Fails migration when setting pin fails`() = runTest {
        coEvery { legacyAutoLockRepository.hasAutoLockPinCode() } returns true
        every { legacyAutoLockRepository.observeAutoLockPinCode() } returns flowOf(dummyLegacyPin.right())
        coEvery { userSessionRepository.setAutoLockPinCode(any()) } returns SetAutoLockPinError.PinIsMalformed.left()

        val result = migrateLegacyAutoLockPinCode()

        assertEquals(MigrationError.AutoLockFailure.FailedToSetAutoLockPin.left(), result)
    }
}
