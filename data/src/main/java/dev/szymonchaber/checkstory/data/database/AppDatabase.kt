package dev.szymonchaber.checkstory.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.szymonchaber.checkstory.data.database.dao.CheckboxDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistDao
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.dao.ReminderDao
import dev.szymonchaber.checkstory.data.database.dao.TemplateCheckboxDao
import dev.szymonchaber.checkstory.data.database.model.CheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistEntity
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity
import dev.szymonchaber.checkstory.data.database.model.TemplateCheckboxEntity
import dev.szymonchaber.checkstory.data.database.model.reminder.ReminderEntity

@Database(
    entities = [
        ChecklistTemplateEntity::class,
        TemplateCheckboxEntity::class,
        ChecklistEntity::class,
        CheckboxEntity::class,
        ReminderEntity::class
    ],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ]
)
@TypeConverters(value = [Converters::class])
abstract class AppDatabase : RoomDatabase() {

    abstract val checklistTemplateDao: ChecklistTemplateDao

    abstract val templateCheckboxDao: TemplateCheckboxDao

    abstract val checklistDao: ChecklistDao

    abstract val checkboxDao: CheckboxDao

    abstract val reminderDao: ReminderDao
}
