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

package ch.protonmail.android.mailpagination.presentation.paging

import androidx.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class RustPagingSource<Key : Any, Value : Any> : PagingSource<Key, Value>() {

    override suspend fun load(params: LoadParams<Key>): LoadResult<Key, Value> {
        return withContext(Dispatchers.IO) {

            val loadResult = loadPage(params)
            @Suppress("UNCHECKED_CAST")
            if (invalid) INVALID as LoadResult.Invalid<Key, Value> else loadResult
        }
    }

    abstract suspend fun loadPage(params: LoadParams<Key>): LoadResult<Key, Value>

    companion object {

        // Keep only 1 instance for all Invalid results.
        val INVALID = LoadResult.Invalid<Any, Any>()
    }
}
