package dev.szymonchaber.checkstory.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class UserDataStore @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    suspend fun storeCurrentUser(user: User.LoggedIn) {
        context.dataStore.edit { settings ->
            settings[KEY_IS_PAID_USER] = user.isPaidUser
            settings[KEY_USER_ID] = user.id
            settings.insertOrRemove(KEY_USER_EMAIL, user.email)
        }
    }

    suspend fun removeCurrentUser() {
        context.dataStore.edit { settings ->
            settings.remove(KEY_IS_PAID_USER)
            settings.remove(KEY_USER_ID)
            settings.remove(KEY_USER_EMAIL)
        }
    }

    suspend fun getCurrentUser(): User {
        return getCurrentUserFlow().first()
    }

    fun getCurrentUserFlow(): Flow<User> {
        return context.dataStore.data
            .map { preferences ->
                val requiredValuesPresent = preferences.contains(KEY_USER_ID) && preferences.contains(KEY_IS_PAID_USER)
                if (!requiredValuesPresent) {
                    User.Guest()
                } else {
                    User.LoggedIn(
                        id = preferences[KEY_USER_ID]!!,
                        email = preferences[KEY_USER_EMAIL],
                        if (preferences[KEY_IS_PAID_USER]!!) {
                            Tier.PAID
                        } else {
                            Tier.FREE
                        }
                    )
                }
            }
    }

    companion object {

        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "currentUser")

        private val KEY_IS_PAID_USER = booleanPreferencesKey("isPaidUser")
        private val KEY_USER_ID = stringPreferencesKey("userId")
        private val KEY_USER_EMAIL = stringPreferencesKey("userEmail")

        private fun MutablePreferences.insertOrRemove(key: Preferences.Key<String>, value: String?) {
            if (value != null) {
                this[key] = value
            } else {
                this.remove(key)
            }
        }
    }
}
