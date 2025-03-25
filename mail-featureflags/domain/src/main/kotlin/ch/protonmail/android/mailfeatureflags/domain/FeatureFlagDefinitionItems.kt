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

package ch.protonmail.android.mailfeatureflags.domain

import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagCategory
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlagDefinition

data object NotificationsEnabledDefinition : FeatureFlagDefinition(
    key = "notifications_enabled",
    name = "Push Notifications",
    category = FeatureFlagCategory.System,
    description = "Enables the push notifications feature.",
    defaultValue = true
)

data object ComposerEnabledDefinition : FeatureFlagDefinition(
    key = "composer_enabled",
    name = "Navigation",
    category = FeatureFlagCategory.Composer,
    description = "Grants access to Composer.",
    defaultValue = false
)
