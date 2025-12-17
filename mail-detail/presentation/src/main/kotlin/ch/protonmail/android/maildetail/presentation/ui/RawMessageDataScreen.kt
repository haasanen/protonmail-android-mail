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

package ch.protonmail.android.maildetail.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonCenteredProgress
import ch.protonmail.android.design.compose.component.ProtonErrorMessage
import ch.protonmail.android.design.compose.component.appbar.ProtonTopAppBar
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumNorm
import ch.protonmail.android.mailcommon.presentation.NO_CONTENT_DESCRIPTION
import ch.protonmail.android.maildetail.presentation.R
import ch.protonmail.android.maildetail.presentation.model.RawMessageDataState
import ch.protonmail.android.maildetail.presentation.model.RawMessageDataType

@Composable
fun RawMessageDataScreen(state: RawMessageDataState) {
    val isDropDownExpanded = remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ProtonTheme.colors.backgroundInvertedNorm,
        topBar = {
            ProtonTopAppBar(
                backgroundColor = ProtonTheme.colors.backgroundInvertedNorm,
                title = {
                    Text(
                        text = stringResource(
                            id = when (state.type) {
                                RawMessageDataType.HTML -> R.string.raw_message_data_html_title
                                RawMessageDataType.Headers -> R.string.raw_message_data_headers_title
                            }
                        ),
                        style = ProtonTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            tint = ProtonTheme.colors.iconNorm,
                            painter = painterResource(id = R.drawable.ic_proton_close),
                            contentDescription = stringResource(
                                id = R.string.raw_message_data_close_button_content_description
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isDropDownExpanded.value = true }
                    ) {
                        Icon(
                            tint = ProtonTheme.colors.iconNorm,
                            painter = painterResource(id = R.drawable.ic_proton_three_dots_vertical),
                            contentDescription = stringResource(
                                id = R.string.raw_message_data_more_button_content_description
                            )
                        )
                    }

                    DropdownMenu(
                        expanded = isDropDownExpanded.value,
                        onDismissRequest = { isDropDownExpanded.value = false },
                        containerColor = ProtonTheme.colors.backgroundInvertedNorm
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.raw_message_data_download_action),
                                    style = ProtonTheme.typography.bodyLargeNorm
                                )
                            },
                            onClick = {},
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_proton_arrow_down_line),
                                    contentDescription = NO_CONTENT_DESCRIPTION
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.raw_message_data_copy_action),
                                    style = ProtonTheme.typography.bodyLargeNorm
                                )
                            },
                            onClick = {},
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_proton_squares),
                                    contentDescription = NO_CONTENT_DESCRIPTION
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(id = R.string.raw_message_data_learn_more_action),
                                    style = ProtonTheme.typography.bodyLargeNorm
                                )
                            },
                            onClick = {},
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_proton_info_circle),
                                    contentDescription = NO_CONTENT_DESCRIPTION
                                )
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->

        when (state) {
            is RawMessageDataState.Loading -> ProtonCenteredProgress()
            is RawMessageDataState.Data -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = ProtonTheme.colors.backgroundNorm)
                    .padding(padding)
                    .padding(horizontal = ProtonDimens.Spacing.Large)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = state.data,
                    style = ProtonTheme.typography.bodyMediumNorm.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
                Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))
            }
            is RawMessageDataState.Error -> ProtonErrorMessage(
                modifier = Modifier.padding(padding),
                errorMessage = stringResource(id = R.string.raw_message_data_error)
            )
        }
    }
}

@Preview
@Composable
fun RawMessageDataScreenPreview() {
    RawMessageDataScreen(
        state = RawMessageDataState.Data(
            type = RawMessageDataType.Headers,
            data = """
                In-Reply-To: <0102019be4a10c22-e88213bb-9c64-4c5d-b1a4-72c4c0d916ab-000000@eu-west-1.amazonses.com>
                References: <0102019be4a10c22-e88213bb-9c64-4c5d-b1a4-72c4c0d916ab-000000@eu-west-1.amazonses.com>
                X-Pm-Content-Encryption: on-compose
                X-Pm-Origin: internal
                X-Pm-Sender-Type: external
                X-Pm-Message-Format: plaintext
                X-Pm-Transport-Encryption: tls
                X-Pm-Internal-Id: 6f94e3de-5523-4494-862d-d3f10a97e17c
                X-Pm-Message-Id: UDBuN3BjeWdwbm1yY2ZrLWdqcXNk
                X-Pm-Spam-Score: -0.9
                X-Pm-Authentication-Results: none
                X-Pm-Recipient-Authentication: nat.miller%40gmail.com=none
                X-Pm-Recipient-Encryption: nat.miller%40gmail.com=none
                X-Pm-Time: 1733142942

                Return-Path: <eric.johnson@proton.ch>
                Received: from mailout.proton.ch (mailout.proton.ch [185.70.40.92])
                    by mx.proton.ch with ESMTPS id 2hFS44aG7lz9YQv
                    for <nat.miller@gmail.com>;
                    Mon, 02 Dec 2025 12:35:42 +0000 (UTC)
                Received: from mailfrontend02 (unknown [10.5.14.22])
                    by mailout.proton.ch with ESMTPA;
                    Mon, 02 Dec 2025 12:35:41 +0000 (UTC)

                DKIM-Signature: v=1; a=rsa-sha256; c=relaxed/simple;
                    d=proton.ch; s=default;
                    t=1733142941;
                    bh=YVQzA1f4S3mpuVqWs6Gq3J6nWAMfA1o3p5ZK8EBzT8k=;
                    h=Subject:To:From:Date:Message-Id:MIME-Version:Content-Type;
                    b=FQn3j2MeVIQw4LKh1z2UuC13z0u6TlqsN... (mock shortened)

                Subject: Re: Seeking Helping Hands for My Moving Day on December 4th – Your Support is Greatly Appreciated!
                To: Nathan Miller <nat.miller@gmail.com>
                From: Eric Johnson <eric.johnson@proton.ch>
                Date: Mon, 02 Dec 2025 12:35:42 +0000
                Message-Id: <k62Kf9W3X8xZ14LsJg3Z-7Cz9uQkE0iO9t3eHfG2z4S1AqB7Nc9vP0R8Qw7T5LpD@proton.ch>
                Mime-Version: 1.0
                Content-Type: text/plain; charset=UTF-8
                Content-Transfer-Encoding: quoted-printable
                X-Attached: none
                User-Agent: ProtonMail/Android-5.5.3
                X-Proton-Sender-Ip: 83.212.41.102
                X-Proton-Relay: yes
            """.trimIndent()
        )
    )
}
