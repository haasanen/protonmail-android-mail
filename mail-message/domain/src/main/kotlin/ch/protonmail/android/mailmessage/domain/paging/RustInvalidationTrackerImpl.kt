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

package ch.protonmail.android.mailmessage.domain.paging

import java.lang.ref.WeakReference
import javax.inject.Inject

class RustInvalidationTrackerImpl @Inject constructor() : RustInvalidationTracker {

    private var observerInstance: WeakReference<RustInvalidationObserver>? = null

    override fun addObserver(observer: RustInvalidationObserver) {
        observerInstance = WeakReference(observer)
    }

    override fun notifyInvalidation(invalidatedDataSources: Set<RustDataSourceId>) {
        observerInstance?.get()?.let { observer ->
            val observedDataSourceChanged = observer.dataSources.any { it in invalidatedDataSources }
            if (observedDataSourceChanged) {
                observer.onInvalidated(invalidatedDataSources)
            }
        }
    }
}
