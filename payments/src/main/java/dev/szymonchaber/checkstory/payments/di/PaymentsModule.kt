package dev.szymonchaber.checkstory.payments.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.payments.PaymentInteractor
import dev.szymonchaber.checkstory.payments.PaymentInteractorImpl

@Module
@InstallIn(SingletonComponent::class)
interface PaymentsModule {

    @Binds
    fun bindPaymentInteractor(
        paymentInteractor: PaymentInteractorImpl
    ): PaymentInteractor
}
