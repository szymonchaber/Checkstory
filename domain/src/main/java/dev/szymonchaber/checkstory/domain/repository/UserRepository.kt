package dev.szymonchaber.checkstory.domain.repository

import dev.szymonchaber.checkstory.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun storeCurrentUser(user: User.LoggedIn)

    suspend fun removeCurrentUser()

    suspend fun getCurrentUser(): User

    fun getCurrentUserFlow(): Flow<User>
}
