package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.repository.UserRepository
import javax.inject.Inject

class IsProUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend fun isProUser(): Boolean {
        return userRepository.getCurrentUser().isPaidUser
    }
}
