package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.model.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetUserUseCase @Inject constructor(private val isProUserUseCase: IsProUserUseCase) {

    fun getUser(): Flow<User> {
        return flowOf(User.LoggedIn(UserId("userId"), Tier.PAID))
    }
}
