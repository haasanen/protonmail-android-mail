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
    id("dagger.hilt.android.plugin")
    id("app.cash.paparazzi")
}

android {
    namespace = "me.proton.android.core.auth.presentation"

    compileSdk = Config.compileSdk

    defaultConfig {
        minSdk = Config.minSdk
        lint.targetSdk = Config.targetSdk
        testInstrumentationRunner = Config.testInstrumentationRunner
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

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.AndroidX.composeCompiler
    }
}

dependencies {
    compileOnly("me.proton.mail.common:lib:${Versions.Proton.rustCore}")

    implementation("androidx.constraintlayout:constraintlayout-compose:${Versions.AndroidX.constraintLayoutCompose}")
    implementation("androidx.compose.ui:ui-tooling-preview-android:${Versions.AndroidX.compose}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Versions.AndroidX.compose}")

    implementation("me.proton.core:presentation:${Versions.Proton.core}")
    implementation("me.proton.core:presentation-compose:${Versions.Proton.core}")
    implementation("me.proton.core:util-kotlin:${Versions.Proton.core}")

    implementation("com.google.dagger:hilt-android:${Versions.Dagger.dagger}")
    kapt("com.google.dagger:hilt-compiler:${Versions.Dagger.dagger}")

    implementation("androidx.hilt:hilt-navigation-compose:${Versions.AndroidX.hilt}")
    kapt("androidx.hilt:hilt-compiler:${Versions.AndroidX.hilt}")

    testImplementation(kotlin("test"))
    testImplementation("me.proton.mail.common:lib:${Versions.Proton.rustCore}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.KotlinX.coroutines}")
    testImplementation("junit:junit:${Versions.Junit.junit}")
    testImplementation("io.mockk:mockk:${Versions.Mockk.mockk}")
    testImplementation("app.cash.turbine:turbine:${Versions.Cash.turbine}")
}
