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

package ch.protonmail.android.mailmailbox.domain.usecase

import arrow.core.left
import arrow.core.right
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.usecase.GetCurrentMailLabel
import ch.protonmail.android.maillabel.domain.usecase.ObserveSystemMailLabels
import ch.protonmail.android.testdata.maillabel.MailLabelTestData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ShouldShowRecipientsTest {

    private val userId = UserIdSample.Primary
    private val getCurrentMailLabel = mockk<GetCurrentMailLabel>()
    private val observeSystemMailLabels = mockk<ObserveSystemMailLabels> {
        coEvery { this@mockk.invoke(userId) } returns flowOf(
            MailLabelTestData.dynamicSystemLabels.right()
        )
    }
    private val shouldShowRecipients = ShouldShowRecipients(getCurrentMailLabel, observeSystemMailLabels)


    @Test
    fun `should return false when current label is custom`() = runTest {
        // Given
        val customMailLabel = MailLabelTestData.customLabelOne
        coEvery { getCurrentMailLabel(userId) } returns customMailLabel

        // When
        val result = shouldShowRecipients(userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return true when current label is sent and exists in system labels`() = runTest {
        // Given
        val sentMailLabel = MailLabelTestData.sentSystemLabel
        coEvery { getCurrentMailLabel(userId) } returns sentMailLabel

        // When
        val result = shouldShowRecipients(userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return false when current system label is not eligible`() = runTest {
        // Given
        val archiveMailLabel = MailLabelTestData.archiveSystemLabel
        coEvery { getCurrentMailLabel(userId) } returns archiveMailLabel

        // When
        val result = shouldShowRecipients(userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return false when observeCurrentMailLabel returns null`() = runTest {
        // Given
        coEvery { getCurrentMailLabel(userId) } returns null

        // When
        val result = shouldShowRecipients(userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return false when observeSystemMailLabels fails`() = runTest {
        // Given
        val sentMailLabel = MailLabelTestData.sentSystemLabel
        coEvery { getCurrentMailLabel(userId) } returns sentMailLabel
        coEvery { observeSystemMailLabels(userId) } returns flowOf(
            DataError.Local.CryptoError.left()
        )

        // When
        val result = shouldShowRecipients(userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should cache system labels result for same userId`() = runTest {
        // Given
        val sentMailLabel = MailLabelTestData.sentSystemLabel
        coEvery { getCurrentMailLabel(userId) } returns sentMailLabel

        // When
        val firstCallResult = shouldShowRecipients(userId)
        val secondCallResult = shouldShowRecipients(userId)

        // Then
        assertTrue(firstCallResult)
        assertTrue(secondCallResult)

        coVerify(exactly = 1) { observeSystemMailLabels(userId) }
    }
}
