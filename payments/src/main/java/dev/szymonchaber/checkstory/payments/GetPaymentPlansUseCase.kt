package dev.szymonchaber.checkstory.payments

import arrow.core.Either
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPaymentPlansUseCase @Inject constructor(
    private val billingInteractor: BillingInteractor
) {

    fun getPaymentPlans(): Flow<Either<BillingError, SubscriptionPlans>?> {
        return billingInteractor.subscriptionPlans
    }
}
