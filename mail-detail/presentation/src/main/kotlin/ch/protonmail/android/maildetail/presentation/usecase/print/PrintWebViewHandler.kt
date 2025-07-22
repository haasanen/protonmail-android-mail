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

package ch.protonmail.android.maildetail.presentation.usecase.print

import java.io.ByteArrayInputStream
import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.mailmessage.domain.model.EmbeddedImage
import ch.protonmail.android.mailmessage.domain.model.MessageId
import ch.protonmail.android.mailmessage.presentation.extension.getSecuredWebResourceResponse
import ch.protonmail.android.mailmessage.presentation.extension.isEmbeddedImage
import ch.protonmail.android.mailmessage.presentation.extension.isRemoteContent
import ch.protonmail.android.mailmessage.presentation.extension.isRemoteUnsecuredContent
import timber.log.Timber
import javax.inject.Inject

class PrintWebViewHandler @Inject constructor() {

    data class PrintWebViewConfig(
        val context: Context,
        val htmlContent: String,
        val subject: String,
        val resourceConfig: ResourceLoadingConfig
    )

    data class ResourceLoadingConfig(
        val messageId: MessageId,
        val loadEmbeddedImage: (MessageId, String) -> EmbeddedImage?,
        val showRemoteContent: Boolean,
        val showEmbeddedImages: Boolean
    )

    fun createPrintWebView(config: PrintWebViewConfig): WebView {
        return WebView(config.context).apply {
            webViewClient = PrintWebViewClient(
                context = config.context,
                subject = config.subject,
                resourceConfig = config.resourceConfig
            )

            settings.apply {
                loadWithOverviewMode = true
                useWideViewPort = true
                javaScriptEnabled = false
                allowFileAccess = false
            }

            loadDataWithBaseURL(null, config.htmlContent, "text/html", "UTF-8", null)
        }
    }

    private class PrintWebViewClient(
        private val context: Context,
        private val subject: String,
        private val resourceConfig: ResourceLoadingConfig
    ) : WebViewClient() {

        override fun onPageFinished(webView: WebView, url: String) {
            triggerPrint(webView)
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            return when {
                !resourceConfig.showRemoteContent && request?.isRemoteContent() == true -> {
                    WebResourceResponse("", "", null)
                }

                resourceConfig.showEmbeddedImages && request?.isEmbeddedImage() == true -> {
                    handleEmbeddedImageRequest(request)
                }

                request?.isRemoteUnsecuredContent() == true -> {
                    request.getSecuredWebResourceResponse()
                }

                else -> super.shouldInterceptRequest(view, request)
            }
        }

        private fun handleEmbeddedImageRequest(request: WebResourceRequest): WebResourceResponse? {
            val imageId = "<${request.url.schemeSpecificPart}>"
            return resourceConfig.loadEmbeddedImage(resourceConfig.messageId, imageId)?.let {
                WebResourceResponse(it.mimeType, "", ByteArrayInputStream(it.data))
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private fun triggerPrint(webView: WebView) {
            try {
                val printAdapter = webView.createPrintDocumentAdapter(subject)
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                printManager.print(subject, printAdapter, PrintAttributes.Builder().build())
            } catch (e: Exception) {
                Timber.e(e, "Error printing message")
                Toast.makeText(
                    context,
                    context.getString(R.string.error_print_message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
