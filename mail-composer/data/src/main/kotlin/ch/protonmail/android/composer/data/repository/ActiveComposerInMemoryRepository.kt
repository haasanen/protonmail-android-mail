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

package ch.protonmail.android.composer.data.repository

import java.util.Collections
import androidx.annotation.VisibleForTesting
import ch.protonmail.android.mailcomposer.domain.repository.ActiveComposerRepository
import timber.log.Timber
import javax.inject.Inject

class ActiveComposerInMemoryRepository @Inject constructor() : ActiveComposerRepository {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val instances: MutableList<String> = Collections.synchronizedList(mutableListOf<String>())

    private var unregisterObserver: ((String) -> Unit)? = null

    override fun registerInstance(id: String) {
        Timber.tag("ActiveComposerRepo").d("registering instance $id to: $instances")
        instances.add(id)
    }

    override fun unregisterInstance(id: String) {
        Timber.tag("ActiveComposerRepo").d("unregistering instance $id from: $instances")
        instances.remove(id)
        unregisterObserver?.invoke(id)
    }

    override fun getLatestActiveInstance(): String? = instances.getOrNull(instances.lastIndex)

    override fun setUnregisterCallback(callback: (String) -> Unit) {
        Timber.tag("ActiveComposerRepo").d("Registering 'unregister observer'")
        unregisterObserver = callback
    }

}
