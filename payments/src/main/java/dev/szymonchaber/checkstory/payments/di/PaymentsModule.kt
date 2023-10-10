package dev.szymonchaber.checkstory.payments.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.domain.repository.PlayPaymentRepository
import dev.szymonchaber.checkstory.payments.billing.BillingInteractorImpl

@Module
@InstallIn(SingletonComponent::class)
internal interface PaymentsModule {

    @Binds
    fun bindPlayPaymentRepository(
        paymentInteractor: BillingInteractorImpl
    ): PlayPaymentRepository
}
