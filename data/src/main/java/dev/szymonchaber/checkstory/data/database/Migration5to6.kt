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
        database.migrateTemplateEntities()
    }

    private fun SupportSQLiteDatabase.migrateTemplateEntities() {
        val checklisTemplateUuidColumn = "templateUuid"
        addTemporaryUuids(this, "ChecklistTemplateEntity", "id", checklisTemplateUuidColumn)

        migrateReminderIds(checklisTemplateUuidColumn)
        migrateTemplateCheckboxEntities(checklisTemplateUuidColumn)
        migrateChecklistEntities(checklisTemplateUuidColumn)

        migrateTable(
            name = "ChecklistTemplateEntity",
            columnsToCreate = "`id` BLOB NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(`id`)",
            columnsToInsert = "id, title, description, createdAt",
            dataSourceColumns = "uuid, title, description, createdAt"
        )
    }

    private fun SupportSQLiteDatabase.migrateReminderIds(templateUuidColumn: String) {
        addTemporaryUuids(this, "ReminderEntity", "reminderId", "uuid")
        execSQL("ALTER TABLE ReminderEntity ADD COLUMN template_uuid BLOB")

        execSQL(
            """
            UPDATE ReminderEntity SET template_uuid = (
            SELECT $templateUuidColumn FROM ChecklistTemplateEntity WHERE ChecklistTemplateEntity.id = ReminderEntity.templateId
            );
        """.trimIndent()
        )

        migrateTable(
            name = "ReminderEntity",
            columnsToCreate = "`reminderId` BLOB NOT NULL, `templateId` BLOB NOT NULL, `startDateUtc` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `recurrencePattern` TEXT, PRIMARY KEY(`reminderId`)",
            columnsToInsert = "reminderId, templateId, startDateUtc, isRecurring, recurrencePattern",
            dataSourceColumns = "uuid, template_uuid, startDateUtc, isRecurring, recurrencePattern"
        )
    }

    private fun SupportSQLiteDatabase.migrateCheckboxEntities(checklistUuidColumn: String) {
        addTemporaryUuids(this, "CheckboxEntity", "checkboxId", "uuid")
        execSQL("ALTER TABLE CheckboxEntity ADD COLUMN parent_uuid BLOB")
        execSQL("ALTER TABLE CheckboxEntity ADD COLUMN checklist_uuid BLOB")

        execSQL(
            """
            UPDATE CheckboxEntity SET parent_uuid = (
            SELECT uuid FROM CheckboxEntity AS parent_tasks WHERE parent_tasks.checkboxId = CheckboxEntity.parentId
            );
        """.trimIndent()
        )
        execSQL(
            """
            UPDATE CheckboxEntity SET checklist_uuid = (
            SELECT $checklistUuidColumn FROM ChecklistEntity WHERE ChecklistEntity.checklistId = CheckboxEntity.checklistId
            );
        """.trimIndent()
        )


        migrateTable(
            name = "CheckboxEntity",
            columnsToCreate = "`checkboxId` BLOB NOT NULL, `checklistId` BLOB NOT NULL, `checkboxTitle` TEXT NOT NULL, `isChecked` INTEGER NOT NULL, `parentId` BLOB, PRIMARY KEY(`checkboxId`)",
            columnsToInsert = "checkboxId, checklistId, checkboxTitle, isChecked, parentId",
            dataSourceColumns = "uuid, checklist_uuid, checkboxTitle, isChecked, parent_uuid"
        )
    }

    private fun SupportSQLiteDatabase.migrateTemplateCheckboxEntities(templateUuidColumn: String) {
        addTemporaryUuids(this, "TemplateCheckboxEntity", "checkboxId", "uuid")
        execSQL("ALTER TABLE TemplateCheckboxEntity ADD COLUMN parent_uuid BLOB")
        execSQL("ALTER TABLE TemplateCheckboxEntity ADD COLUMN template_uuid BLOB")

        execSQL(
            """
            UPDATE TemplateCheckboxEntity SET parent_uuid = (
            SELECT uuid FROM TemplateCheckboxEntity AS parent_tasks WHERE parent_tasks.checkboxId = TemplateCheckboxEntity.parentId
            );
        """.trimIndent()
        )
        execSQL(
            """
            UPDATE TemplateCheckboxEntity SET template_uuid = (
            SELECT $templateUuidColumn FROM ChecklistTemplateEntity WHERE ChecklistTemplateEntity.id = TemplateCheckboxEntity.templateId
            );
        """.trimIndent()
        )


        migrateTable(
            name = "TemplateCheckboxEntity",
            columnsToCreate = "`checkboxId` BLOB NOT NULL, `templateId` BLOB NOT NULL, `checkboxTitle` TEXT NOT NULL, `parentId` BLOB, `sortPosition` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`checkboxId`)",
            columnsToInsert = "checkboxId, templateId, checkboxTitle, parentId, sortPosition",
            dataSourceColumns = "uuid, template_uuid, checkboxTitle, parent_uuid, sortPosition"
        )
    }

    private fun SupportSQLiteDatabase.migrateChecklistEntities(templateUuidColumn: String) {
        val checklistUuidColumn = "checklist_uuid"
        addTemporaryUuids(this, "ChecklistEntity", "checklistId", checklistUuidColumn)
        execSQL("ALTER TABLE ChecklistEntity ADD COLUMN template_uuid BLOB")

        migrateCheckboxEntities(checklistUuidColumn)

        execSQL(
            """
            UPDATE ChecklistEntity SET template_uuid = (
            SELECT $templateUuidColumn FROM ChecklistTemplateEntity WHERE ChecklistTemplateEntity.id = ChecklistEntity.templateId
            );
        """.trimIndent()
        )

        migrateTable(
            name = "ChecklistEntity",
            columnsToCreate = "`checklistId` BLOB NOT NULL, `templateId` BLOB NOT NULL, notes TEXT NOT NULL, createdAt INTEGER NOT NULL, PRIMARY KEY(`checklistId`)",
            columnsToInsert = "checklistId, templateId, notes, createdAt",
            dataSourceColumns = "uuid, template_uuid, notes, createdAt"
        )
    }

    private fun SupportSQLiteDatabase.migrateTable(
        name: String,
        columnsToCreate: String,
        columnsToInsert: String,
        dataSourceColumns: String
    ) {
        execSQL("CREATE TABLE ${name}New ($columnsToCreate)")
        execSQL("INSERT INTO ${name}New ($columnsToInsert) SELECT $dataSourceColumns FROM $name")
        execSQL("DROP TABLE $name")
        execSQL("ALTER TABLE ${name}New RENAME TO $name")
    }


    private fun addTemporaryUuids(
        database: SupportSQLiteDatabase,
        tableName: String,
        idColumnName: String,
        newTemporaryUuidColum: String
    ) {
        database.execSQL("ALTER TABLE $tableName ADD COLUMN $newTemporaryUuidColum BLOB")
        val cursor = database.query("SELECT * FROM $tableName")
        if (cursor.moveToFirst()) {
            do {
                val uuid = uuidGenerator()
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(idColumnName))
                val contentValues = ContentValues()
                    .apply {
                        put(newTemporaryUuidColum, UUIDUtil.convertUUIDToBytes(uuid))
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
