package dev.szymonchaber.checkstory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateCheckboxDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity

@Database(
    entities = [
        ChecklistTemplateEntity::class,
        TemplateCheckboxEntity::class,
        ChecklistEntity::class,
        CheckboxEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun checklistTemplateDao(): ChecklistTemplateDao

    abstract fun templateCheckboxDao(): TemplateCheckboxDao

    abstract fun checklistDao(): ChecklistDao

    abstract fun checkboxDao(): CheckboxDao
}
