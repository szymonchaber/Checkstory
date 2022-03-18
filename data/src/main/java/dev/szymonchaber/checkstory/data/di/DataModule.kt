package dev.szymonchaber.checkstory.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dev.szymonchaber.checkstory.data.repository.ChecklistRepositoryImpl
import dev.szymonchaber.checkstory.domain.repository.ChecklistRepository

@Module
@InstallIn(ViewModelComponent::class)
interface DataModule {

    @Binds
    fun bindChecklistRepository(checklistRepository: ChecklistRepositoryImpl): ChecklistRepository
}
