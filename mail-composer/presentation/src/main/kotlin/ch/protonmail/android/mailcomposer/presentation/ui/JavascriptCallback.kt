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

package ch.protonmail.android.mailcomposer.presentation.ui

import android.webkit.JavascriptInterface
import androidx.annotation.Keep
import timber.log.Timber

const val JAVASCRIPT_CALLBACK_INTERFACE_NAME = "MessageBodyInterface"

@Keep
class JavascriptCallback(
    private val onMessageBodyChanged: (String) -> Unit,
    private val onCursorPositionChanged: (Float, Float) -> Unit,
    private val onInlineImageRemoved: (String) -> Unit,
    private val onInlineImageClicked: (String) -> Unit,
    private val onInlineImagePasted: (String) -> Unit
) {

    @JavascriptInterface
    fun onBodyUpdated(body: String) {
        onMessageBodyChanged(body)
        Timber.tag(TAG).d("Body updated, size: ${body.length} characters")
    }

    @JavascriptInterface
    fun onCaretPositionChanged(position: Float, lineHeight: Float) {
        onCursorPositionChanged(position, lineHeight)
        Timber.tag(TAG).d("Caret position changed: $position")
    }

    @JavascriptInterface
    fun onInlineImageDeleted(contentId: String) {
        onInlineImageRemoved(contentId)
    }

    @JavascriptInterface
    fun onInlineImageTapped(contentId: String) {
        onInlineImageClicked(contentId)
    }

    @JavascriptInterface
    fun onImagePasted(base64ImageData: String) {
        onInlineImagePasted(base64ImageData)
        Timber.tag(TAG).d("Image pasted of size: ${base64ImageData.length} characters")
    }

    @JavascriptInterface
    fun onDebugLog(msg: String) {
        Timber.tag(TAG).d(msg)
    }

    companion object {
        const val TAG = "JavaScript"
    }
}
