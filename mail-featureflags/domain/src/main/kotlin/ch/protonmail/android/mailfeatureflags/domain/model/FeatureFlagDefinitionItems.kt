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

package ch.protonmail.android.mailfeatureflags.domain.model

data object DebugInspectDbEnabled : FeatureFlagDefinition(
    key = "debug_observe_db_enabled",
    name = "Attach to DB for debug inspection",
    category = FeatureFlagCategory.Global,
    description = "(Only on debuggable builds) Enables attaching AS DB inspector (read only) to rust DB",
    defaultValue = false
)

data object LinkifyUrlEnabled : FeatureFlagDefinition(
    key = "linkify_url_enabled",
    name = "Linkify Urls in Detail",
    category = FeatureFlagCategory.Details,
    description = "Makes urls clickable in Message Detail",
    defaultValue = true
)

data object UpsellingEnabled : FeatureFlagDefinition(
    key = "MailAndroidV7Upselling",
    name = "Enable Feature Upsell",
    category = FeatureFlagCategory.Global,
    description = "Makes the upsell flow available for all supported entry points",
    defaultValue = true
)

data object MessageExpirationEnabled : FeatureFlagDefinition(
    key = "message_expiration_enabled",
    name = "Message Expiration",
    category = FeatureFlagCategory.Composer,
    description = "Allow to set message expiration in composer",
    defaultValue = true
)

data object MessageDetailEnabled : FeatureFlagDefinition(
    key = "MailAndroidV7MessageDetail",
    name = "View Single Message Detail",
    category = FeatureFlagCategory.Details,
    description = "Allow to view a single message in the detail screen",
    defaultValue = true
)

