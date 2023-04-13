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
    }

    private fun SupportSQLiteDatabase.migrateReminderIds() {
        execSQL("ALTER TABLE ReminderEntity ADD COLUMN uuid BLOB")
        addTemporaryUuids(this, "ReminderEntity", "reminderId")


        execSQL("CREATE TABLE ReminderEntityNew (`reminderId` BLOB NOT NULL, `templateId` INTEGER NOT NULL, `startDateUtc` INTEGER NOT NULL, `isRecurring` INTEGER NOT NULL, `recurrencePattern` TEXT, PRIMARY KEY(`reminderId`))")
        execSQL("INSERT INTO ReminderEntityNew (reminderId, templateId, startDateUtc, isRecurring, recurrencePattern) SELECT uuid, templateId, startDateUtc, isRecurring, recurrencePattern FROM ReminderEntity")
        execSQL("DROP TABLE ReminderEntity")
        execSQL("ALTER TABLE ReminderEntityNew RENAME TO ReminderEntity")
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
