package dev.szymonchaber.checkstory.domain.interactor

interface AuthCache {

    suspend fun clearAuthToken()
}
