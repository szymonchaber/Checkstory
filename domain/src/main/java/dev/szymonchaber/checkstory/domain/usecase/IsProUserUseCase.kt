package dev.szymonchaber.checkstory.domain.usecase

interface IsProUserUseCase {

    suspend fun isProUser(): Boolean
}