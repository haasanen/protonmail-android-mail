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

package ch.protonmail.android.maildetail.presentation.usecase

import ch.protonmail.android.mailmessage.domain.model.MessageBodyImage
import ch.protonmail.android.mailmessage.domain.usecase.LoadMessageBodyImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class LoadImageAvoidDuplicatedExecution @Inject constructor(
    private val loadMessageBodyImage: LoadMessageBodyImage
) {

    private val loadMessageBodyImageJobMap = mutableMapOf<String, Deferred<MessageBodyImage?>>()

    suspend operator fun invoke(
        userId: UserId,
        messageId: MessageId,
        url: String,
        coroutineContext: CoroutineContext
    ): MessageBodyImage? = runCatching {
        withContext(coroutineContext) {
            if (loadMessageBodyImageJobMap[url]?.isActive == true) {
                loadMessageBodyImageJobMap[url]
            } else {
                async { loadMessageBodyImage(userId, messageId, url).getOrNull() }.apply {
                    loadMessageBodyImageJobMap[url] = this
                }
            }?.await()
        }
    }.getOrNull()
}
