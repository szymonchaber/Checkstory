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
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
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
        )
            .setQueryCallback({ sqlQuery, bindArgs ->
                println("SQL Query: $sqlQuery SQL Args: $bindArgs")
            }, Executors.newSingleThreadExecutor())
            .build()
            .apply { insertDummyData() }
    }

    private fun AppDatabase.insertDummyData() {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                // TODO the app without below code will crash
                checklistTemplateDao.insert(
                    ChecklistTemplateEntity(
                        1,
                        "Cleaning something",
                        "It's good to do",
                        LocalDateTime.now().minusDays(2)
                    )
                )
                templateCheckboxDao.insertAll(
                    TemplateCheckboxEntity(0, 1, "Checkbox item"),
                    TemplateCheckboxEntity(0, 1, "Checkbox item 2")
                )
                insert {
                    checklist(1, "This was an awesome session") {
                        checkbox("Clean the table", true)
                        checkbox("Dust the lamp shade", false)
                        checkbox("Clean all the windows", false)
                        checkbox("Be awesome", true)
                    }
                    checklist(
                        1,
                        "We should focus on upkeep of cleanliness, rather than doing this huge cleaning sessions"
                    ) {
                        checkbox("Clean the table", false)
                        checkbox("Dust the lamp shade", true)
                        checkbox("Clean all the windows", true)
                        checkbox("Be totally awesome", false)
                    }
                }
            }
        }
    }

    @Provides
    fun provideChecklistTemplateDao(database: AppDatabase): ChecklistTemplateDao {
        return database.checklistTemplateDao
    }

    @Provides
    fun provideTemplateCheckboxDao(database: AppDatabase): TemplateCheckboxDao {
        return database.templateCheckboxDao
    }

    @Provides
    fun provideChecklistDao(database: AppDatabase): ChecklistDao {
        return database.checklistDao
    }

    @Provides
    fun provideCheckboxDao(database: AppDatabase): CheckboxDao {
        return database.checkboxDao
    }
}

class InsertDsl {

    class Checklist(val templateId: Long, val notes: String = "")

    class Checkbox(val title: String, val isChecked: Boolean = false)

    private val items = mutableMapOf<Checklist, List<Checkbox>>()

    class CheckboxDsl {

        val checkboxes = mutableListOf<Checkbox>()

        fun checkbox(title: String, isChecked: Boolean = false) {
            checkboxes.add(Checkbox(title, isChecked))
        }
    }

    fun checklist(templateId: Long, notes: String = "", checkboxesBlock: CheckboxDsl.() -> Unit) {
        items[Checklist(templateId, notes)] = CheckboxDsl().apply(checkboxesBlock).checkboxes
    }

    fun insertInto(appDatabase: AppDatabase) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                items.map { (checklist, checkboxes) ->
                    appDatabase.checklistDao.insert(
                        ChecklistEntity(
                            0,
                            checklist.templateId,
                            checklist.notes,
                            LocalDateTime.now().minusDays(2)
                        )
                    ) to checkboxes
                }.forEach { (checklistId, checkboxes) ->
                    appDatabase.checkboxDao.insertAll(
                        *checkboxes.map {
                            CheckboxEntity(
                                0,
                                checklistId,
                                it.title,
                                it.isChecked
                            )
                        }.toTypedArray()
                    )
                }
            }
        }
    }
}

fun AppDatabase.insert(block: (InsertDsl).() -> Unit) {

    InsertDsl().apply(block).insertInto(this)
}
