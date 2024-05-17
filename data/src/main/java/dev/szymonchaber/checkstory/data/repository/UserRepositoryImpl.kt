package dev.szymonchaber.checkstory.data.repository

import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class UserRepositoryImpl @Inject constructor(
    private val userDataStore: UserDataStore
) : UserRepository {

    override suspend fun storeCurrentUser(user: User.LoggedIn) {
        userDataStore.storeCurrentUser(user)
    }

    override suspend fun removeCurrentUser() {
        userDataStore.removeCurrentUser()
    }

    override suspend fun getCurrentUser(): User {
        return userDataStore.getCurrentUser()
    }

    override fun getCurrentUserFlow(): Flow<User> {
        return userDataStore.getCurrentUserFlow()
    }
}
