package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserUseCase @Inject constructor(private val isProUserUseCase: IsProUserUseCase) {

    fun getUser(): Flow<User> {
        return isProUserUseCase.isProUserFlow
            .map { isProUser ->
                if (isProUser) {
                    User.LoggedIn(Tier.PAID)
                } else {
                    User.LoggedIn(Tier.FREE)
                }
            }.flowOn(Dispatchers.IO)
    }
}
