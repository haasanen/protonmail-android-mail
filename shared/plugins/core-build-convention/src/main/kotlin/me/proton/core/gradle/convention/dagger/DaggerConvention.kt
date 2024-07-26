/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.gradle.convention.dagger

import me.proton.core.gradle.convention.BuildConvention
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

internal class DaggerConvention : BuildConvention<DaggerConventionSettings> {
    override fun apply(target: Project, settings: DaggerConventionSettings) {
        target.dependencies {
            add("implementation", target.libs.findLibrary("hilt-android").get().toString())
            add("kapt", target.libs.findPlugin("hilt-compiler").get())
        }

        target.extensions.getByType(KaptExtension::class).apply {
            correctErrorTypes = true
        }

        target.afterEvaluate {
            if (settings.workManagerHiltIntegration) {
                addWorkManagerHiltDependencies()
            }
        }
    }

    private fun Project.addWorkManagerHiltDependencies() {
        dependencies {
            add("implementation", libs.findLibrary("androidx-hilt-work").get().toString())
            add("kapt", libs.findLibrary("androidx-hilt-compiler").get().toString())
        }
    }
}
