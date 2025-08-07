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

package ch.protonmail.android.mailcomposer.presentation.reducer.modifications

import ch.protonmail.android.mailcomposer.domain.model.ExternalEncryptionPassword
import ch.protonmail.android.mailcomposer.presentation.model.ComposerState
import ch.protonmail.android.mailcomposer.presentation.model.ExpirationTimeUiModel
import ch.protonmail.android.mailcomposer.presentation.model.ScheduleSendOptionsUiModel

internal sealed interface AccessoriesStateModification : ComposerStateModification<ComposerState.Accessories> {

    override fun apply(state: ComposerState.Accessories): ComposerState.Accessories = when (this) {
        is MessageExpirationUpdated -> state.copy(expirationTime = expiration)
        is MessagePasswordUpdated -> state.copy(isMessagePasswordSet = messagePassword != null)
        is ScheduleSendOptionsUpdated -> state.copy(scheduleSendOptions = options)
    }

    data class MessagePasswordUpdated(val messagePassword: ExternalEncryptionPassword?) : AccessoriesStateModification
    data class MessageExpirationUpdated(val expiration: ExpirationTimeUiModel) : AccessoriesStateModification
    data class ScheduleSendOptionsUpdated(val options: ScheduleSendOptionsUiModel) : AccessoriesStateModification
}
