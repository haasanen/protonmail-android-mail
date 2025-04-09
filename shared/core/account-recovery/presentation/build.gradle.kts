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

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dagger.hilt.android.plugin")
    id("app.cash.paparazzi")
    id("app-config-plugin")
    id("kotlin-parcelize")
}

android {
    namespace = "me.proton.android.core.accountrecovery.presentation"

    compileSdk = AppConfiguration.compileSdk.get()

    defaultConfig {
        minSdk = AppConfiguration.minSdk.get()
        lint.targetSdk = AppConfiguration.targetSdk.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    compileOnly(libs.proton.rust.core)

    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.dagger.hilt.android)
    implementation(libs.proton.core.presentation)
    implementation(libs.proton.core.presentationCompose)
    implementation(libs.proton.core.utilKotlin)
    implementation(libs.proton.core.domain)
    implementation(libs.proton.core.network.domain)

    implementation(project(":shared:core:account:domain"))
    implementation(project(":shared:core:account-manager:presentation"))
    implementation(project(":design-system"))
    implementation(project(":presentation-compose"))

    kapt(libs.bundles.app.annotationProcessors)

    testImplementation(kotlin("test"))
    testImplementation(libs.bundles.test)
    testImplementation(libs.proton.rust.core)
}
