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

package ch.protonmail.android.testdata.avatar

import java.io.File
import ch.protonmail.android.mailmessage.domain.model.AvatarImageState
import ch.protonmail.android.mailmessage.domain.model.AvatarImageStates

object AvatarImageStatesTestData {
    private val mockFile = File("mockImage1.png")

    val SampleData1 = AvatarImageStates(
        states = mapOf(
            "user1@example.com" to AvatarImageState.Data(mockFile),
            "user2@example.com" to AvatarImageState.Loading,
            "user3@example.com" to AvatarImageState.NoImageAvailable
        )
    )
}
