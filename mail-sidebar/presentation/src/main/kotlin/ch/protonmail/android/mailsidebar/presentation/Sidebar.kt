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

package ch.protonmail.android.mailsidebar.presentation

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.protonmail.android.design.compose.component.VerticalSpacer
import ch.protonmail.android.design.compose.theme.ProtonDimens
import ch.protonmail.android.design.compose.theme.ProtonTheme
import ch.protonmail.android.mailcommon.domain.AppInformation
import ch.protonmail.android.maillabel.domain.model.LabelType
import ch.protonmail.android.maillabel.presentation.MailLabelsUiModel
import ch.protonmail.android.mailsidebar.presentation.SidebarViewModel.State.Disabled
import ch.protonmail.android.mailsidebar.presentation.SidebarViewModel.State.Enabled
import ch.protonmail.android.mailsidebar.presentation.common.ProtonSidebarAppVersionItem
import ch.protonmail.android.mailsidebar.presentation.common.ProtonSidebarItem
import ch.protonmail.android.mailsidebar.presentation.common.ProtonSidebarLazy
import ch.protonmail.android.mailsidebar.presentation.common.ProtonSidebarReportBugItem
import ch.protonmail.android.mailsidebar.presentation.common.ProtonSidebarSettingsItem
import ch.protonmail.android.mailsidebar.presentation.common.ProtonSidebarShareLogs
import ch.protonmail.android.mailsidebar.presentation.common.ProtonSidebarSubscriptionItem
import ch.protonmail.android.mailsidebar.presentation.label.SidebarLabelAction
import ch.protonmail.android.mailsidebar.presentation.label.sidebarFolderItems
import ch.protonmail.android.mailsidebar.presentation.label.sidebarLabelItems
import ch.protonmail.android.mailsidebar.presentation.label.sidebarSystemLabelItems
import kotlinx.coroutines.launch

@Composable
@Suppress("ComplexMethod")
fun Sidebar(
    drawerState: DrawerState,
    navigationActions: Sidebar.NavigationActions,
    modifier: Modifier = Modifier,
    viewModel: SidebarViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val viewState = rememberSidebarState(
        drawerState = drawerState,
        appInformation = viewModel.appInformation
    )

    val isReportAProblemEnabled by viewModel.isReportAProblemEnabled.collectAsStateWithLifecycle()

    fun close() = scope.launch {
        viewState.drawerState.close()
    }

    when (val viewModelState = viewModel.state.collectAsState().value) {
        is Disabled -> Unit
        is Enabled -> {
            viewState.isSubscriptionVisible = viewModelState.canChangeSubscription
            viewState.mailLabels = viewModelState.mailLabels
            val actions = navigationActions.toSidebarActions(
                close = ::close,
                onLabelAction = { sidebarLabelAction ->
                    when (sidebarLabelAction) {
                        is SidebarLabelAction.Add -> {
                            close()
                            if (sidebarLabelAction.type == LabelType.MessageLabel) {
                                navigationActions.onLabelAdd()
                            } else if (sidebarLabelAction.type == LabelType.MessageFolder) {
                                navigationActions.onFolderAdd()
                            }
                        }

                        is SidebarLabelAction.Select -> {
                            close()
                            viewModel.submit(SidebarViewModel.Action.LabelAction(sidebarLabelAction))
                        }

                        is SidebarLabelAction.Collapse,
                        is SidebarLabelAction.Expand -> {
                            viewModel.submit(SidebarViewModel.Action.LabelAction(sidebarLabelAction))
                        }
                    }
                }
            )
            Sidebar(
                modifier = modifier,
                viewState = viewState,
                actions = actions,
                isReportAProblemEnabled = isReportAProblemEnabled
            )
        }
    }
}

