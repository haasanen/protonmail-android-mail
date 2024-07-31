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

package ch.protonmail.android.maillabel.domain.usecase

import app.cash.turbine.test
import ch.protonmail.android.mailcommon.domain.sample.UserSample
import ch.protonmail.android.mailcommon.domain.usecase.ObservePrimaryUser
import ch.protonmail.android.maillabel.domain.SelectedMailLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.user.domain.entity.User
import org.junit.Test
import kotlin.test.assertEquals

class SelectedMailLabelIdTest {

    private val appScope = TestScope()
    private val observePrimaryUser: ObservePrimaryUser = mockk {
        every { this@mockk() } returns emptyFlow()
    }

    // Hardcoded to "0" (Inbox label remote ID) as arbitrary value while removing all usages of system.
    // to be updated to dynamically pick inbox from the dynamic system labels
    private val initialSystemLabel = MailLabelId.DynamicSystemLabelId(SystemLabelId.Inbox.labelId)

    private val selectedMailLabelId by lazy {
        SelectedMailLabelId(
            appScope = appScope,
            observePrimaryUser = observePrimaryUser
        )
    }

    @Test
    fun `initial selected mailLabelId is inbox by default`() = runTest {
        selectedMailLabelId.flow.test {
            assertEquals(initialSystemLabel, awaitItem())
        }
    }

    @Test
    fun `emits newly selected mailLabelId when it changes`() = runTest {
        // Given
        val draftsSystemLabel = MailLabelTestData.draftsSystemLabel.id

        // When
        selectedMailLabelId.flow.test {
            assertEquals(initialSystemLabel, awaitItem())

            selectedMailLabelId.set(draftsSystemLabel)

            assertEquals(draftsSystemLabel, awaitItem())
        }
    }

    @Test
    fun `does not emit same mailLabelId twice`() = runTest {
        // Given
        val archiveSystemLabel = MailLabelTestData.archiveSystemLabel.id

        // When
        selectedMailLabelId.flow.test {
            // Then
            assertEquals(initialSystemLabel, awaitItem())

            selectedMailLabelId.set(archiveSystemLabel)
            selectedMailLabelId.set(archiveSystemLabel)
            selectedMailLabelId.set(MailLabelId.Custom.Label(LabelId("lId1")))

            assertEquals(archiveSystemLabel, awaitItem())
            assertEquals(MailLabelId.Custom.Label(LabelId("lId1")), awaitItem())
        }
    }

    @Test
    fun `emits inbox when primary user changes`() = runTest {
        // given
        val userFlow = MutableStateFlow<User?>(null)
        val archiveSystemLabel = MailLabelTestData.archiveSystemLabel.id
        every { observePrimaryUser() } returns userFlow

        selectedMailLabelId.flow.test {
            assertEquals(initialSystemLabel, awaitItem())
            selectedMailLabelId.set(archiveSystemLabel)
            assertEquals(archiveSystemLabel, awaitItem())

            // when
            userFlow.emit(UserSample.Primary)
            appScope.advanceUntilIdle()

            // then
            assertEquals(initialSystemLabel, awaitItem())
        }
    }
}
