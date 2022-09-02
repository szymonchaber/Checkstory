package dev.szymonchaber.checkstory.payments

import arrow.core.Either

interface GetPaymentPlansUseCase {

    suspend fun getPaymentPlans(): Either<BillingError, SubscriptionPlans>
}
