package dev.szymonchaber.checkstory.data.di

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import dev.szymonchaber.checkstory.data.database.AppDatabase
import dev.szymonchaber.checkstory.data.database.Migration5to6
import dev.szymonchaber.checkstory.data.database.UUIDUtil
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MigrationTest {

    private val TEST_DB = "migration-test"

    private val uuidCounter = AtomicInteger(0)

    private val knownUuids = List(15) { UUID.randomUUID() }

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate5to6() {
        helper.createDatabase(TEST_DB, 5)
            .apply {
                execSQL(
                    """
                    INSERT INTO "ReminderEntity" (templateId, startDateUtc, isRecurring, recurrencePattern) 
                    VALUES 
                    (1, 1649818800, 1, 'Every Monday'),
                    (2, 1650397200, 0, NULL),
                    (3, 1650991200, 1, 'Every other day');
                """.trimIndent()
                )
                close()
            }

        val database = helper.runMigrationsAndValidate(
            TEST_DB,
            6,
            true,
            Migration5to6 { knownUuids[uuidCounter.getAndIncrement()] })
        verify(database)
    }

    private fun verify(database: SupportSQLiteDatabase) {
        val cursor = database.query("SELECT * FROM ReminderEntity", null)
        var index = 0
        if (cursor.moveToFirst()) {
            do {
                val uuid = cursor.getBlob(cursor.getColumnIndexOrThrow("reminderId"))
                assertThat(UUIDUtil.convertBytesToUUID(uuid)).isEqualTo(knownUuids[index++])
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

}
