package dev.szymonchaber.checkstory.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface SynchronizationStatusRepository {

    val lastSuccessfulSynchronizationDate: Flow<Instant?>
    val lastFailedSynchronizationDate: Flow<Instant?>

    suspend fun markSuccess()

    suspend fun markFailure()

    suspend fun clear()
}
