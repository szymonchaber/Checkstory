package dev.szymonchaber.checkstory.domain.interactor

interface FirebaseMessagingInteractor {

    suspend fun pushFirebaseMessagingToken(token: String)
}
