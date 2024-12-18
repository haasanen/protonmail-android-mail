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

package me.proton.android.core.auth.presentation.challenge

import me.proton.core.challenge.data.frame.ChallengeFrame
import javax.inject.Inject

internal class GetChallengePayload @Inject constructor() {

    @Suppress("ForbiddenComment")
    operator fun invoke(frames: List<ChallengeFrame>): Map<String, ChallengeFrame> {
        return frames.mapIndexed { index, frame ->
            // FIXME Add support for other apps
            Pair("$MAIL_FRAME_KEY_PREFIX-$index", frame)
        }.toMap()
    }
}
