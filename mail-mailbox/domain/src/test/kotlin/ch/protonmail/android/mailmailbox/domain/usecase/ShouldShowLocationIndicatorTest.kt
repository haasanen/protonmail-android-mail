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

import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.maillabel.domain.model.ExclusiveLocation
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.model.LabelWithSystemLabelId
import ch.protonmail.android.maillabel.domain.model.MailLabelId
import ch.protonmail.android.maillabel.domain.model.SystemLabelId
import ch.protonmail.android.maillabel.domain.repository.LabelRepository
import ch.protonmail.android.testdata.label.LabelTestData.buildLabel
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShouldShowLocationIndicatorTest {

    private val userId = UserIdSample.Primary

    private val labelRepository: LabelRepository = mockk()

    private val shouldShowLocationIndicator = ShouldShowLocationIndicator(labelRepository)

    private val systemLabels = listOf(
        LabelWithSystemLabelId(
            buildLabel(id = SystemLabelId.Starred.labelId.id, type = LabelType.SystemFolder),
            SystemLabelId.Starred
        ),
        LabelWithSystemLabelId(
            buildLabel(id = SystemLabelId.AllMail.labelId.id, type = LabelType.SystemFolder),
            SystemLabelId.AllMail
        ),
        LabelWithSystemLabelId(
            buildLabel(id = SystemLabelId.AlmostAllMail.labelId.id, type = LabelType.SystemFolder),
            SystemLabelId.AlmostAllMail
        ),
        LabelWithSystemLabelId(
            buildLabel(id = SystemLabelId.Sent.labelId.id, type = LabelType.SystemFolder),
            SystemLabelId.Sent
        ),
        LabelWithSystemLabelId(
            buildLabel(id = SystemLabelId.AllSent.labelId.id, type = LabelType.SystemFolder),
            SystemLabelId.AllSent
        )
    )

    @Test
    fun `should return true when current location is a custom label`() = runTest {
        // Given
        val currentLocation = MailLabelId.Custom.Label(LabelId("0"))
        val itemLocation = ExclusiveLocation.System(SystemLabelId.Inbox, LabelId("1"))

        // When
        val result = shouldShowLocationIndicator.invoke(userId, currentLocation, itemLocation)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return false when current system location is not in exclusive system labels`() = runTest {
        // Given
        val currentLocation = MailLabelId.System(SystemLabelId.Inbox.labelId)
        val itemLocation = ExclusiveLocation.System(SystemLabelId.Inbox, LabelId("1"))
        coEvery { labelRepository.observeSystemLabels(any()) } returns flowOf(emptyList())

        // When
        val result = shouldShowLocationIndicator.invoke(userId, currentLocation, itemLocation)

        // Then
        assertFalse(result)
        verify { labelRepository.observeSystemLabels(userId) }
    }

    @Test
    fun `should return true when current system location is starred`() = runTest {
        // Given
        val currentLocation = MailLabelId.System(SystemLabelId.Starred.labelId)
        val itemLocation = ExclusiveLocation.System(SystemLabelId.Inbox, LabelId("1"))
        coEvery { labelRepository.observeSystemLabels(any()) } returns flowOf(systemLabels)

        // When
        val result = shouldShowLocationIndicator.invoke(userId, currentLocation, itemLocation)

        // Then
        assertTrue(result)
        verify { labelRepository.observeSystemLabels(userId) }
    }

    @Test
    fun `should return true when current system location is AllMail`() = runTest {
        // Given
        val currentLocation = MailLabelId.System(SystemLabelId.AllMail.labelId)
        val itemLocation = ExclusiveLocation.System(SystemLabelId.Inbox, LabelId("1"))
        coEvery { labelRepository.observeSystemLabels(any()) } returns flowOf(systemLabels)

        // When
        val result = shouldShowLocationIndicator.invoke(userId, currentLocation, itemLocation)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return true when current system location is AlmostAllMail`() = runTest {
        // Given
        val currentLocation = MailLabelId.System(SystemLabelId.AlmostAllMail.labelId)
        val itemLocation = ExclusiveLocation.System(SystemLabelId.Inbox, LabelId("1"))
        coEvery { labelRepository.observeSystemLabels(any()) } returns flowOf(systemLabels)

        // When
        val result = shouldShowLocationIndicator.invoke(userId, currentLocation, itemLocation)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should cache system labels in memory by user`() = runTest {
        // Given
        val firstLocation = MailLabelId.System(SystemLabelId.Archive.labelId)
        val secondLocation = MailLabelId.System(SystemLabelId.Starred.labelId)
        val itemLocation = ExclusiveLocation.System(SystemLabelId.Inbox, LabelId("1"))
        coEvery { labelRepository.observeSystemLabels(any()) } returns flowOf(systemLabels)

        // When
        val firstResult = shouldShowLocationIndicator.invoke(userId, firstLocation, itemLocation)
        val secondResult = shouldShowLocationIndicator.invoke(userId, secondLocation, itemLocation)

        // Then
        verify(exactly = 1) { labelRepository.observeSystemLabels(any()) }
        assertFalse(firstResult)
        assertTrue(secondResult)
    }

    @Test
    fun `should return true when system location is Sent and mailboxItem is scheduled for sending`() = runTest {
        // Given
        val currentLocation = MailLabelId.System(SystemLabelId.Sent.labelId)
        val itemLocation = ExclusiveLocation.System(SystemLabelId.AllScheduled, LabelId("12"))
        coEvery { labelRepository.observeSystemLabels(any()) } returns flowOf(systemLabels)

        // When
        val result = shouldShowLocationIndicator.invoke(userId, currentLocation, itemLocation)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return true when system location is All Sent and mailboxItem is scheduled for sending`() = runTest {
        // Given
        val currentLocation = MailLabelId.System(SystemLabelId.AllSent.labelId)
        val itemLocation = ExclusiveLocation.System(SystemLabelId.AllScheduled, LabelId("12"))
        coEvery { labelRepository.observeSystemLabels(any()) } returns flowOf(systemLabels)

        // When
        val result = shouldShowLocationIndicator.invoke(userId, currentLocation, itemLocation)

        // Then
        assertTrue(result)
    }

    @Test
    fun `should return false when current system location is Sent and mailboxItem is not scheduled`() = runTest {
        // Given
        val currentLocation = MailLabelId.System(SystemLabelId.Sent.labelId)
        val itemLocation = ExclusiveLocation.System(SystemLabelId.Sent, LabelId("7"))
        coEvery { labelRepository.observeSystemLabels(any()) } returns flowOf(systemLabels)

        // When
        val result = shouldShowLocationIndicator.invoke(userId, currentLocation, itemLocation)

        // Then
        assertFalse(result)
    }

}
