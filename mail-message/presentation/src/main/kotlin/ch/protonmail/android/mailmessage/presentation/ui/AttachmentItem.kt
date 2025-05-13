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

package ch.protonmail.android.mailmessage.presentation.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.component.ProtonAlertDialog
import ch.protonmail.android.design.compose.component.ProtonAlertDialogButton
import ch.protonmail.android.design.compose.component.ProtonAlertDialogText
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.mailmessage.domain.model.AttachmentId
import ch.protonmail.android.mailmessage.domain.model.AttachmentState
import ch.protonmail.android.mailmessage.presentation.R
import ch.protonmail.android.mailmessage.presentation.model.attachment.AttachmentMetadataUiModel
import ch.protonmail.android.mailmessage.presentation.sample.AttachmentMetadataUiModelSamples
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AttachmentItem(
    modifier: Modifier = Modifier,
    attachmentUiModel: AttachmentMetadataUiModel,
    onAttachmentItemClicked: (attachmentId: AttachmentId) -> Unit,
    onAttachmentItemDeleteClicked: (attachmentId: AttachmentId) -> Unit
) {
    val context = LocalContext.current
    val shouldShowPermissionDialog = remember { mutableStateOf(false) }
    val nameTextColor = if (attachmentUiModel.status == AttachmentState.Uploading) ProtonTheme.colors.textHint else
        ProtonTheme.colors.textWeak

    // Use a conditional to skip permission logic in preview mode
    val externalStoragePermission =
        rememberPermissionState(
            permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            onPermissionResult = { result ->
                if (result) {
                    onAttachmentItemClicked(AttachmentId(attachmentUiModel.id.value))
                } else {
                    shouldShowPermissionDialog.value = true
                }
            }
        )

    if (shouldShowPermissionDialog.value) {
        ProtonAlertDialog(
            title = stringResource(id = R.string.attachment_permission_dialog_title),
            text = { ProtonAlertDialogText(R.string.attachment_permission_dialog_message) },
            dismissButton = {
                ProtonAlertDialogButton(R.string.attachment_permission_dialog_dismiss_button) {
                    shouldShowPermissionDialog.value = false
                }
            },
            confirmButton = {
                ProtonAlertDialogButton(R.string.attachment_permission_dialog_action_button) {
                    shouldShowPermissionDialog.value = false
                    context.startActivity(
                        Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                }
            },
            onDismissRequest = { shouldShowPermissionDialog.value = false }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MailDimens.Attachment.ItemRowHeight)
            .background(
                color = ProtonTheme.colors.interactionWeakNorm,
                shape = ProtonTheme.shapes.huge
            )
            .clip(ProtonTheme.shapes.huge)
            .clickable {
                if (!attachmentUiModel.deletable) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || externalStoragePermission.status.isGranted) {
                        onAttachmentItemClicked(AttachmentId(attachmentUiModel.id.value))
                    } else {
                        externalStoragePermission.launchPermissionRequest()
                    }
                }
            }
            .padding(
                start = ProtonDimens.Spacing.Medium,
                end = ProtonDimens.Spacing.Large
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(ProtonDimens.IconSize.Medium)
                .testTag(AttachmentItemTestTags.Icon),
            painter = painterResource(id = attachmentUiModel.icon),
            contentDescription = stringResource(
                id = R.string.attachment_type_description,
                stringResource(id = attachmentUiModel.contentDescription)
            )
        )

        Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Standard))

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .testTag(AttachmentItemTestTags.Name),
                text = attachmentUiModel.name,
                style = ProtonTheme.typography.bodyMedium,
                color = nameTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Medium))
            Text(
                modifier = Modifier.testTag(AttachmentItemTestTags.Size),
                text = Formatter.formatShortFileSize(context, attachmentUiModel.size),
                style = ProtonTheme.typography.bodyMedium.copy(
                    color = ProtonTheme.colors.textHint
                )
            )
        }

        if (attachmentUiModel.deletable) {
            Spacer(modifier = Modifier.width(ProtonDimens.Spacing.Large))
            Box(
                modifier = Modifier
                    .size(MailDimens.Attachment.UploadingSpinnerSize),
                contentAlignment = Alignment.Center
            ) {
                if (attachmentUiModel.status == AttachmentState.Uploading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(AttachmentItemTestTags.Loader),
                        trackColor = ProtonTheme.colors.brandMinus20,
                        color = ProtonTheme.colors.brandNorm,
                        strokeWidth = ProtonDimens.BorderSize.Medium
                    )
                }

                Icon(
                    modifier = Modifier
                        .size(ProtonDimens.IconSize.Medium)
                        .clickable { onAttachmentItemDeleteClicked(AttachmentId(attachmentUiModel.id.value)) }
                        .testTag(AttachmentItemTestTags.Delete),
                    painter = painterResource(id = R.drawable.ic_proton_cross_small),
                    contentDescription = null
                )
            }

        }
    }
}

@Composable
@Preview(showBackground = true)
fun AttachmentItemPreview() {
    ProtonTheme {
        AttachmentItem(
            attachmentUiModel = AttachmentMetadataUiModelSamples.Invoice,
            onAttachmentItemClicked = {},
            onAttachmentItemDeleteClicked = {}
        )
    }
}

@Composable
@Preview(showBackground = true)
fun DeletableAttachmentItemPreview() {
    ProtonTheme {
        Box(
            modifier = Modifier
                .background(ProtonTheme.colors.backgroundNorm)
                .padding(ProtonDimens.Spacing.Large)
        ) {
            AttachmentItem(
                attachmentUiModel = AttachmentMetadataUiModelSamples.DeletableInvoice,
                onAttachmentItemClicked = {},
                onAttachmentItemDeleteClicked = {}
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun AttachmentItemUploadingStatePreview() {
    ProtonTheme {
        Box(
            modifier = Modifier
                .background(ProtonTheme.colors.backgroundNorm)
                .padding(ProtonDimens.Spacing.Large)
        ) {
            AttachmentItem(
                attachmentUiModel = AttachmentMetadataUiModelSamples.DeletableInvoice.copy(
                    status = AttachmentState.Uploading
                ),
                onAttachmentItemClicked = {},
                onAttachmentItemDeleteClicked = {}
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun AttachmentItemTruncationPreview() {
    ProtonTheme {
        Box(
            modifier = Modifier
                .background(ProtonTheme.colors.backgroundNorm)
                .padding(ProtonDimens.Spacing.Large)
        ) {
            AttachmentItem(
                attachmentUiModel = AttachmentMetadataUiModelSamples.DocumentWithReallyLongFileName,
                onAttachmentItemClicked = {},
                onAttachmentItemDeleteClicked = {}
            )
        }
    }
}

object AttachmentItemTestTags {

    const val Loader = "AttachmentLoader"
    const val Icon = "AttachmentIcon"
    const val Name = "AttachmentName"
    const val Extension = "AttachmentExtension"
    const val Size = "AttachmentSize"
    const val Delete = "AttachmentDelete"
}
