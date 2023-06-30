package dev.szymonchaber.checkstory.api.firebase

import dev.szymonchaber.checkstory.api.di.ConfiguredHttpClient
import dev.szymonchaber.checkstory.domain.interactor.FirebaseMessagingInteractor
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject

internal class FirebaseMessagingInteractorImpl @Inject constructor(
    private val configuredHttpClient: ConfiguredHttpClient
) : FirebaseMessagingInteractor {

    override suspend fun pushFirebaseMessagingToken(token: String) {
        withContext(Dispatchers.IO) {
            configuredHttpClient.post("notification/firebase-token") {
                setBody(FirebaseMessagingTokenPayload(token))
            }
        }
    }
}

@Serializable
internal data class FirebaseMessagingTokenPayload(val token: String)
