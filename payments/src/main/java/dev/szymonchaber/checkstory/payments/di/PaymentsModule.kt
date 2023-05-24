package dev.szymonchaber.checkstory.payments.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.payments.GetPaymentPlansUseCase
import dev.szymonchaber.checkstory.payments.PurchaseSubscriptionUseCase
import dev.szymonchaber.checkstory.payments.PurchaseSubscriptionUseCaseImpl

@Module
@InstallIn(SingletonComponent::class)
interface PaymentsModule {

    @Binds
    fun bindPurchaseSubscriptionUseCase(
        purchaseSubscriptionUseCaseImpl: PurchaseSubscriptionUseCaseImpl
    ): PurchaseSubscriptionUseCase

    @Binds
    fun bindGetPaymentPlansUseCase(
        purchaseSubscriptionUseCaseImpl: PurchaseSubscriptionUseCaseImpl
    ): GetPaymentPlansUseCase
}
