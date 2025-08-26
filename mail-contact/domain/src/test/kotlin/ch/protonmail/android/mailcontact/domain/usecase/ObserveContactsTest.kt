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

package ch.protonmail.android.mailcontact.domain.usecase

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.right
import ch.protonmail.android.mailcontact.domain.model.ContactMetadata
import ch.protonmail.android.mailcontact.domain.repository.ContactRepository
import ch.protonmail.android.testdata.user.UserIdTestData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveContactsTest {

    private val contactRepository = mockk<ContactRepository> {
        coEvery { observeAllContacts(UserIdTestData.userId) } returns flowOf(emptyList<ContactMetadata>().right())
    }
    private val observeContacts = ObserveContacts(contactRepository)

    @Test
    fun ` returns empty list until Rust contact repository is implemented`() = runTest {
        // When
        observeContacts(UserIdTestData.userId).test {
            // Then
            val actual = assertIs<Either.Right<List<ContactMetadata.Contact>>>(awaitItem())
            assertEquals(emptyList(), actual.value)
            awaitComplete()
        }
    }

}
