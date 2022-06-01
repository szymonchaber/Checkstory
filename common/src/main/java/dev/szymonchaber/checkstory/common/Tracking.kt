package dev.szymonchaber.checkstory.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent

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
