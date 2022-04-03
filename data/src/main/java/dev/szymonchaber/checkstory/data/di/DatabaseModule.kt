package dev.szymonchaber.checkstory.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.data.database.AppDatabase
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "checkstory-database"
        ).build().also {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    it.checklistTemplateDao().insertAll(
                        ChecklistTemplateEntity(
                            1,
                            "First template",
                            "Description"
                        )
                    )
                    it.templateCheckboxDao().insertAll(
                        TemplateCheckboxEntity(
                            0,
                            1,
                            "Checkbox item"
                        ),
                        TemplateCheckboxEntity(
                            0,
                            1,
                            "Checkbox item 2"
                        )
                    )
                }
            }
        }
    }

    @Provides
    fun provideChecklistTemplateDao(database: AppDatabase): ChecklistTemplateDao {
        return database.checklistTemplateDao()
    }
}