package dev.szymonchaber.checkstory.common

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission", "ComposableNaming")
@Composable
fun trackScreenName(screenName: String) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        FirebaseAnalytics.getInstance(context)
            .logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
                param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            }
    }
}

@SuppressLint("MissingPermission")
@Singleton
class Tracker @Inject constructor(
    @ApplicationContext context: Context
) {

    private val analytics = FirebaseAnalytics.getInstance(context)

    fun logEvent(eventName: String) {
        analytics.logEvent(eventName) {
        }
    }
}
