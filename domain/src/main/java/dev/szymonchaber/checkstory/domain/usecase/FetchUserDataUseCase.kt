package dev.szymonchaber.checkstory.domain.usecase

import javax.inject.Inject

class FetchUserDataUseCase @Inject constructor(
    private val loginUseCase: LoginUseCase
) {

    suspend fun fetchUserData() {
        loginUseCase.login()
    }
}
