package dev.szymonchaber.checkstory.domain.repository

interface PlayPaymentRepository {

    suspend fun deviceHasActiveSubscription(): Boolean
}
