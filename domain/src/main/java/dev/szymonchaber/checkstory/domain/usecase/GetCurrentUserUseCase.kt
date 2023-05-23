package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    fun getCurrentUserFlow(): Flow<User> {
        return userRepository.getCurrentUserFlow()
    }

    suspend fun getCurrentUser(): User {
        return userRepository.getCurrentUser()
    }
}
