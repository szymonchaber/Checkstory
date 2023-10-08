package dev.szymonchaber.checkstory.payments.billing

internal data class SubscriptionPlans(
    val monthly: SubscriptionPlan,
    val quarterly: SubscriptionPlan,
    val yearly: SubscriptionPlan
)
