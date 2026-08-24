/*
 * Copyright (c) 2026 haasanen
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.proton.android.core.auth.fido.nfc

import android.app.Activity
import androidx.activity.result.ActivityResultCaller
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey

/**
 * Binds the native (NFC, GMS-free) implementation of the FIDO2 two-factor use
 * case. Mirrors CoreAuthFidoDataPlayModule; the auth-fido-dagger module exposes
 * the binding as `Optional<PerformTwoFaWithSecurityKey<ActivityResultCaller, Activity>>`
 * via @BindsOptionalOf.
 */
@Module
@InstallIn(SingletonComponent::class)
interface CoreAuthFidoNativeModule {

    @Binds
    fun bindPerformTwoFaWithSecurityKey(
        impl: NativeSecurityKeyUseCase,
    ): PerformTwoFaWithSecurityKey<ActivityResultCaller, Activity>
}
