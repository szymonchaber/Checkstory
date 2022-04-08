package dev.szymonchaber.checkstory.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.data.database.AppDatabase
import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateCheckboxDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
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
        ).setQueryCallback({ sqlQuery, bindArgs ->
            println("SQL Query: $sqlQuery SQL Args: $bindArgs")
        }, Executors.newSingleThreadExecutor())
            .build().also {
                GlobalScope.launch {
                    withContext(Dispatchers.IO) {
                        it.checklistTemplateDao().insert(
                            ChecklistTemplateEntity(
                                1,
                                "Cleaning something",
                                "It's good to do"
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

    @Provides
    fun provideTemplateCheckboxDao(database: AppDatabase): TemplateCheckboxDao {
        return database.templateCheckboxDao()
    }

    @Provides
    fun provideChecklistDao(database: AppDatabase): ChecklistDao {
        return database.checklistDao()
    }

    @Provides
    fun provideCheckboxDao(database: AppDatabase): CheckboxDao {
        return database.checkboxDao()
    }
}
