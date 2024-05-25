package dev.szymonchaber.checkstory.data.repository

import com.google.firebase.auth.FirebaseAuth
import dev.szymonchaber.checkstory.domain.interactor.AuthCache
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

internal class UserRepositoryImpl @Inject constructor(
    private val userDataStore: UserDataStore,
    private val authCache: AuthCache,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    override fun isFirebaseLoggedInFlow(): Flow<Boolean> {
        return callbackFlow {
            val listener: (FirebaseAuth) -> Unit = {
                trySend(it.currentUser != null)
            }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose {
                firebaseAuth.removeAuthStateListener(listener)
            }
        }
    }

    override suspend fun storeCurrentUser(user: User.LoggedIn) {
        userDataStore.storeCurrentUser(user)
    }

    override suspend fun removeCurrentUser() {
        userDataStore.removeCurrentUser()
        firebaseAuth.signOut()
        authCache.clearAuthToken()
    }

    override suspend fun getCurrentUser(): User {
        return userDataStore.getCurrentUser()
    }

    override fun getCurrentUserFlow(): Flow<User> {
        return userDataStore.getCurrentUserFlow()
    }
}
