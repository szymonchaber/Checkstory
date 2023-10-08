package dev.szymonchaber.checkstory.payments.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import dev.szymonchaber.checkstory.payments.billing.BillingInteractor
import dev.szymonchaber.checkstory.payments.billing.BillingInteractorImpl

@Module
@InstallIn(SingletonComponent::class)
interface PaymentsModule {

    @Binds
    fun bindPaymentInteractor(
        paymentInteractor: BillingInteractorImpl
    ): BillingInteractor

    @Binds
    fun bindPlayPaymentRepository(
        paymentInteractor: BillingInteractorImpl
    ): PlayPaymentRepository
}
