package dev.szymonchaber.checkstory.payments

import arrow.core.Either
import javax.inject.Inject

class GetPaymentPlansUseCase @Inject constructor(
    private val paymentInteractor: PaymentInteractor
) {

    suspend fun getPaymentPlans(): Either<BillingError, SubscriptionPlans> {
        return paymentInteractor.getPaymentPlans()
    }
}
