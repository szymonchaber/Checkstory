package dev.szymonchaber.checkstory.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.szymonchaber.checkstory.data.database.dao.ChecklistTemplateDao
import dev.szymonchaber.checkstory.data.database.model.ChecklistTemplateEntity

@Database(entities = [ChecklistTemplateEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun checklistTemplateDao(): ChecklistTemplateDao
}