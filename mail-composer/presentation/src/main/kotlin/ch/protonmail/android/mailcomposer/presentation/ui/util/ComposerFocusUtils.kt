/*
 * Copyright (c) 2025 Proton Technologies AG
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

package ch.protonmail.android.mailcomposer.presentation.ui.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.os.ResultReceiver
import android.view.ViewTreeObserver
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import timber.log.Timber

object ComposerFocusUtils {

    private const val RETRY_DELAY_MS = 32L

    /**
     * Hands off focus to the WebView's editable body (via JS `focusEditor()`) and
     * shows the IME (keyboard)
     */
    fun focusEditorAndShowKeyboard(webView: WebView, context: Context) {

        // Do nothing until the WebView is actually attached.
        if (!webView.isAttachedToWindow) {
            webView.doOnAttach { focusEditorAndShowKeyboard(webView, context) }
            return
        }

        // Wait for the Activity window to have focus before we try to show the IME.
        if (!webView.hasWindowFocus()) {
            val vto = webView.viewTreeObserver
            val listener = object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    if (hasFocus) {
                        // Remove listener and try again now that the window is focused.
                        if (vto.isAlive) vto.removeOnWindowFocusChangeListener(this)
                        focusEditorAndShowKeyboard(webView, context)
                    }
                }
            }
            vto.addOnWindowFocusChangeListener(listener)

            // Leak prevention: if the WebView detaches before window focus comes,
            webView.doOnDetach {
                if (vto.isAlive) vto.removeOnWindowFocusChangeListener(listener)
            }
            return
        }

        // Ensure the WebView itself is focused inside the window.
        if (!webView.hasFocus()) {
            Timber.d("editor-webview: WebView not focused, requesting focus...")
            webView.requestFocus()
        }

        // Now focus on the editor and show the keyboard
        webView.post {
            webView.evaluateJavascript("focusEditor();") {
                Timber.d("editor-webview: editor webview got focused; show keyboard...")

                val window: Window? = context.findActivity()?.window
                val imm = context.getIMM()

                // Defer the show by one frame
                webView.post {
                    window?.let {
                        showKeyboardUsingInsetsController(it, webView)
                    } ?: run {
                        // No Activity/Window --> IMM fallback
                        showKeyboardUsingImm(webView, imm)
                    }
                }
            }
        }
    }

    private fun showKeyboardUsingInsetsController(window: Window, webView: WebView) {
        Timber.tag("editor-webview").d("Showing IME using InsetsController")

        val controller = WindowCompat.getInsetsController(window, webView)
        controller.show(WindowInsetsCompat.Type.ime())

        // Retry after a short delay, to prevent any race conditions between Compose and View focus systems
        webView.postDelayed({
            controller.show(WindowInsetsCompat.Type.ime())
        }, RETRY_DELAY_MS)
    }

    private fun showKeyboardUsingImm(webView: WebView, imm: InputMethodManager?) {

        requestImeWithResult(webView, imm) { res ->
            when (res) {
                ImeResult.SHOWN, ImeResult.UNCHANGED_SHOWN -> {
                    Timber.tag("editor-webview").d("IMM fallback result: IME shown or already shown")
                }
                else -> {
                    // IME not shown, use FORCED show as a fallback
                    @Suppress("deprecated")
                    imm?.showSoftInput(webView, InputMethodManager.SHOW_FORCED)
                    Timber.tag("editor-webview").w("IMM fallback result: IMM failed to show IME!")
                }
            }
        }
    }

    private fun requestImeWithResult(
        webView: WebView,
        imm: InputMethodManager?,
        onResult: (ImeResult) -> Unit
    ) {
        val rr = object : ResultReceiver(webView.handler) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                val result = when (resultCode) {
                    InputMethodManager.RESULT_SHOWN -> ImeResult.SHOWN
                    InputMethodManager.RESULT_HIDDEN -> ImeResult.HIDDEN
                    InputMethodManager.RESULT_UNCHANGED_SHOWN -> ImeResult.UNCHANGED_SHOWN
                    InputMethodManager.RESULT_UNCHANGED_HIDDEN -> ImeResult.UNCHANGED_HIDDEN
                    else -> ImeResult.UNKNOWN
                }
                onResult(result)
            }
        }
        imm?.showSoftInput(webView, InputMethodManager.SHOW_IMPLICIT, rr)
    }

    fun Context.getIMM(): InputMethodManager? = this.getSystemService(InputMethodManager::class.java)

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    private enum class ImeResult {
        SHOWN, HIDDEN, UNCHANGED_SHOWN, UNCHANGED_HIDDEN, UNKNOWN
    }
}
