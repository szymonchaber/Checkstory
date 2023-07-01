package dev.szymonchaber.checkstory.domain.interactor

interface FirebaseMessagingTokenProvider {

    suspend fun getFirebaseToken(): String
}
