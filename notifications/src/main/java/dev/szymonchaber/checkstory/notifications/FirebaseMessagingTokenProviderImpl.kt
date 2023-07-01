package dev.szymonchaber.checkstory.notifications

import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dev.szymonchaber.checkstory.domain.interactor.FirebaseMessagingTokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMessagingTokenProviderImpl @Inject constructor() : FirebaseMessagingTokenProvider {

    override suspend fun getFirebaseToken(): String {
        return withContext(Dispatchers.IO) {
            Firebase.messaging.token.await()
        }
    }
}
