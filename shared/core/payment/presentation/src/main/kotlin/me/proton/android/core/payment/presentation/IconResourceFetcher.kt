/*
 * Copyright (C) 2025 Proton AG
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.android.core.payment.presentation

import android.content.Context
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.disk.DiskCache
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.key.Keyer
import coil.request.Options
import me.proton.android.core.payment.domain.IconResourceManager
import me.proton.android.core.payment.presentation.extension.use
import okio.buffer
import okio.source

data class IconResource(
    val name: String
)

class IconResourceKeyer : Keyer<IconResource> {

    override fun key(data: IconResource, options: Options): String = data.name
}

@OptIn(ExperimentalCoilApi::class)
class IconResourceFetcher(
    private val context: Context,
    private val iconResource: IconResource,
    private val iconResourceManager: IconResourceManager
) : Fetcher {

    private val diskCache = DiskCache.Builder()
        .directory(context.cacheDir.resolve("icon-cache"))
        .maxSizePercent(0.02)
        .build()

    private fun readCache(snapshot: DiskCache.Snapshot): SourceResult {
        val inputStream = snapshot.data.toFile().inputStream()
        return SourceResult(
            source = ImageSource(source = inputStream.source().buffer(), context = context),
            dataSource = DataSource.DISK,
            mimeType = null
        )
    }

    private suspend fun readResource(editor: DiskCache.Editor): SourceResult {
        val array = requireNotNull(iconResourceManager.getBitmap(iconResource.name))
        editor.data.toFile().writeBytes(array)
        return SourceResult(
            source = ImageSource(source = array.inputStream().source().buffer(), context = context),
            dataSource = DataSource.NETWORK,
            mimeType = null
        )
    }

    override suspend fun fetch(): FetchResult? {
        return diskCache.openSnapshot(iconResource.name)?.use { readCache(it) }
            ?: diskCache.openEditor(iconResource.name)?.use { readResource(it) }
    }

    class Factory(
        private val context: Context,
        private val manager: IconResourceManager
    ) : Fetcher.Factory<IconResource> {

        override fun create(
            data: IconResource,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher = IconResourceFetcher(context, data, manager)
    }
}
