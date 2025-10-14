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

package ch.protonmail.android.legacymigration.di.stubs

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository

/**
 * This class is only provided for bindings required by transitive dependencies brought by the legacy Core library.
 * No usage is expected.
 */
internal object FeatureFlagRepositoryNoOp : FeatureFlagRepository, LegacyDeprecated {

    @Suppress("OVERRIDE_DEPRECATION")
    override suspend fun awaitNotEmptyScope(userId: UserId?, scope: Scope) = throwUnsupported()

    override fun getValue(userId: UserId?, featureId: FeatureId): Boolean? = throwUnsupported()

    override suspend fun getAll(userId: UserId?): List<FeatureFlag> = throwUnsupported()

    override fun refreshAllOneTime(userId: UserId?) = throwUnsupported()

    override fun refreshAllPeriodic(userId: UserId?, immediately: Boolean) = throwUnsupported()

    override fun observe(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): Flow<FeatureFlag?> = throwUnsupported()

    override fun observe(
        userId: UserId?,
        featureIds: Set<FeatureId>,
        refresh: Boolean
    ): Flow<List<FeatureFlag>> = throwUnsupported()

    override suspend fun get(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): FeatureFlag? = throwUnsupported()

    override suspend fun get(
        userId: UserId?,
        featureIds: Set<FeatureId>,
        refresh: Boolean
    ): List<FeatureFlag> = throwUnsupported()

    override fun prefetch(userId: UserId?, featureIds: Set<FeatureId>) = throwUnsupported()

    override suspend fun update(featureFlag: FeatureFlag) = throwUnsupported()
}
