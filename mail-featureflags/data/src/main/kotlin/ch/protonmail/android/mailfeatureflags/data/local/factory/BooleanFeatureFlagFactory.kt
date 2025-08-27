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

package ch.protonmail.android.mailfeatureflags.data.local.factory

import ch.protonmail.android.mailfeatureflags.domain.FeatureFlagResolver
import ch.protonmail.android.mailfeatureflags.domain.model.FeatureFlag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooleanFeatureFlagFactory @Inject constructor(
    private val resolver: FeatureFlagResolver
) {

    @Suppress("ExpressionBodySyntax")
    fun create(key: String, defaultValue: Boolean): FeatureFlag<Boolean> {
        return FeatureFlag(key, defaultValue, resolver)
    }
}
