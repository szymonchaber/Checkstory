package dev.szymonchaber.checkstory.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration8to9 : Migration(8, 9) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ChecklistTemplateEntity ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE ChecklistTemplateEntity SET updatedAt = createdAt")

        db.execSQL("ALTER TABLE ChecklistEntity ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE ChecklistEntity SET updatedAt = createdAt")
    }
}
