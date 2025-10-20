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

package ch.protonmail.android.mail.plugin

import java.util.Properties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

class ApplicationConfigPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create("AppConfiguration", ConfigExtension::class.java, target)
    }
}

abstract class ConfigExtension(project: Project) {

    abstract val applicationId: Property<String>
    abstract val namespace: Property<String>
    abstract val compileSdk: Property<Int>
    abstract val minSdk: Property<Int>
    abstract val targetSdk: Property<Int>
    abstract val ndkVersion: Property<String>
    abstract val testInstrumentationRunner: Property<String>
    abstract val versionCode: Property<Int>
    abstract val versionName: Property<String>

    init {
        val gradleProperties = project.rootProject.file("app-configuration.properties")
        val properties = Properties()
        if (gradleProperties.exists()) {
            gradleProperties.inputStream().use { properties.load(it) }
        }

        applicationId.convention(properties.getProperty("applicationId"))
        compileSdk.convention(properties.getProperty("compileSdk").toInt())
        minSdk.convention(properties.getProperty("minSdk").toInt())
        targetSdk.convention(properties.getProperty("targetSdk").toInt())
        ndkVersion.convention(properties.getProperty("ndkVersion"))
        testInstrumentationRunner.convention(properties.getProperty("testInstrumentationRunner"))
        versionCode.convention(properties.getProperty("versionCode").toInt())
        versionName.convention(properties.getProperty("versionName"))
    }
}
