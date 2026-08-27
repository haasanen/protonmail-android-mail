import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("app-config-plugin")
}

android {
    namespace = "me.proton.android.core.auth.fido.nfc"

    compileSdk = AppConfiguration.compileSdk.get()

    defaultConfig {
        minSdk = AppConfiguration.minSdk.get()
        lint.targetSdk = AppConfiguration.targetSdk.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("17")
            optIn.add("kotlin.ExperimentalUnsignedTypes")
        }
    }
}

dependencies {
    kapt(libs.bundles.app.annotationProcessors)

    testImplementation(libs.bundles.test)

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.cbor)

    implementation(libs.proton.core.fido.domain)
}
