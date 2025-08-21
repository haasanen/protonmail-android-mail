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

package ch.protonmail.android.mailsettings.data.repository

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailsettings.data.local.MobileSignatureDataSource
import ch.protonmail.android.mailsettings.domain.model.MobileSignaturePreference
import ch.protonmail.android.mailsettings.domain.model.MobileSignatureStatus
import ch.protonmail.android.mailsettings.domain.repository.MobileSignatureRepository
import ch.protonmail.android.test.utils.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class MobileSignatureRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dataSource = mockk<MobileSignatureDataSource>()
    private val repository: MobileSignatureRepository = MobileSignatureRepositoryImpl(dataSource)

    private val userId = UserId("user-123")

    @Test
    fun `observeMobileSignature emits current preference from data source`() = runTest {
        // Given
        val pref = MobileSignaturePreference(
            value = "My Signature",
            status =
            MobileSignatureStatus.Enabled
        )
        coEvery { dataSource.getMobileSignature(userId) } returns pref.right()

        // When
        repository.observeMobileSignature(userId).test {

            // Then
            assertEquals(pref, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { dataSource.getMobileSignature(userId) }
    }

    @Test
    fun `observeMobileSignature defaults to Empty when data source fails`() = runTest {
        // Given
        coEvery { dataSource.getMobileSignature(userId) } returns DataError.Local.Unknown.left()

        // When / Then
        repository.observeMobileSignature(userId).test {
            assertEquals(MobileSignaturePreference.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 1) { dataSource.getMobileSignature(userId) }
    }

    @Test
    fun `setMobileSignature success triggers second emission with updated value`() = runTest {
        // Given
        val oldSignature = "old signature"
        val newSignature = "new signature"
        val initial = MobileSignaturePreference(
            value = oldSignature,
            status =
            MobileSignatureStatus.Enabled
        )
        val updated = MobileSignaturePreference(
            value = newSignature,
            status =
            MobileSignatureStatus.Enabled
        )

        coEvery { dataSource.getMobileSignature(userId) } returns initial.right() andThen updated.right()
        coEvery { dataSource.setMobileSignature(userId, newSignature) } returns Unit.right()

        // When
        repository.observeMobileSignature(userId).test {
            // Then
            assertEquals(initial, awaitItem())

            // When
            repository.setMobileSignature(userId, newSignature)

            // Then
            assertEquals(updated, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { dataSource.setMobileSignature(userId, newSignature) }
        coVerify(exactly = 2) { dataSource.getMobileSignature(userId) }
    }

    @Test
    fun `setMobileSignatureEnabled success triggers second emission with updated value`() = runTest {
        // Given
        val signature = "signature"
        val initial = MobileSignaturePreference(
            value = signature,
            status =
            MobileSignatureStatus.Disabled
        )
        val updated = MobileSignaturePreference(
            value = signature,
            status =
            MobileSignatureStatus.Enabled
        )

        coEvery { dataSource.getMobileSignature(userId) } returns initial.right() andThen updated.right()
        coEvery { dataSource.setMobileSignatureEnabled(userId, true) } returns Unit.right()

        // When / Then
        repository.observeMobileSignature(userId).test {
            assertEquals(initial, awaitItem())

            repository.setMobileSignatureEnabled(userId, true)

            assertEquals(updated, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { dataSource.setMobileSignatureEnabled(userId, true) }
        coVerify(exactly = 2) { dataSource.getMobileSignature(userId) }
    }

    @Test
    fun `getMobileSignature returns the signature when data source is successful`() = runTest {
        // Given
        val pref = MobileSignaturePreference(
            value = "proxy",
            status =
            MobileSignatureStatus.Enabled
        )
        coEvery { dataSource.getMobileSignature(userId) } returns pref.right()

        // When
        val result = repository.getMobileSignature(userId)

        // Then
        assertEquals(Either.Right(pref), result)
        coVerify(exactly = 1) { dataSource.getMobileSignature(userId) }
    }

    @Test
    fun `getMobileSignature returns error when data source fails`() = runTest {
        // Given
        val err = DataError.Local.NoUserSession
        coEvery { dataSource.getMobileSignature(userId) } returns err.left()

        // When
        val result = repository.getMobileSignature(userId)

        // Then
        assertEquals(Either.Left(err), result)
        coVerify(exactly = 1) { dataSource.getMobileSignature(userId) }
    }
}
