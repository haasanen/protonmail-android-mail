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

    private val observerMap = mutableMapOf<RustInvalidationObserver, ObserverWrapper>()

    internal class WeakObserver(
        private val tracker: RustInvalidationTracker,
        delegate: RustInvalidationObserver
    ) : RustInvalidationObserver(delegate.dataSources) {

        private val delegateRef: WeakReference<RustInvalidationObserver> = WeakReference(delegate)
        override fun onInvalidated(invalidatedDataSources: Set<RustDataSourceId>) {
            val observer = delegateRef.get()
            if (observer == null) {
                tracker.removeObserver(this)
            } else {
                observer.onInvalidated(invalidatedDataSources)
            }
        }
    }

    internal class ObserverWrapper(
        private val observer: RustInvalidationObserver,
        private val dataSources: Set<RustDataSourceId>
    ) {

        /**
         * Notifies the underlying observer if any of the observed data sources are invalidated
         */
        internal fun notifyInvalidStatus(invalidatedDataSources: Set<RustDataSourceId>) {
            val invalidatedTables = buildSet {
                dataSources.forEach { dataSource ->
                    if (invalidatedDataSources.contains(dataSource)) {
                        add(dataSource)
                    }
                }
            }

            if (invalidatedTables.isNotEmpty()) {
                observer.onInvalidated(invalidatedTables)
            }
        }
    }

    override fun addObserver(observer: RustInvalidationObserver) {
        synchronized(observerMap) {
            observerMap.putIfAbsent(WeakObserver(this, observer), ObserverWrapper(observer, observer.dataSources))
        }
    }

    override fun removeObserver(observer: RustInvalidationObserver) {
        synchronized(observerMap) {
            observerMap.remove(observer)
        }
    }

    override fun notifyInvalidation(invalidatedDataSources: Set<RustDataSourceId>) {
        synchronized(observerMap) {
            observerMap.values.forEach { it.notifyInvalidStatus(invalidatedDataSources) }
        }
    }
}
