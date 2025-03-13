/*
 * Copyright (C) 2025 Proton AG
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

package me.proton.android.core.payment.google.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.core.payment.domain.PaymentManager
import me.proton.android.core.payment.domain.PurchaseListener
import me.proton.android.core.payment.google.data.PaymentManagerImpl
import me.proton.android.core.payment.google.data.PurchaseStoreListener
import me.proton.android.core.payment.google.presentation.PurchaseOrchestratorImpl
import me.proton.android.core.payment.presentation.PurchaseOrchestrator

@InstallIn(SingletonComponent::class)
@Module
object CorePaymentGoogleModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface BindsModule {

        @Binds
        fun bindPaymentManager(impl: PaymentManagerImpl): PaymentManager

        @Binds
        fun bindPurchaseOrchestrator(impl: PurchaseOrchestratorImpl): PurchaseOrchestrator

        @Binds
        fun providePurchaseStoreListener(purchaseStoreListener: PurchaseStoreListener): PurchaseListener
    }
}
