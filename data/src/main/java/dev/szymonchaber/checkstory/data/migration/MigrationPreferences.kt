package dev.szymonchaber.checkstory.data.migration

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "migrations")
private val KEY_DID_RUN_COMMAND_MIGRATION = booleanPreferencesKey("didRunCommandMigration")

internal class MigrationPreferences @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    suspend fun didRunCommandModelMigration(): Boolean {
        return context.dataStore.data.first()[KEY_DID_RUN_COMMAND_MIGRATION] ?: false
    }

    suspend fun markDidRunCommandModelMigration() {
        context.dataStore.edit { settings ->
            settings[KEY_DID_RUN_COMMAND_MIGRATION] = true
        }
    }
}
