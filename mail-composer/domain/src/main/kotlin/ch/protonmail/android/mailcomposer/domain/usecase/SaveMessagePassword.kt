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

package ch.protonmail.android.mailcomposer.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import ch.protonmail.android.mailcommon.domain.annotation.MissingRustApi
import ch.protonmail.android.mailcommon.domain.model.DataError
import ch.protonmail.android.mailcomposer.domain.model.MessagePassword
import ch.protonmail.android.mailcomposer.domain.repository.MessagePasswordRepository
import ch.protonmail.android.mailmessage.domain.model.MessageId
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@MissingRustApi
// Rust not exposing API to save message password yet
class SaveMessagePassword @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto,
    private val messagePasswordRepository: MessagePasswordRepository
) {

    suspend operator fun invoke(
        userId: UserId,
        messageId: MessageId,
        password: String,
        passwordHint: String?,
        action: SaveMessagePasswordAction = SaveMessagePasswordAction.Create
    ): Either<DataError.Local, Unit> = either {
        val encryptedPassword = runCatching { keyStoreCrypto.encrypt(password) }.fold(
            onSuccess = { it },
            onFailure = { raise(DataError.Local.EncryptionError) }
        )

        if (action == SaveMessagePasswordAction.Create) {
            messagePasswordRepository.saveMessagePassword(
                MessagePassword(userId, messageId, encryptedPassword, passwordHint)
            )
        } else {
            messagePasswordRepository.updateMessagePassword(
                userId, messageId, encryptedPassword, passwordHint
            )
        }.bind()
    }
}

enum class SaveMessagePasswordAction { Create, Update }
