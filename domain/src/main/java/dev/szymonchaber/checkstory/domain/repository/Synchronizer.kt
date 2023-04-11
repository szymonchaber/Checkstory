package dev.szymonchaber.checkstory.domain.repository

interface Synchronizer {

    suspend fun synchronize()
}
