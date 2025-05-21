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

data object UseV6CssInjectionDefinition : FeatureFlagDefinition(
    key = "v6_css_injection",
    name = "Enforce V6 CSS injection",
    category = FeatureFlagCategory.Details,
    description = "Applies V6 CSS injection on the Rust-provided message body.",
    defaultValue = true
)

data object DebugInspectDbEnabled : FeatureFlagDefinition(
    key = "debug_observe_db_enabled",
    name = "Attach to DB for debug inspection",
    category = FeatureFlagCategory.System,
    description = "(Only on debuggable builds) Enables attaching AS DB inspector (read only) to rust DB",
    defaultValue = true
)

data object ReportAProblemEnabled : FeatureFlagDefinition(
    key = "report_a_problem",
    name = "Report A Problem",
    category = FeatureFlagCategory.Settings,
    description = "Grants access to 'Report a Problem' feature.",
    defaultValue = false
)

data object InlineImagesComposerEnabled : FeatureFlagDefinition(
    key = "inline_images_composer",
    name = "Inline images composer",
    category = FeatureFlagCategory.Composer,
    description = "Allow to add attachments as inline images in composer",
    defaultValue = false
)
