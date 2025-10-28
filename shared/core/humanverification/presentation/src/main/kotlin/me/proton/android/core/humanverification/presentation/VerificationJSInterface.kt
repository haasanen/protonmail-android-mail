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

package me.proton.android.core.humanverification.presentation

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.proton.core.util.kotlin.deserializeOrNull

/** JS Interface used for communication between the WebView contents and the client. */
@Keep
class VerificationJSInterface(
    private val scope: CoroutineScope,
    private val handleVerificationResponse: (HV3ResponseMessage) -> Unit
) {

    /** Used as callback by all verification methods once the challenge is solved. */
    @JavascriptInterface
    fun dispatch(response: String) {
        response.deserializeOrNull<HV3ResponseMessage>()?.let {
            scope.launch {
                handleVerificationResponse(it)
            }
        }
    }
}
