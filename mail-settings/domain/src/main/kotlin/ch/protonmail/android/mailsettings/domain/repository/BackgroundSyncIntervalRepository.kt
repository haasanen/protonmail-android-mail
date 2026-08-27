/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package ch.protonmail.android.mailsettings.domain.repository

import arrow.core.Either
import ch.protonmail.android.mailcommon.domain.model.PreferencesError
import ch.protonmail.android.mailsettings.domain.model.BackgroundSyncInterval
import kotlinx.coroutines.flow.Flow

interface BackgroundSyncIntervalRepository {

    fun observe(): Flow<Either<PreferencesError, BackgroundSyncInterval>>

    suspend fun update(interval: BackgroundSyncInterval): Either<PreferencesError, Unit>
}
