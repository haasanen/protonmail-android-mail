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

package me.proton.android.core.devicemigration.presentation.origin.success

import android.content.Context
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.yield
import me.proton.android.core.account.domain.model.CoreAccount
import me.proton.android.core.account.domain.usecase.ObservePrimaryCoreAccount
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OriginSuccessViewModelTest : CoroutinesTest by CoroutinesTest() {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var observePrimaryCoreAccount: ObservePrimaryCoreAccount

    private lateinit var tested: OriginSuccessViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = OriginSuccessViewModel(context, observePrimaryCoreAccount)
    }

    @Test
    fun `loading user email`() = coroutinesTest {
        // GIVEN
        every { observePrimaryCoreAccount() } returns flowOf(
            mockk<CoreAccount> {
                every { primaryEmailAddress } returns "email"
            }
        )

        tested.state.test {
            // THEN
            assertEquals(OriginSuccessState.Loading, awaitItem())
            assertEquals(OriginSuccessState.Idle("email"), awaitItem())
        }
    }

    @Test
    fun `failure while loading`() = coroutinesTest {
        // GIVEN
        val errMsg = "Cannot load user"
        every { observePrimaryCoreAccount() } returns flow {
            yield()
            error(errMsg)
        }

        tested.state.test {
            // THEN
            assertEquals(OriginSuccessState.Loading, awaitItem())
            assertEquals(OriginSuccessState.Error.Unknown(errMsg), awaitItem())
        }
    }
}
