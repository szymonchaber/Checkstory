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
        execSQL("ALTER TABLE ReminderEntity ADD COLUMN actualUuid BLOB")
        updateRowsWithUUIDs(this, "ReminderEntity")
    }

    private fun updateRowsWithUUIDs(database: SupportSQLiteDatabase, tableName: String) {
        val cursor = database.query("SELECT * FROM $tableName", null)
        if (cursor.moveToFirst()) {
            do {
                val uuid = uuidGenerator()
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("reminderId"))
                val contentValues = ContentValues()
                    .apply {
                        put("actualUuid", UUIDUtil.convertUUIDToBytes(uuid))
                    }
                database.update(
                    tableName,
                    SQLiteDatabase.CONFLICT_ABORT,
                    contentValues,
                    "reminderId = ?",
                    arrayOf(id.toString())
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
    }
}
