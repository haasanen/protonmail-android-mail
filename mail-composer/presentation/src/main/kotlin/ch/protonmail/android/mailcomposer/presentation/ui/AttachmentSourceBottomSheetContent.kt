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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.titleLargeNorm
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailmessage.presentation.ui.bottomsheet.ActionGroup
import ch.protonmail.android.mailmessage.presentation.ui.bottomsheet.ActionGroupItem
import ch.protonmail.android.uicomponents.BottomNavigationBarSpacer
import kotlinx.collections.immutable.persistentListOf

@Composable
@Suppress("UseComposableActions")
fun AttachmentSourceBottomSheetContent(
    onCamera: () -> Unit,
    onFiles: () -> Unit,
    onPhotos: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ProtonTheme.colors.backgroundInvertedNorm)
            .padding(ProtonDimens.Spacing.Large)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .scrollable(
                    rememberScrollableState(consumeScrollDelta = { 0f }),
                    orientation = Orientation.Vertical
                )
        ) {
            Text(
                modifier = Modifier
                    .background(ProtonTheme.colors.backgroundInvertedNorm)
                    .align(Alignment.CenterHorizontally),
                text = stringResource(R.string.composer_add_attachments_content_description),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ProtonTheme.typography.titleLargeNorm,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.size(ProtonDimens.Spacing.Large))

            val actions = persistentListOf(Sources.Photos, Sources.Camera, Sources.Files)

            ActionGroup(
                modifier = Modifier,
                items = actions,
                onItemClicked = { source ->
                    when (source) {
                        Sources.Camera -> onCamera()
                        Sources.Files -> onFiles()
                        Sources.Photos -> onPhotos()
                    }
                }
            ) { action, onClick ->
                ActionGroupItem(
                    modifier = Modifier,
                    icon = action.icon,
                    description = stringResource(action.description),
                    contentDescription = stringResource(action.description),
                    onClick = onClick
                )
            }

            BottomNavigationBarSpacer()
        }
    }
}

private enum class Sources(@DrawableRes val icon: Int, @StringRes val description: Int) {
    Camera(R.drawable.ic_proton_camera, R.string.composer_add_attachments_bottom_sheet_from_camera),
    Files(R.drawable.ic_proton_folder_open, R.string.composer_add_attachments_bottom_sheet_import_from),
    Photos(R.drawable.ic_proton_image, R.string.composer_add_attachments_bottom_sheet_from_photos)
}

@Preview(showBackground = true)
@Composable
private fun PreviewInlineActions() {
    ProtonTheme {
        AttachmentSourceBottomSheetContent(
            onCamera = {},
            onFiles = {},
            onPhotos = {}
        )
    }
}
