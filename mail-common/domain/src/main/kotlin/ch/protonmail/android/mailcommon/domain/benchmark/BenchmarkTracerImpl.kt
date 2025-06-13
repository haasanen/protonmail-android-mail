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

package ch.protonmail.android.mailcommon.domain.benchmark

import android.os.Trace
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BenchmarkTracerImpl @Inject constructor(private val benchmarkEnabled: Boolean) :
    BenchmarkTracer {

    override fun begin(name: String) {
        if (benchmarkEnabled) {
            Trace.beginSection(name)
        }
    }

    override fun end() {
        if (benchmarkEnabled) {
            Trace.endSection()
        }
    }

    override fun beginAsync(name: String) {
        if (benchmarkEnabled) {
            Trace.beginAsyncSection(name, 0)
        }
    }

    override fun endAsync(name: String) {
        if (benchmarkEnabled) {
            Trace.endAsyncSection(name, 0)
        }
    }
}
