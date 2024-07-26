/*
 * Copyright (C) 2024 Proton AG
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

import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.plugin.use.PluginDependencySpec

val PluginDependenciesSpecScope.coreAndroidApp: PluginDependencySpec
    get() = id("me.proton.core.android.app")

val PluginDependenciesSpecScope.coreAndroidLibrary: PluginDependencySpec
    get() = id("me.proton.core.android.library")

val PluginDependenciesSpecScope.coreAndroidTest: PluginDependencySpec
    get() = id("me.proton.core.android.test")

val PluginDependenciesSpecScope.coreAndroidUiLibrary: PluginDependencySpec
    get() = id("me.proton.core.android.library.ui")

val PluginDependenciesSpecScope.coreAndroidComposeUiLibrary: PluginDependencySpec
    get() = id("me.proton.core.android.library.ui.compose")

val PluginDependenciesSpecScope.coreDagger: PluginDependencySpec
    get() = id("me.proton.core.dagger")

val PluginDependenciesSpecScope.coreKotlinLibrary: PluginDependencySpec
    get() = id("me.proton.core.kotlin.library")
