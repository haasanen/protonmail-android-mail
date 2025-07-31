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

package ch.protonmail.android.mailmailbox.domain.mapper

import ch.protonmail.android.mailattachments.domain.model.AttachmentCount
import ch.protonmail.android.mailattachments.domain.sample.AttachmentMetadataSamples
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.domain.sample.LabelSample
import ch.protonmail.android.testdata.label.LabelTestData
import ch.protonmail.android.testdata.message.MessageTestData
import ch.protonmail.android.testdata.user.UserIdTestData.userId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MessageMailboxItemMapperTest {

    private val mapper = MessageMailboxItemMapper()

    @Test
    fun `when mapping message to mailbox item num messages is always 1`() {
        // Given
        val message = MessageTestData.buildMessage(userId, "id")
        // When
        val actual = mapper.toMailboxItem(message)
        // Then
        assertEquals(1, actual.numMessages)
    }

    @Test
    fun `when mapping message to mailbox item all custom labels are preserved`() {
        // Given
        val expectedLabels = listOf(LabelSample.Label2021, LabelSample.Label2022)
        val message = MessageTestData.buildMessage(userId, "id", customLabels = expectedLabels)
        // When
        val actual = mapper.toMailboxItem(message)
        // Then
        assertEquals(expectedLabels, actual.labels)
    }

    @Test
    fun `when mapping message to mailbox item labels are sorted according to their order`() {
        // Given
        val customLabels = listOf("5", "0", "10").map(::buildLabel)
        val message = MessageTestData.buildMessage(userId, "id", customLabels = customLabels)
        // When
        val actual = mapper.toMailboxItem(message = message)
        // Then
        val expected = listOf("0", "5", "10").map(::buildLabel)
        assertEquals(expected, actual.labels)
    }

    @Test
    fun `when mapping message with 1 or more non-calendar attachments to mailbox item then has attachments is true`() {
        // Given
        val message = MessageTestData.buildMessage(
            userId,
            "id",
            numAttachments = 1,
            attachmentCount = AttachmentCount(calendar = 0)
        )
        // When
        val actual = mapper.toMailboxItem(message)
        // Then
        assertTrue(actual.hasNonCalendarAttachments)
    }

    @Test
    fun `when mapping message with 0 attachments to mailbox item then has attachments is false`() {
        // Given
        val message = MessageTestData.buildMessage(userId, "id", numAttachments = 0)
        // When
        val actual = mapper.toMailboxItem(message)
        // Then
        assertFalse(actual.hasNonCalendarAttachments)
    }

    @Test
    fun `when mapping message with only calendar attachments to mailbox item then has attachments is false`() {
        // Given
        val message = MessageTestData.buildMessage(
            userId,
            "id",
            numAttachments = 1,
            attachmentCount = AttachmentCount(calendar = 1)
        )
        // When
        val actual = mapper.toMailboxItem(message)
        // Then
        assertFalse(actual.hasNonCalendarAttachments)
    }

    @Test
    fun `when mapping message expiration time is preserved in the mailbox item`() {
        // Given
        val expirationTime = 1000L
        val message = MessageTestData.buildMessage(userId, "id", expirationTime = expirationTime)
        // When
        val mailboxItem = mapper.toMailboxItem(message)
        // Then
        assertEquals(expirationTime, mailboxItem.expirationTime)
    }

    @Test
    fun `when mapping message, calendar attachment count is preserved in the mailbox item`() {
        // Given
        val calendarAttachmentCount = 1
        val message = MessageTestData.buildMessage(
            userId,
            "id",
            attachmentCount = AttachmentCount(calendar = calendarAttachmentCount)
        )
        // When
        val mailboxItem = mapper.toMailboxItem(message)
        // Then
        assertEquals(calendarAttachmentCount, mailboxItem.calendarAttachmentCount)
    }

    @Test
    fun `when mapping message, attachments are preserved in the mailbox item`() {
        // Given
        val attachments = listOf(
            AttachmentMetadataSamples.Pdf,
            AttachmentMetadataSamples.Image
        )
        val message = MessageTestData.buildMessage(
            userId,
            "id",
            attachments = attachments
        )
        // When
        val mailboxItem = mapper.toMailboxItem(message)
        // Then
        assertEquals(attachments, mailboxItem.attachments)
    }


    @Test
    fun `when mapping message to displaySnoozeReminder is always false`() {
        // Given
        val message = MessageTestData.buildMessage(userId, "id").copy()
        // When
        val actual = mapper.toMailboxItem(message)
        // Then
        assertFalse(actual.displaySnoozeReminder)
    }

    private fun buildLabel(value: String) = LabelTestData.buildLabel(
        id = value,
        type = LabelType.MessageLabel,
        order = value.hashCode()
    )
}
