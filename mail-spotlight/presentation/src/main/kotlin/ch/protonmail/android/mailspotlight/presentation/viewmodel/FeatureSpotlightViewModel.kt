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

package ch.protonmail.android.mailspotlight.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.protonmail.android.mailcommon.domain.AppInformation
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailspotlight.domain.usecase.MarkFeatureSpotlightSeen
import ch.protonmail.android.mailspotlight.presentation.R
import ch.protonmail.android.mailspotlight.presentation.model.AppVersionUiModel
import ch.protonmail.android.mailspotlight.presentation.model.FeatureItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FeatureSpotlightViewModel @Inject constructor(
    appInformation: AppInformation,
    private val markFeatureSpotlightSeen: MarkFeatureSpotlightSeen
) : ViewModel() {

    private val _closeScreenEvent = MutableSharedFlow<Unit>()
    val closeScreenEvent = _closeScreenEvent.asSharedFlow()

    val appVersion: AppVersionUiModel = AppVersionUiModel(
        text = TextUiModel.TextResWithArgs(
            value = R.string.spotlight_screen_version_text,
            formatArgs = listOf(appInformation.appVersionName)
        )
    )

    val features = listOf(
        FeatureItem(
            icon = R.drawable.ic_palette,
            title = TextUiModel.TextRes(R.string.spotlight_feature_item_discreet_icon_title),
            description = TextUiModel.TextRes(R.string.spotlight_feature_item_discreet_icon_description)
        ),
        FeatureItem(
            icon = R.drawable.ic_lock,
            title = TextUiModel.TextRes(R.string.spotlight_feature_item_encryption_locks_title),
            description = TextUiModel.TextRes(R.string.spotlight_feature_item_encryption_locks_description)
        ),
        FeatureItem(
            icon = R.drawable.ic_shield_check,
            title = TextUiModel.TextRes(R.string.spotlight_feature_item_tracking_protection_title),
            description = TextUiModel.TextRes(R.string.spotlight_feature_item_tracking_protection_description)
        ),
        FeatureItem(
            icon = R.drawable.ic_code,
            title = TextUiModel.TextRes(R.string.spotlight_feature_item_headers_html_title),
            description = TextUiModel.TextRes(R.string.spotlight_feature_item_headers_html_description)
        )
    ).toImmutableList()

    fun saveScreenShown() {
        viewModelScope.launch {
            markFeatureSpotlightSeen()
            _closeScreenEvent.emit(Unit)
        }
    }
}
