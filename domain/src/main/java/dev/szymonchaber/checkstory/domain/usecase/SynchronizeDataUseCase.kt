package dev.szymonchaber.checkstory.domain.usecase

import dev.szymonchaber.checkstory.domain.repository.Synchronizer
import javax.inject.Inject

class SynchronizeDataUseCase @Inject constructor(
    private val synchronizer: Synchronizer
) {

    suspend fun synchronizeData() {
        synchronizer.synchronize()
    }
}
