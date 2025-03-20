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

buildCache {
    if (System.getenv("CI") != "true") {
        local {
            removeUnusedEntriesAfterDays = 5
        }
    }

    val remoteCacheUrl = providers.environmentVariable("GRADLE_REMOTE_CACHE_URL").orNull
        ?: providers.gradleProperty("remoteCacheUrl").orNull
        ?: ""

    if (remoteCacheUrl.isNotEmpty()) {
        remote<HttpBuildCache> {
            url = uri(remoteCacheUrl)
            isPush = providers.environmentVariable("CI_PIPELINE_SOURCE").orNull == "push"
            credentials {
                username = providers.environmentVariable("GRADLE_REMOTE_CACHE_USERNAME").orNull
                    ?: providers.gradleProperty("remoteCacheUsername").orNull
                        ?: ""
                password = providers.environmentVariable("GRADLE_REMOTE_CACHE_PASSWORD").orNull
                    ?: providers.gradleProperty("remoteCachePassword").orNull
                        ?: ""
            }
        }
    }
}
