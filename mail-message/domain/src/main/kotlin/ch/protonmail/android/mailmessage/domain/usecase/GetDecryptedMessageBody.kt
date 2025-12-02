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

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.right
import ch.protonmail.android.mailfeatureflags.domain.annotation.IsInjectCssOverrideEnabled
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import ch.protonmail.android.mailmessage.domain.model.DecryptedMessageBody
import ch.protonmail.android.mailmessage.domain.model.GetMessageBodyError
import ch.protonmail.android.mailmessage.domain.model.MessageBodyTransformations
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.domain.repository.MessageBodyRepository
import ch.protonmail.android.mailmessage.domain.repository.MessageRepository
import ch.protonmail.android.mailmessage.domain.repository.RsvpEventRepository
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class GetDecryptedMessageBody @Inject constructor(
    private val messageRepository: MessageRepository,
    private val messageBodyRepository: MessageBodyRepository,
    private val rsvpEventRepository: RsvpEventRepository,
    private val injectViewPortMetaTagIntoMessageBody: InjectViewPortMetaTagIntoMessageBody,
    private val injectFixedHeightCss: InjectFixedHeightCss,
    @IsInjectCssOverrideEnabled private val isInjectCssOverrideEnabled: FeatureFlag<Boolean>
) {

    suspend operator fun invoke(
        userId: UserId,
        messageId: MessageId,
        transformations: MessageBodyTransformations = MessageBodyTransformations.MessageDetailsDefaults
    ): Either<GetMessageBodyError, DecryptedMessageBody> = messageRepository.getMessage(userId, messageId)
        .mapLeft { GetMessageBodyError.Data(it) }
        .flatMap { messageMetadata ->
            messageBodyRepository.getMessageBody(userId, messageId, transformations)
                .mapLeft { GetMessageBodyError.Data(it) }
                .flatMap { messageBody ->
                    // Handle zoomed in newsletters
                    val transformedMessageBody = injectViewPortMetaTagIntoMessageBody(messageBody.body).let {
                        // Handle HTML content that styles with 'height: 100%'
                        if (isInjectCssOverrideEnabled.get()) injectFixedHeightCss(it) else it
                    }

                    val hasCalendarInvite = rsvpEventRepository.identifyRsvp(userId, messageId).getOrElse { false }

                    DecryptedMessageBody(
                        messageId = messageId,
                        value = transformedMessageBody,
                        isUnread = messageMetadata.isUnread,
                        mimeType = messageBody.mimeType,
                        hasQuotedText = messageBody.hasQuotedText,
                        hasCalendarInvite = hasCalendarInvite,
                        banners = messageBody.banners,
                        attachments = messageBody.attachments,
                        transformations = messageBody.transformations
                    ).right()
                }
        }
}
