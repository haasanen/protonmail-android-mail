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

package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.ExternalEncryptionPassword
import ch.protonmail.android.mailcomposer.domain.model.ExternalEncryptionPasswordError
import ch.protonmail.android.mailcomposer.domain.repository.MessagePasswordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveMessagePasswordTest {

    private val messagePasswordRepository = mockk<MessagePasswordRepository>()

    private val saveMessagePassword = SaveMessagePassword(
        messagePasswordRepository = messagePasswordRepository
    )

    @Test
    fun `should return unit when message password stored successfully`() = runTest {
        // Given
        val password = "password"
        val passwordHint = "password hint"
        coEvery {
            messagePasswordRepository.savePassword(
                ExternalEncryptionPassword(
                    password,
                    passwordHint
                )
            )
        } returns Unit.right()

        // When
        val actual = saveMessagePassword(password, passwordHint)

        // Then
        assertEquals(Unit.right(), actual)
    }

    @Test
    fun `should return error when saving password fails`() = runTest {
        // Given
        val password = "password"
        val passwordHint = "password hint"
        val expected = ExternalEncryptionPasswordError.Other(DataError.Local.Unknown)
        coEvery {
            messagePasswordRepository.savePassword(ExternalEncryptionPassword(password, passwordHint))
        } returns expected.left()

        // When
        val actual = saveMessagePassword(password, passwordHint)

        // Then
        assertEquals(expected.left(), actual)
    }

}
