package dev.szymonchaber.checkstory.data.synchronization

import dev.szymonchaber.checkstory.data.repository.LocalChecklistTemplateRepository
import dev.szymonchaber.checkstory.data.repository.RemoteChecklistTemplateRepository
import dev.szymonchaber.checkstory.domain.usecase.Synchronizer
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

class SynchronizerImpl @Inject internal constructor(
    private val checklistTemplateRepository: LocalChecklistTemplateRepository,
    private val remoteChecklistTemplateRepository: RemoteChecklistTemplateRepository
) : Synchronizer {

    // push all to backend
    // push all changes to backend
    // get all data from backend - updating if it's already there

    override suspend fun synchronize() {
        val checklistTemplates = checklistTemplateRepository.getAll().first()
        val checklistTemplatesWithRemoteId = remoteChecklistTemplateRepository.pushAll(checklistTemplates)
        Timber.d("Response: $checklistTemplatesWithRemoteId")
//        checklistTemplateRepository.updateAll(checklistTemplatesWithRemoteId)
    }
}
