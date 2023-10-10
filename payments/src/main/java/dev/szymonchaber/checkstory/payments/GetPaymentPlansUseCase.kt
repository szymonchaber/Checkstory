package dev.szymonchaber.checkstory.payments

import arrow.core.Either
import dev.szymonchaber.checkstory.payments.billing.BillingError
import dev.szymonchaber.checkstory.payments.billing.BillingInteractorImpl
import dev.szymonchaber.checkstory.payments.billing.SubscriptionPlans
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class GetPaymentPlansUseCase @Inject constructor(
    private val billingInteractor: BillingInteractorImpl
) {

    fun getPaymentPlans(): Flow<Either<BillingError, SubscriptionPlans>?> {
        return billingInteractor.subscriptionPlans
    }
}
