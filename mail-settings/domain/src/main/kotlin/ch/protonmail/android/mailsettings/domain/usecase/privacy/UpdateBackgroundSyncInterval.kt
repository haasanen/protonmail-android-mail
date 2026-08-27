/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package ch.protonmail.android.mailsettings.domain.usecase.privacy

import ch.protonmail.android.mailsettings.domain.model.BackgroundSyncInterval
import ch.protonmail.android.mailsettings.domain.repository.BackgroundSyncIntervalRepository
import javax.inject.Inject

class UpdateBackgroundSyncInterval @Inject constructor(
    private val backgroundSyncIntervalRepository: BackgroundSyncIntervalRepository
) {

    suspend operator fun invoke(interval: BackgroundSyncInterval) =
        backgroundSyncIntervalRepository.update(interval)
}
