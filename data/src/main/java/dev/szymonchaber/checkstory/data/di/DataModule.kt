package dev.szymonchaber.checkstory.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.szymonchaber.checkstory.data.repository.ChecklistRepositoryImpl
import dev.szymonchaber.checkstory.data.repository.ChecklistTemplateRepositoryImpl
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository
import dev.szymonchaber.checkstory.domain.repository.ChecklistTemplateRepository

@Module
@InstallIn(ViewModelComponent::class)
interface DataModule {

    @Binds
    fun bindChecklistRepository(checklistRepository: ChecklistRepositoryImpl): ChecklistRepository

    @Binds
    fun bindChecklistTemplateRepository(checklistTemplateRepository: ChecklistTemplateRepositoryImpl): ChecklistTemplateRepository
}
