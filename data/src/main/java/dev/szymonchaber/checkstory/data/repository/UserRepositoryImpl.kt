package dev.szymonchaber.checkstory.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

internal class UserRepositoryImpl @Inject constructor(
    private val userDataStore: UserDataStore
) : UserRepository {

    private val auth by lazy { Firebase.auth }

    override fun isFirebaseLoggedInFlow(): Flow<Boolean> {
        return callbackFlow {
            val listener: (FirebaseAuth) -> Unit = {
                trySend(it.currentUser != null)
            }
            auth.addAuthStateListener(listener)
            awaitClose {
                auth.removeAuthStateListener(listener)
            }
        }
    }

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
