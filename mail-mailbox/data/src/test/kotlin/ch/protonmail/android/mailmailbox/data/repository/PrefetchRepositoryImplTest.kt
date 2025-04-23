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

package ch.protonmail.android.mailmailbox.data.repository

import ch.protonmail.android.mailmailbox.data.local.RustPrefetchDataSource
import ch.protonmail.android.mailsession.domain.wrapper.MailUserSessionWrapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import uniffi.proton_mail_uniffi.MailUserSession
import kotlin.test.Test

internal class PrefetchRepositoryImplTest {

    private val dataSource = mockk<RustPrefetchDataSource>()
    private val repository = PrefetchRepositoryImpl(dataSource)

    @Test
    fun `should call the data source with the appropriate user session wrapping`() = runTest {
        // Given
        val wrappedUserSession = mockk<MailUserSessionWrapper>()
        val userSession = mockk<MailUserSession>()

        every { wrappedUserSession.getRustUserSession() } returns userSession
        coEvery { dataSource.prefetchData(userSession) } just runs

        // When
        repository.prefetch(wrappedUserSession)

        // Then
        coVerify(exactly = 1) { dataSource.prefetchData(userSession) }
        confirmVerified(dataSource)
    }
}
