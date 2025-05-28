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
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRemoteDataSource

/**
 * This class is only provided for bindings required by transitive dependencies brought by the legacy Core library.
 * No usage is expected.
 */
internal object FeatureFlagLocalDataSourceNoOp : FeatureFlagLocalDataSource, LegacyDeprecated {

    override suspend fun getAll(scope: Scope): List<FeatureFlag> = throwUnsupported()

    override suspend fun replaceAll(
        userId: UserId?,
        scope: Scope,
        flags: List<FeatureFlag>
    ) = throwUnsupported()

    override fun observe(userId: UserId?, scope: Scope): Flow<List<FeatureFlag>> = throwUnsupported()

    override fun observe(userId: UserId?, featureIds: Set<FeatureId>) = throwUnsupported()

    override suspend fun upsert(flags: List<FeatureFlag>) = throwUnsupported()

    override suspend fun updateValue(
        userId: UserId?,
        featureId: FeatureId,
        value: Boolean
    ) = throwUnsupported()
}

/**
 * This class is only provided for bindings required by transitive dependencies brought by the legacy Core library.
 * No usage is expected.
 */
internal object FeatureFlagRemoteDataSourceNoOp : FeatureFlagRemoteDataSource, LegacyDeprecated {

    override suspend fun getAll(userId: UserId?): List<FeatureFlag> = throwUnsupported()

    override suspend fun get(userId: UserId?, ids: Set<FeatureId>): List<FeatureFlag> = throwUnsupported()

    override suspend fun update(
        userId: UserId?,
        featureId: FeatureId,
        enabled: Boolean
    ) = throwUnsupported()
}
