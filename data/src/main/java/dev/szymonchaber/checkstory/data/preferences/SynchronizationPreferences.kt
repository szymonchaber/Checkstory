package dev.szymonchaber.checkstory.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import dev.szymonchaber.checkstory.domain.repository.SynchronizationStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SynchronizationPreferences @Inject constructor(
    @Named("synchronizationPreferences")
    private val dataStore: DataStore<Preferences>
) : SynchronizationStatusRepository {

    override val lastSuccessfulSynchronizationDate: Flow<Instant?> = dataStore.data
        .map {
            it[KEY_LAST_SUCCESS_SYNC_DATE]?.let { date ->
                Instant.fromEpochMilliseconds(date)
            }
        }

    override val lastFailedSynchronizationDate: Flow<Instant?> = dataStore.data
        .map {
            it[KEY_LAST_FAIL_SYNC_DATE]?.let { date ->
                Instant.fromEpochMilliseconds(date)
            }
        }

    override suspend fun markSuccess() {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SUCCESS_SYNC_DATE] = Clock.System.now().toEpochMilliseconds()
        }
    }

    override suspend fun markFailure() {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_FAIL_SYNC_DATE] = Clock.System.now().toEpochMilliseconds()
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_LAST_SUCCESS_SYNC_DATE)
            preferences.remove(KEY_LAST_FAIL_SYNC_DATE)
        }
    }

    companion object {

        private val KEY_LAST_SUCCESS_SYNC_DATE = longPreferencesKey("LAST_SUCCESS_SYNC_DATE")
        private val KEY_LAST_FAIL_SYNC_DATE = longPreferencesKey("LAST_FAIL_SYNC_DATE")
    }
}
