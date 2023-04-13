package dev.szymonchaber.checkstory.data.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.*

class Migration5to6(
    private val uuidGenerator: () -> UUID = {
        UUID.randomUUID()
    }
) : Migration(5, 6) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.migrateReminderIds()
        database.migrateCheckboxEntities()
        database.migrateTemplateCheckboxEntities()
    }

    private fun SupportSQLiteDatabase.migrateReminderIds() {
        execSQL("ALTER TABLE ReminderEntity ADD COLUMN uuid BLOB")
        addTemporaryUuids(this, "ReminderEntity", "reminderId")


        execSQL("CREATE TABLE ReminderEntityNew (`reminderId` BLOB NOT NULL, `templateId` INTEGER NOT NULL, `startDateUtc` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `recurrencePattern` TEXT, PRIMARY KEY(`reminderId`))")
        execSQL("INSERT INTO ReminderEntityNew (reminderId, templateId, startDateUtc, isRecurring, recurrencePattern) SELECT uuid, templateId, startDateUtc, isRecurring, recurrencePattern FROM ReminderEntity")
        execSQL("DROP TABLE ReminderEntity")
        execSQL("ALTER TABLE ReminderEntityNew RENAME TO ReminderEntity")
    }

    private fun SupportSQLiteDatabase.migrateCheckboxEntities() {
        execSQL("ALTER TABLE CheckboxEntity ADD COLUMN uuid BLOB")
        execSQL("ALTER TABLE CheckboxEntity ADD COLUMN parent_uuid BLOB")
        addTemporaryUuids(this, "CheckboxEntity", "checkboxId")

        execSQL(
            """
            UPDATE CheckboxEntity SET parent_uuid = (
            SELECT uuid FROM CheckboxEntity AS parent_tasks WHERE parent_tasks.checkboxId = CheckboxEntity.parentId
            );
        """.trimIndent()
        )


        execSQL("CREATE TABLE CheckboxEntityNew (`checkboxId` BLOB NOT NULL, `checklistId` INTEGER NOT NULL, `checkboxTitle` TEXT NOT NULL, `isChecked` INTEGER NOT NULL, `parentId` BLOB, PRIMARY KEY(`checkboxId`))")
        execSQL("INSERT INTO CheckboxEntityNew (checkboxId, checklistId, checkboxTitle, isChecked, parentId) SELECT uuid, checklistId, checkboxTitle, isChecked, parent_uuid FROM CheckboxEntity")
        execSQL("DROP TABLE CheckboxEntity")
        execSQL("ALTER TABLE CheckboxEntityNew RENAME TO CheckboxEntity")
    }

    private fun SupportSQLiteDatabase.migrateTemplateCheckboxEntities() {
        execSQL("ALTER TABLE TemplateCheckboxEntity ADD COLUMN uuid BLOB")
        execSQL("ALTER TABLE TemplateCheckboxEntity ADD COLUMN parent_uuid BLOB")
        addTemporaryUuids(this, "TemplateCheckboxEntity", "checkboxId")

        execSQL(
            """
            UPDATE TemplateCheckboxEntity SET parent_uuid = (
            SELECT uuid FROM TemplateCheckboxEntity AS parent_tasks WHERE parent_tasks.checkboxId = TemplateCheckboxEntity.parentId
            );
        """.trimIndent()
        )


        execSQL("CREATE TABLE TemplateCheckboxEntityNew (`checkboxId` BLOB NOT NULL, `templateId` INTEGER NOT NULL, `checkboxTitle` TEXT NOT NULL, `parentId` BLOB, `sortPosition` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`checkboxId`))")
        execSQL("INSERT INTO TemplateCheckboxEntityNew (checkboxId, templateId, checkboxTitle, parentId, sortPosition) SELECT uuid, templateId, checkboxTitle, parent_uuid, sortPosition FROM TemplateCheckboxEntity")
        execSQL("DROP TABLE TemplateCheckboxEntity")
        execSQL("ALTER TABLE TemplateCheckboxEntityNew RENAME TO TemplateCheckboxEntity")
    }

    private fun addTemporaryUuids(database: SupportSQLiteDatabase, tableName: String, idColumnName: String) {
        val cursor = database.query("SELECT * FROM $tableName")
        if (cursor.moveToFirst()) {
            do {
                val uuid = uuidGenerator()
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(idColumnName))
                val contentValues = ContentValues()
                    .apply {
                        put("uuid", UUIDUtil.convertUUIDToBytes(uuid))
                    }
                database.update(
                    tableName,
                    SQLiteDatabase.CONFLICT_ABORT,
                    contentValues,
                    "$idColumnName = ?",
                    arrayOf(id.toString())
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
}
