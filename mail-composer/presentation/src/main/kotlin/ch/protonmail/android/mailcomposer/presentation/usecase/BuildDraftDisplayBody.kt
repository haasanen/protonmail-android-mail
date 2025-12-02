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

import ch.protonmail.android.mailcommon.domain.coroutines.AppScope
import ch.protonmail.android.mailcomposer.domain.model.ComposerValues.EDITOR_ID
import ch.protonmail.android.mailcomposer.domain.model.DraftBody
import ch.protonmail.android.mailcomposer.domain.model.DraftHead
import ch.protonmail.android.mailcomposer.domain.usecase.GenerateCspNonce
import ch.protonmail.android.mailcomposer.presentation.model.DraftDisplayBodyUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import javax.inject.Inject

class BuildDraftDisplayBody @Inject constructor(
    private val getCustomCss: GetCustomCss,
    private val getCustomJs: GetCustomJs,
    private val generateCspNonce: GenerateCspNonce,
    @AppScope private val coroutineScope: CoroutineScope
) {

    private val cssAndJs: Deferred<Pair<String, String>> = coroutineScope.async {
        getCustomCss() to getCustomJs()
    }

    suspend operator fun invoke(draftHead: DraftHead, draftBody: DraftBody): DraftDisplayBodyUiModel {
        val cspNonce = generateCspNonce()
        val (css, javascript) = cssAndJs.await()

        return buildHtmlTemplate(draftBody, draftHead, css, javascript, cspNonce.value)
    }

    private fun buildHtmlTemplate(
        bodyContent: DraftBody,
        draftHead: DraftHead,
        customCss: String,
        javascript: String,
        cspNonce: String
    ): DraftDisplayBodyUiModel {
        val html = """
            <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>Proton HTML Editor</title>
                    <meta id="myViewport" name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no, shrink-to-fit=yes">
                    <meta id="myCSP" http-equiv="Content-Security-Policy" content="script-src 'nonce-$cspNonce'">
                    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                    
                    <!-- START Rust SDK CSS overrides -->
                    ${draftHead.value}
                    <!-- END Rust SDK CSS overrides -->
                    
                    <!-- START Client CSS overrides -->
                    <style>
                        $customCss
                    </style>
                    <!-- END Client CSS overrides -->
                </head>
                <body>
                    <div id="editor_header"></div>
                    <div role="textbox" aria-multiline="true" id="$EDITOR_ID" contentEditable="true" placeholder="" class="placeholder">
                        ${bodyContent.value}
                    </div>
                    <div id="editor_footer"></div>

                    <script nonce="$cspNonce">
                        $javascript
                    </script>
                </body>
                </html>
        """.trimIndent()

        return DraftDisplayBodyUiModel(html)
    }
}
