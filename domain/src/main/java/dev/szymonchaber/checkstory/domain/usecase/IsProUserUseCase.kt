package dev.szymonchaber.checkstory.domain.usecase

import kotlinx.coroutines.flow.Flow

interface IsProUserUseCase {

    val isProUserFlow: Flow<Boolean>

    suspend fun isProUser(): Boolean
}