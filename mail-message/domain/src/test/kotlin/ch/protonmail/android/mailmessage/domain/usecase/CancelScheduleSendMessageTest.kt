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

package ch.protonmail.android.mailmessage.domain.usecase

import arrow.core.right
import ch.protonmail.android.mailcommon.domain.sample.UserIdSample
import ch.protonmail.android.mailmessage.domain.repository.PreviousScheduleSendTimeRepository
import ch.protonmail.android.mailmessage.domain.model.PreviousScheduleSendTime
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.sample.MessageIdSample
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class CancelScheduleSendMessageTest {

    private val messageRepository = mockk<MessageRepository>()
    private val previousScheduleTimeRepository = mockk<PreviousScheduleSendTimeRepository>(relaxUnitFun = true)

    private val cancelScheduleSendMessage = CancelScheduleSendMessage(
        messageRepository,
        previousScheduleTimeRepository
    )

    @Test
    fun `successful cancel schedule send caches previous send time in memory`() = runTest {
        // Given
        val userId = UserIdSample.Primary
        val messageId = MessageIdSample.PlainTextMessage
        val previousScheduleTime = PreviousScheduleSendTime(Instant.DISTANT_FUTURE)
        coEvery { messageRepository.cancelScheduleSend(userId, messageId) } returns previousScheduleTime.right()

        // When
        cancelScheduleSendMessage(userId, messageId)

        // Then
        coVerify { previousScheduleTimeRepository.save(previousScheduleTime) }
    }
}
