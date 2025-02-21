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
    kotlin("kapt")
    kotlin("android")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
    id("dagger.hilt.android.plugin")
    id("app.cash.paparazzi")
    id("kotlin-parcelize")
    id("app-config-plugin")
}

android {
    namespace = "me.proton.android.core.auth.presentation"

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
    kapt(libs.bundles.app.annotationProcessors)

    compileOnly(libs.proton.rust.core)

    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.proton.core.challenge)
    implementation(libs.proton.core.domain)
    implementation(libs.proton.core.presentation)
    implementation(libs.proton.core.presentationCompose)
    implementation(libs.proton.core.utilKotlin)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.lottie.compose)

    implementation(project(":design-system"))
    implementation(project(":mail-session:domain"))
    implementation(project(":mail-settings:domain"))

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.bundles.test)
    testImplementation(libs.proton.rust.core)
    testImplementation(kotlin("test"))
}
