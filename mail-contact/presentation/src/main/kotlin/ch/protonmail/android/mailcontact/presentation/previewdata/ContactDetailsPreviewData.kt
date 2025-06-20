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

package ch.protonmail.android.mailcontact.presentation.previewdata

import androidx.compose.ui.graphics.Color
import ch.protonmail.android.mailcommon.presentation.model.TextUiModel
import ch.protonmail.android.mailcontact.presentation.R
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.AvatarUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsItemGroupUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsItemType
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsItemUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsState
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.ContactDetailsUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.HeaderUiModel
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.QuickActionType
import ch.protonmail.android.mailcontact.presentation.contactdetails.model.QuickActionUiModel

object ContactDetailsPreviewData {

    val contactDetailsState = ContactDetailsState.Data(
        uiModel = ContactDetailsUiModel(
            avatarUiModel = AvatarUiModel.Initials(
                value = "P",
                color = Color.Blue
            ),
            headerUiModel = HeaderUiModel(
                displayName = "Proton Mail",
                displayEmailAddress = "pm@pm.me"
            ),
            quickActionUiModels = listOf(
                QuickActionUiModel(
                    quickActionType = QuickActionType.Message,
                    icon = R.drawable.ic_proton_pen_square,
                    label = R.string.contact_details_quick_action_message,
                    isEnabled = true
                ),
                QuickActionUiModel(
                    quickActionType = QuickActionType.Call,
                    icon = R.drawable.ic_proton_phone,
                    label = R.string.contact_details_quick_action_call,
                    isEnabled = false
                ),
                QuickActionUiModel(
                    quickActionType = QuickActionType.Share,
                    icon = R.drawable.ic_proton_arrow_up_from_square,
                    label = R.string.contact_details_quick_action_share,
                    isEnabled = true
                )
            ),
            contactDetailsItemGroupUiModels = listOf(
                ContactDetailsItemGroupUiModel(
                    contactDetailsItemUiModels = listOf(
                        ContactDetailsItemUiModel(
                            contactDetailsItemType = ContactDetailsItemType.Email,
                            label = TextUiModel.Text("Work"),
                            value = TextUiModel.Text("pm@pm.me")
                        ),
                        ContactDetailsItemUiModel(
                            contactDetailsItemType = ContactDetailsItemType.Email,
                            label = TextUiModel.Text("Home"),
                            value = TextUiModel.Text("proton@pm.me")
                        )
                    )
                ),
                ContactDetailsItemGroupUiModel(
                    contactDetailsItemUiModels = listOf(
                        ContactDetailsItemUiModel(
                            contactDetailsItemType = ContactDetailsItemType.Phone,
                            label = TextUiModel.Text("Home"),
                            value = TextUiModel.Text("+370(637) 98 998")
                        )
                    )
                ),
                ContactDetailsItemGroupUiModel(
                    contactDetailsItemUiModels = listOf(
                        ContactDetailsItemUiModel(
                            contactDetailsItemType = ContactDetailsItemType.Other,
                            label = TextUiModel.Text("Address"),
                            value = TextUiModel.Text("Lettensteg 10, 8037 Zurich")
                        )
                    )
                ),
                ContactDetailsItemGroupUiModel(
                    contactDetailsItemUiModels = listOf(
                        ContactDetailsItemUiModel(
                            contactDetailsItemType = ContactDetailsItemType.Other,
                            label = TextUiModel.Text("Birthday"),
                            value = TextUiModel.Text("Dec 09, 2006")
                        )
                    )
                ),
                ContactDetailsItemGroupUiModel(
                    contactDetailsItemUiModels = listOf(
                        ContactDetailsItemUiModel(
                            contactDetailsItemType = ContactDetailsItemType.Other,
                            label = TextUiModel.Text("Note"),
                            value = TextUiModel.Text("This is a note.")
                        )
                    )
                )
            )
        )
    )
}
