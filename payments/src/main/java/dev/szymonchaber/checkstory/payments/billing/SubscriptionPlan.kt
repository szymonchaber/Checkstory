package dev.szymonchaber.checkstory.payments.billing

import com.android.billingclient.api.ProductDetails

internal data class SubscriptionPlan(
    val productDetails: ProductDetails,
    val offerToken: String,
    val planDuration: PlanDuration,
    val price: String
)
