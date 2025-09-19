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

package ch.protonmail.android.mailcontact.presentation.contactdetails.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsItemBadgeUiModel

@Composable
fun ContactDetailsBadges(badges: List<ContactDetailsItemBadgeUiModel>, modifier: Modifier = Modifier) {
    SubcomposeLayout(
        modifier = modifier.wrapContentSize()
    ) { constraints ->
        val badgesMeasurables = badges.map { badge ->
            badge to subcompose(badge.name) {
                Badge(name = badge.name, color = badge.color)
            }.single()
        }

        val (placeables, height) = measure(
            badgesMeasurables = badgesMeasurables,
            constraints = constraints
        )

        layout(
            width = constraints.maxWidth,
            height = height
        ) {
            placeables.forEach { (placeable, coordinates) ->
                placeable.place(x = coordinates.x, y = coordinates.y)
            }
        }
    }
}

private fun measure(
    badgesMeasurables: List<Pair<ContactDetailsItemBadgeUiModel, Measurable>>,
    constraints: Constraints
): MeasureResult {
    val badgesPlaceables = badgesMeasurables.map { (_, measurable) ->
        measurable.measure(constraints)
    }

    return calculateCoordinates(badgesPlaceables, constraints)
}

private fun calculateCoordinates(badgesPlaceables: List<Placeable>, constraints: Constraints): MeasureResult {
    val rowHeight = badgesPlaceables.firstOrNull()?.height ?: 0
    var rowsCount = 1

    var x = 0
    var y = 0
    var availableRowWidth = constraints.maxWidth
    val badgesPlaceablesCoordinates = badgesPlaceables.associateWith { placeable ->

        val rowCanFitPlaceable = availableRowWidth - placeable.width >= 0

        if (rowCanFitPlaceable.not()) {
            rowsCount++
            availableRowWidth = constraints.maxWidth
            y += rowHeight
            x = 0
        }

        PlaceableCoordinates(x, y).also {
            x += placeable.width
            availableRowWidth -= placeable.width
        }
    }

    return MeasureResult(
        placeablesCoordinates = badgesPlaceablesCoordinates,
        height = rowsCount * rowHeight
    )
}

@Composable
private fun Badge(
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(end = ProtonDimens.Spacing.Small, bottom = ProtonDimens.Spacing.Small)
            .height(20.dp)
            .background(color = color, shape = ProtonTheme.shapes.large)
            .padding(horizontal = ProtonDimens.Spacing.Standard),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = ProtonTheme.typography.labelSmall.copy(color = Color.White),
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private data class MeasureResult(
    val placeablesCoordinates: Map<Placeable, PlaceableCoordinates>,
    val height: Int
)

private data class PlaceableCoordinates(
    val x: Int,
    val y: Int
)
