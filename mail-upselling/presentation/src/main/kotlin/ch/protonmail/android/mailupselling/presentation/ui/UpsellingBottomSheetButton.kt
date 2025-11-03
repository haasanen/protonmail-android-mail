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

package ch.protonmail.android.mailupselling.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.bodyLargeNorm
import ch.protonmail.android.design.compose.theme.bodyMediumWeak
import ch.protonmail.android.mailupselling.domain.model.UpsellingEntryPoint
import ch.protonmail.android.mailupselling.presentation.R
import ch.protonmail.android.mailupselling.presentation.model.UpsellingVisibility
import ch.protonmail.android.mailupselling.presentation.viewmodel.UpsellingButtonViewModel

/**
 * A button to be displayed within a bottom sheet to allow feature upsells.
 *
 * In case no plans are available or the user has a non compatible config (e.g. no Play Services),
 * the element will still be rendered but the invoked onClick callback will either be
 * the provided [onUnavailableUpsell] lambda or a generic Toast message (default).
 */
@Composable
fun UpsellingBottomSheetButton(
    modifier: Modifier = Modifier,
    text: String,
    hint: String = stringResource(R.string.upselling_button_hint),
    upsellingEntryPoint: UpsellingEntryPoint.Feature,
    onUpsellNavigation: (type: UpsellingVisibility) -> Unit,
    onUnavailableUpsell: (() -> Unit)? = null,
    layout: UpsellingBottomSheetButtonLayout = Row
) {
    val viewModel = hiltViewModel<UpsellingButtonViewModel, UpsellingButtonViewModel.Factory> { factory ->
        factory.create(upsellingEntryPoint)
    }

    val state = viewModel.state.collectAsStateWithLifecycle()
    val type = state.value.visibility
    val context = LocalContext.current
    val fallbackText = stringResource(R.string.upselling_upgrade_plan_generic)

    val onNavigateToUpsell: () -> Unit = {
        when (state.value.visibility) {
            is UpsellingVisibility.Hidden -> if (onUnavailableUpsell != null) {
                onUnavailableUpsell()
            } else {
                Toast.makeText(context, fallbackText, Toast.LENGTH_SHORT).show()
            }

            is UpsellingVisibility.Promotional,
            is UpsellingVisibility.Normal -> onUpsellNavigation(type)
        }
    }

    UpsellingBottomSheetButtonContent(
        modifier = modifier,
        text = text,
        hint = hint,
        layout = layout,
        onClick = onNavigateToUpsell
    )
}

@Composable
private fun UpsellingBottomSheetButtonContent(
    modifier: Modifier = Modifier,
    text: String,
    hint: String,
    layout: UpsellingBottomSheetButtonLayout = Row,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = ProtonTheme.shapes.extraLarge,
        colors = CardDefaults.outlinedCardColors(
            containerColor = ProtonTheme.colors.backgroundInvertedSecondary
        ),
        border = UpsellingLayoutValues.UpsellCards.outlineBorderStoke
    ) {
        when (layout) {
            is Tile -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UpsellIcon(
                        modifier = Modifier
                            .defaultMinSize(minWidth = ProtonDimens.IconSize.MediumLarge)
                            .padding(bottom = ProtonDimens.Spacing.Tiny)
                    )
                    ButtonTextContent(text, hint, ProtonDimens.Spacing.Tiny)
                }
            }

            is Row -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ProtonDimens.Spacing.Large),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ButtonTextContent(text, hint, ProtonDimens.Spacing.Small)
                    }
                    UpsellIcon()
                }
            }
        }
    }
}

@Composable
private fun UpsellIcon(modifier: Modifier = Modifier) = Icon(
    modifier = modifier,
    painter = painterResource(id = R.drawable.ic_proton_mail_upsell),
    contentDescription = null,
    tint = Color.Unspecified
)

@Composable
private fun ButtonTextContent(
    text: String,
    hint: String,
    spacerSize: Dp
) {
    Text(
        text = text,
        style = ProtonTheme.typography.bodyLargeNorm,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )

    Spacer(modifier = Modifier.size(spacerSize))

    Text(
        text = hint,
        style = ProtonTheme.typography.bodyMediumWeak,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

sealed class UpsellingBottomSheetButtonLayout
object Row : UpsellingBottomSheetButtonLayout()
object Tile : UpsellingBottomSheetButtonLayout()

@Preview
@Composable
private fun UpsellingBottomSheetButtonPreview() {
    ProtonTheme {
        UpsellingBottomSheetButtonContent(
            text = "Custom",
            hint = "Upgrade for full flexibility",
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun UpsellingBottomSheetButtonTilePreview() {
    ProtonTheme {
        UpsellingBottomSheetButtonContent(
            text = "Custom",
            hint = "Upgrade for full flexibility",
            layout = Tile,
            onClick = {}
        )
    }
}