@Composable
fun Sidebar(
    modifier: Modifier = Modifier,
    viewState: SidebarState,
    actions: Sidebar.Actions,
    isReportAProblemEnabled: Boolean
) {

    SidebarHeader()

    SidebarDivider()

    ProtonSidebarLazy(
        modifier = modifier.testTag(SidebarMenuTestTags.Root),
        drawerState = viewState.drawerState
    ) {
        sidebarSystemLabelItems(viewState.mailLabels.systemLabels, actions.onLabelAction)
        item { SidebarDivider() }
        sidebarFolderItems(viewState.mailLabels.folders, actions.onLabelAction)
        item { SidebarDivider() }
        sidebarLabelItems(viewState.mailLabels.labels, actions.onLabelAction)
        item { SidebarDivider() }
        item { VerticalSpacer(height = ProtonDimens.Spacing.Standard) }

        item { ProtonSidebarSubscriptionItem { actions.onSubscription() } }

        item { ProtonSidebarSettingsItem(onClick = actions.onSettings) }
        item { SidebarContactsItem(onClick = actions.onContacts) }

        if (isReportAProblemEnabled) {
            item { ProtonSidebarReportBugItem(onClick = actions.onReportBug) }
        }

        item { ProtonSidebarShareLogs(onClick = actions.onExportlogs) }
        item { VerticalSpacer(height = ProtonDimens.Spacing.ExtraLarge) }

        item { SidebarDivider() }
        item { VerticalSpacer(height = ProtonDimens.Spacing.ExtraLarge) }
        item { SidebarAppVersionItem(viewState.appInformation) }
    }
}

@Composable
private fun SidebarHeader() {
    Box(
        modifier = Modifier.padding(
            start = ProtonDimens.Spacing.ExtraLarge,
            bottom = ProtonDimens.Spacing.Small
        )
    ) {
        Image(
            painter = painterResource(R.drawable.proton_mail_logo),
            contentDescription = null
        )
    }
}

@Composable
private fun SidebarDivider() {
    HorizontalDivider(
        color = ProtonTheme.colors.sidebarSeparator
    )
}

@Composable
private fun SidebarContactsItem(onClick: () -> Unit) {
    ProtonSidebarItem(
        icon = painterResource(R.drawable.ic_proton_users),
        text = stringResource(R.string.drawer_title_contacts),
        isSelected = false,
        onClick = onClick
    )
}

@Composable
private fun SidebarAppVersionItem(appInformation: AppInformation) {
    ProtonSidebarAppVersionItem(
        name = appInformation.appName,
        version = "${appInformation.appVersionName} (${appInformation.appVersionCode})",
        sdkVersion = appInformation.rustSdkVersion
    )
}

object Sidebar {

    data class Actions(
        val onSettings: () -> Unit,
        val onLabelAction: (SidebarLabelAction) -> Unit,
        val onSubscription: () -> Unit,
        val onContacts: () -> Unit,
        val onReportBug: () -> Unit,
        val onExportlogs: () -> Unit
    ) {

        companion object {

            val Empty = Actions(
                onSettings = {},
                onLabelAction = {},
                onSubscription = {},
                onContacts = {},
                onReportBug = {},
                onExportlogs = {}
            )
        }
    }

    data class NavigationActions(
        val onSettings: () -> Unit,
        val onLabelAdd: () -> Unit,
        val onFolderAdd: () -> Unit,
        val onSubscription: () -> Unit,
        val onContacts: () -> Unit,
        val onReportBug: () -> Unit,
        val onExportLogs: () -> Unit
    ) {

        fun toSidebarActions(close: () -> Unit, onLabelAction: (SidebarLabelAction) -> Unit) = Actions(
            onSettings = {
                onSettings()
                close()
            },
            onLabelAction = { action ->
                onLabelAction(action)
                close()
            },
            onSubscription = {
                onSubscription()
                close()
            },
            onContacts = {
                onContacts()
                close()
            },
            onReportBug = {
                onReportBug()
                close()
            },
            onExportlogs = {
                onExportLogs()
                close()
            }
        )

        companion object {

            val Empty = NavigationActions(
                onSettings = {},
                onLabelAdd = {},
                onFolderAdd = {},
                onSubscription = {},
                onContacts = {},
                onReportBug = {},
                onExportLogs = {}
            )
        }
    }
}

@SuppressLint("VisibleForTests")
@Preview(
    name = "Sidebar in light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "Sidebar in dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun PreviewSidebar() {
    ProtonTheme {
        Sidebar(
            viewState = SidebarState(
                mailLabels = MailLabelsUiModel.PreviewForTesting,
                isSubscriptionVisible = true
            ),
            actions = Sidebar.Actions.Empty,
            isReportAProblemEnabled = false
        )
    }
}

object SidebarMenuTestTags {

    const val Root = "SidebarMenu"
}
