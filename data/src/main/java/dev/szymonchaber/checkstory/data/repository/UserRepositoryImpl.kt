package dev.szymonchaber.checkstory.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.szymonchaber.checkstory.domain.model.Tier
import dev.szymonchaber.checkstory.domain.model.User
import dev.szymonchaber.checkstory.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "currentUser")

private val KEY_IS_PAID_USER = booleanPreferencesKey("isPaidUser")

class UserRepositoryImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
) : UserRepository {

    override suspend fun storeCurrentUser(user: User) {
        context.dataStore.edit { settings ->
            settings[KEY_IS_PAID_USER] = user.isPaidUser
        }
    }

    override suspend fun removeCurrentUser() {
        context.dataStore.edit { settings ->
            settings.remove(KEY_IS_PAID_USER)
        }
    }

    override suspend fun getCurrentUser(): User {
        return getCurrentUserFlow().first()
    }

    override fun getCurrentUserFlow(): Flow<User> {
        return context.dataStore.data
            .map { preferences ->
                when (val isPaidUser = preferences[KEY_IS_PAID_USER]) {
                    null -> User.Guest
                    else -> User.LoggedIn(
                        if (isPaidUser) {
                            Tier.PAID
                        } else {
                            Tier.FREE
                        }
                    )
                }
            }
    }
}
