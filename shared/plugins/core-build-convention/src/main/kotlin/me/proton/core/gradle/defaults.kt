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

package me.proton.core.gradle

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

internal object AndroidDefaults {
    const val COMPILE_SDK = 34
    const val MIN_SDK = 23
    const val NDK_VERSION = "24.0.8215888"
    const val TARGET_SDK = 34
    const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
}

internal object DaggerDefaults {
    const val WORK_MANAGER_HILT_INTEGRATION = false
}

internal object JvmDefaults {
    val JVM_TARGET = JavaVersion.VERSION_17
}

internal object KotlinDefaults {
    val API_MODE = ExplicitApiMode.Strict
}
