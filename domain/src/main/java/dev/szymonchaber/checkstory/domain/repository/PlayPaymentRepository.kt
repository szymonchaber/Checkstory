package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.payment.ActiveSubscription

interface PlayPaymentRepository {

    suspend fun deviceHasActiveSubscription(): Boolean

    suspend fun getActiveSubscription(): ActiveSubscription?
}
