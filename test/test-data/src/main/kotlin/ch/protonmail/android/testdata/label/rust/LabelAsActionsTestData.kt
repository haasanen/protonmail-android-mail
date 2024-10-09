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

package ch.protonmail.android.testdata.label.rust

import ch.protonmail.android.maillabel.domain.model.LabelAsActions
import ch.protonmail.android.maillabel.domain.model.LabelId
import ch.protonmail.android.maillabel.domain.sample.LabelSample
import ch.protonmail.android.testdata.label.LabelTestData

object LabelAsActionsTestData {

    val actions = LabelAsActions(
        labels = listOf(
            LabelTestData.selectedLabelAction,
            LabelTestData.unselectedLabelAction,
            LabelTestData.partialSelectedLabelAction
        ),
        selected = listOf(LabelId("2")),
        partiallySelected = listOf(LabelId("3"))
    )

    val onlySelectedActions = LabelAsActions(
        listOf(LabelTestData.selectedLabelAction),
        listOf(LabelTestData.selectedLabelAction.labelId),
        emptyList()
    )

    val unselectedActions = LabelAsActions(
        labels = listOf(
            LabelSample.Document,
            LabelSample.Label2021,
            LabelSample.Label2022
        ),
        selected = emptyList(),
        partiallySelected = emptyList()
    )

}
