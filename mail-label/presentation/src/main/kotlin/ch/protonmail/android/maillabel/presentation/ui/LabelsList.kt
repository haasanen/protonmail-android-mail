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

package ch.protonmail.android.maillabel.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.design.compose.theme.labelSmallNorm
import ch.protonmail.android.mailcommon.presentation.compose.MailDimens
import ch.protonmail.android.maillabel.presentation.model.LabelUiModel
import ch.protonmail.android.maillabel.presentation.previewdata.MailboxItemLabelsPreviewDataProvider
import ch.protonmail.android.maillabel.presentation.ui.MailboxItemLabels.Plus1CharLimit
import ch.protonmail.android.maillabel.presentation.ui.MailboxItemLabels.Plus2CharsLimit
import ch.protonmail.android.maillabel.presentation.ui.MailboxItemLabels.Plus3CharsLimit

@Composable
fun LabelsList(
    modifier: Modifier = Modifier,
    labels: List<LabelUiModel>,
    isExpanded: Boolean = false
) {
    SubcomposeLayout(
        modifier = modifier
            .padding(top = ProtonDimens.Spacing.Tiny)
            .wrapContentSize()
    ) { constraints ->

        val labelsMeasurables = labels.map { label ->
            label to subcompose(label.id) {
                Label(label = label)
            }.single()
        }

        val (placeables, height) = measure(
            labels = labels,
            isExpanded = isExpanded,
            labelsMeasurables = labelsMeasurables,
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

private fun SubcomposeMeasureScope.measure(
    labels: List<LabelUiModel>,
    isExpanded: Boolean,
    labelsMeasurables: List<Pair<LabelUiModel, Measurable>>,
    constraints: Constraints
): MeasureResult {
    val plusOneDigitWidth = measurePlusTextWidth(constraints, Plus1CharLimit)
    val plusTwoDigitWidth = measurePlusTextWidth(constraints, Plus2CharsLimit)
    val plusThreeDigitWidth = measurePlusTextWidth(constraints, Plus3CharsLimit)

    var labelsWidth = 0
    var notPlacedCount = labels.size

    fun plusPlaceableWidth(): Int {
        val notPlacedCountExcludingCurrent = notPlacedCount - 1
        return when {
            isExpanded -> 0
            notPlacedCountExcludingCurrent <= 0 -> 0
            notPlacedCount <= Plus1CharLimit -> plusOneDigitWidth
            notPlacedCount <= Plus2CharsLimit -> plusTwoDigitWidth
            else -> plusThreeDigitWidth
        }
    }

    val labelsPlaceables = labelsMeasurables.mapIndexedNotNull { index, (_, measurable) ->
        val availableWidth = constraints.maxWidth - labelsWidth - plusPlaceableWidth()
        if (availableWidth <= 0) return@mapIndexedNotNull null

        val placeable = measurable.measure(
            constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
        )

        if (placeable.width <= availableWidth) {
            labelsWidth += placeable.width
            notPlacedCount--
            placeable
        } else if (index == labelsMeasurables.lastIndex) {
            null
        } else {
            null
        }
    }

    return calculateCoordinates(labelsPlaceables, constraints, isExpanded, notPlacedCount)
}

private fun SubcomposeMeasureScope.calculateCoordinates(
    labelsPlaceables: List<Placeable>,
    constraints: Constraints,
    isExpanded: Boolean,
    notPlacedCount: Int
): MeasureResult {
    val rowHeight = labelsPlaceables.firstOrNull()?.height ?: 0
    var rowsCount = 1

    var x = 0
    var y = 0
    var availableRowWidth = constraints.maxWidth
    val labelsPlaceablesCoordinates = labelsPlaceables.associateWith { placeable ->

        val rowCanFitPlaceable = when (isExpanded) {
            true -> availableRowWidth - placeable.width >= 0
            false -> true
        }

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

    val placeablesCoordinates = if (notPlacedCount > 0) {
        val plusSignPlaceable = subcompose(notPlacedCount) { PlusText(count = notPlacedCount) }
            .single()
            .measure(constraints)

        val plusSignCoordinates = PlaceableCoordinates(
            x = x - ProtonDimens.Spacing.Standard.roundToPx() + ProtonDimens.Spacing.Small.roundToPx(),
            y = (rowsCount - 1) * rowHeight + (rowHeight - plusSignPlaceable.height) / 2
        )
        labelsPlaceablesCoordinates + (plusSignPlaceable to plusSignCoordinates)
    } else {
        labelsPlaceablesCoordinates
    }

    return MeasureResult(
        placeablesCoordinates = placeablesCoordinates,
        height = rowsCount * rowHeight
    )
}

private fun SubcomposeMeasureScope.measurePlusTextWidth(constraints: Constraints, charsLimit: Int) =
    subcompose(charsLimit) { PlusText(count = charsLimit) }
        .maxOf { it.measure(constraints).width }

@Composable
private fun Label(label: LabelUiModel) {
    Box(
        modifier = Modifier
            .testTag(LabelsListTestTags.Label)
            .padding(
                end = ProtonDimens.Spacing.Compact
            )
            .height(MailDimens.MailboxItemLabelHeight)
            .background(label.color, shape = ProtonTheme.shapes.medium)
            .padding(horizontal = ProtonDimens.Spacing.Standard),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.name,
            style = ProtonTheme.typography.labelSmallNorm.copy(
                color = ProtonTheme.colors.floatyText,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Visible
        )
    }
}


@Composable
private fun PlusText(count: Int) {
    Box(
        modifier = Modifier
            .height(MailDimens.MailboxItemLabelHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            text = "+$count",
            style = ProtonTheme.typography.labelMedium.copy(
                color = ProtonTheme.colors.textWeak
            ),
            maxLines = 1,
            textAlign = TextAlign.Center
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

object MailboxItemLabels {

    internal const val Plus1CharLimit = 9
    internal const val Plus2CharsLimit = 99
    internal const val Plus3CharsLimit = 999
}

@Composable
@Preview(showBackground = true, widthDp = 400)
private fun MailboxItemLabelsPreview(
    @PreviewParameter(MailboxItemLabelsPreviewDataProvider::class) labels: List<LabelUiModel>
) {
    ProtonTheme {
        LabelsList(labels = labels)
    }
}

object LabelsListTestTags {

    const val Label = "Label"
}
