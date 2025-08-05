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

package ch.protonmail.android.composer.data.local

import androidx.annotation.VisibleForTesting
import ch.protonmail.android.composer.data.wrapper.DraftWrapper
import ch.protonmail.android.mailcomposer.domain.repository.ActiveComposerRepository
import timber.log.Timber
import javax.inject.Inject

class DraftCache @Inject constructor(
    private val activeComposerRepository: ActiveComposerRepository
) {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val draftByComposerInstance = mutableMapOf<String, DraftWrapper>()

    init {
        activeComposerRepository.setUnregisterCallback { composerInstance ->
            Timber.tag("DraftCache").d("Composer instance $composerInstance unregistered")
            remove(composerInstance)
        }
    }

    fun add(draft: DraftWrapper) {
        Timber.tag("DraftCache").d("Adding draft wrapper ${draft.hashCode()} to cache")
        val key = activeComposerRepository.getLatestActiveInstance()
            ?: throw IllegalStateException("Caching draft without an Active Composer instance")

        draftByComposerInstance[key] = draft

    }

    fun get(): DraftWrapper {
        val key = activeComposerRepository.getLatestActiveInstance()
            ?: throw IllegalStateException("Caching draft without an Active Composer instance")

        val draftWrapper = draftByComposerInstance[key]
            ?: throw IllegalStateException("Attempting to access draft operations while no draft object exists")

        return draftWrapper
    }

    private fun remove(key: String) {
        Timber.tag("DraftCache").d("Removing Cached Draft wrapper with composer instance $key")
        draftByComposerInstance.remove(key)
    }

}
