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

package ch.protonmail.android.mailcomposer.presentation.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcommon.presentation.Effect
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcomposer.presentation.R
import ch.protonmail.android.mailcomposer.presentation.model.ComposerChipsFieldState
import ch.protonmail.android.mailcomposer.presentation.model.InvalidRecipientsError
import ch.protonmail.android.mailcomposer.presentation.ui.chips.ChipsListState
import ch.protonmail.android.mailcomposer.presentation.ui.chips.ChipsListState.Companion.ChipsCreationRegex
import ch.protonmail.android.mailcomposer.presentation.ui.chips.item.ChipItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ComposerChipsListViewModel @Inject constructor() : ViewModel() {

    internal val textFieldState = TextFieldState()

    private val mutableState = MutableStateFlow(initialChipsListState())
    internal val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch { observe() }
    }

    fun updateItems(chipsList: List<ChipItem>) {
        val invalidItems = chipsList.filterIsInstance<ChipItem.Invalid>()
        if (invalidItems.isNotEmpty()) {
            mutableState.update {
                it.copy(
                    invalidRecipientsWarning = InvalidRecipientsError(
                        invalidItems,
                        TextUiModel.TextRes(R.string.composer_error_invalid_email)
                    )
                )
            }
        }

        mutableState.value.listState.updateItems(chipsList)
    }

    private suspend fun observe() = snapshotFlow { textFieldState.text }
        .collectLatest { text ->
            // This is not ideal, but it's due to how the existing Chips state works.
            mutableState.value.listState.type(text.toString())

            if (ChipsCreationRegex.containsMatchIn(text)) textFieldState.edit { delete(0, length) }
            mutableState.update { it.copy(suggestionsTermTyped = Effect.of(text.toString())) }
        }

    private fun onListChanged(list: List<ChipItem>) {
        val deduplicatedList = list.distinctBy { it.value }
        val duplicateRemovalWarning = if (deduplicatedList.size != list.size) {
            Effect.of(TextUiModel(R.string.composer_error_duplicate_recipient))
        } else {
            Effect.empty()
        }

        mutableState.update {
            it.copy(
                duplicateRemovalWarning = duplicateRemovalWarning,
                listChanged = Effect.of(deduplicatedList)
            )
        }
        mutableState.value.listState.updateItems(deduplicatedList)
    }

    private fun initialChipsListState() = ComposerChipsFieldState(
        ChipsListState(this::onListChanged)
    )
}
