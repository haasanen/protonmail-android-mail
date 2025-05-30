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

package me.proton.android.core.auth.presentation.secondfactor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.proton.core.compose.theme.ProtonTheme
import me.proton.android.core.auth.presentation.secondfactor.fido2.Fido2InputForm
import me.proton.android.core.auth.presentation.secondfactor.otp.OneTimePasswordInputForm
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing

@Suppress("UseComposableActions")
@Composable
fun SecondFactorTabContent(
    selectedTab: SecondFactorTab,
    selectedTabIndex: Int,
    tabs: List<SecondFactorTab>,
    onTabSelected: (Int) -> Unit,
    onClose: () -> Unit,
    onError: (String?) -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (tabs.shouldShowTabs()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DefaultSpacing),
                backgroundColor = Color.Transparent,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = ProtonTheme.colors.textNorm
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                text = tab.localizedLabel(),
                                color = if (selectedTabIndex == index) {
                                    ProtonTheme.colors.textNorm
                                } else {
                                    ProtonTheme.colors.textWeak
                                }
                            )
                        }
                    )
                }
            }
        }

        when (selectedTab) {
            SecondFactorTab.SecurityKey -> {
                Fido2InputForm(
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
                    onClose = onClose,
                    onSuccess = onSuccess,
                    onError = onError
                )
            }

            SecondFactorTab.Otp -> {
                OneTimePasswordInputForm(
                    onError = onError,
                    onSuccess = onSuccess,
                    onClose = onClose,
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
                )
            }
        }
    }
}
