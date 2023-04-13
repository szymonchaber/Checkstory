package dev.szymonchaber.checkstory.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.szymonchaber.checkstory.data.database.AppDatabase
import dev.szymonchaber.checkstory.data.database.Migration5to6
import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.dao.ReminderDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateCheckboxDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.domain.model.checklist.fill.Checkbox
import dev.szymonchaber.checkstory.domain.model.checklist.fill.CheckboxId
import dev.szymonchaber.checkstory.domain.model.checklist.fill.ChecklistId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.*
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
            .addMigrations(Migration5to6())
            .build()
//            .apply { insertDummyData() }
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
                    TemplateCheckboxEntity(1, 1, "Checkbox item", null, 0),
                    TemplateCheckboxEntity(2, 1, "Checkbox item 2", null, 0)
                )
                insert {
                    checklist(1, 1, "This was an awesome session") {
                        checkbox("Clean the table", true)
                        checkbox("Dust the lamp shade", false)
                        checkbox("Clean all the windows", false)
                        checkbox("Be awesome", true)
                    }
                    checklist(
                        2,
                        1,
                        "We should focus on upkeep of cleanliness, rather than doing this huge cleaning sessions"
                    ) {
                        checkbox("Clean the table", false)
                        checkbox("Dust the lamp shade", false)
                        checkbox("Clean all the windows", false)
                        checkbox("Be totally awesome", true)
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

    @Provides
    fun provideReminderDao(database: AppDatabase): ReminderDao {
        return database.reminderDao
    }
}

class InsertDsl {

    class Checklist(val checklistId: Long, val templateId: Long, val notes: String = "")

    private val items = mutableMapOf<Checklist, List<Checkbox>>()

    class CheckboxDsl {

        val checkboxes = mutableListOf<Checkbox>()

        fun checkbox(title: String, isChecked: Boolean = false) {
            checkboxes.add(Checkbox(CheckboxId(UUID.randomUUID()), null, ChecklistId(0), title, isChecked, listOf()))
        }
    }

    fun checklist(checklistId: Long, templateId: Long, notes: String = "", checkboxesBlock: CheckboxDsl.() -> Unit) {
        items[Checklist(checklistId, templateId, notes)] = CheckboxDsl().apply(checkboxesBlock).checkboxes
    }

    fun insertInto(appDatabase: AppDatabase) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                items.map { (checklist, checkboxes) ->
                    appDatabase.checklistDao.insert(
                        ChecklistEntity(
                            checklist.checklistId,
                            checklist.templateId,
                            checklist.notes,
                            LocalDateTime.now().minusDays(2)
                        )
                    ) to checkboxes
                }.forEach { (checklistId, checkboxes) ->
                    appDatabase.checkboxDao.insertAll(
                        *checkboxes.map {
                            CheckboxEntity(
                                it.id.id,
                                checklistId,
                                it.title,
                                it.isChecked,
                                it.parentId?.id
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
