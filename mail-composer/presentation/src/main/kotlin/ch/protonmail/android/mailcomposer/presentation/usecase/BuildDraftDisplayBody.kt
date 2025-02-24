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

package ch.protonmail.android.mailcomposer.presentation.usecase

import java.io.IOException
import android.content.Context
import android.content.res.Resources
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBody
import ch.protonmail.android.mailmessage.presentation.model.MessageBodyWithType
import ch.protonmail.android.mailmessage.presentation.usecase.SanitizeHtmlOfDecryptedMessageBody
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

private const val EDITOR_ID = "EditorId"

class BuildDraftDisplayBody @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sanitizeHtmlOfDecryptedMessageBody: SanitizeHtmlOfDecryptedMessageBody
) {

    operator fun invoke(messageBodyWithType: MessageBodyWithType): DraftDisplayBody {
        val bodyContent: String = sanitizeHtmlOfDecryptedMessageBody(messageBodyWithType)
        val css: String = getCustomCss(context)
        val javascript: String = getJavascript()

        return buildHtmlTemplate(bodyContent, css, javascript)
    }

    private fun buildHtmlTemplate(
        bodyContent: String,
        customCss: String,
        javascript: String
    ): DraftDisplayBody {
        val html = """
            <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>Proton HTML Editor</title>
                    <meta id="myViewport" name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, shrink-to-fit=yes">
                    <meta id="myCSP" http-equiv="Content-Security-Policy" content="script-src 'unsafe-inline' 'unsafe-eval'">
                    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                    <style>
                        $customCss
                    </style>
                    <script>
                        $javascript
                    </script>
                </head>
                <body>
                    <div id="editor_header">
                    </div>
                    <div role="textbox" aria-multiline="true" id="$EDITOR_ID" contentEditable="true" placeholder="" class="placeholder">
                        $bodyContent
                    </div>
                    <div id="editor_footer">
                </div>
                </body>
                </html>
        """.trimIndent()

        return DraftDisplayBody(html)
    }

    @SuppressWarnings("FunctionOnlyReturningConstant")
    private fun getJavascript() = "alert(\"foo\")"

    private fun getCustomCss(context: Context): String = try {
        context.resources.openRawResource(R.raw.css_reset_with_custom_props).use {
            it.readBytes().decodeToString()
        }
    } catch (notFoundException: Resources.NotFoundException) {
        Timber.e(notFoundException, "Raw css resource is not found")
        ""
    } catch (ioException: IOException) {
        Timber.e(ioException, "Failed to read raw css resource")
        ""
    }

}
