package dev.szymonchaber.checkstory.api.payment.model

import kotlinx.serialization.Serializable

@Serializable
internal data class AssignPurchaseTokenPayload(val token: String)
