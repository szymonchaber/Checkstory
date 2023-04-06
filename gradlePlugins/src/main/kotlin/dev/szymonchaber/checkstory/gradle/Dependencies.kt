package dev.szymonchaber.checkstory.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class Dependencies : Plugin<Project> {

    override fun apply(target: Project) = Unit

    companion object {

        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4"

        private const val hiltVersion = "2.44.2"
        private const val hiltLibrary = "com.google.dagger:hilt-android:$hiltVersion"
        const val hiltKapt = "com.google.dagger:hilt-compiler:$hiltVersion"
        private const val timber = "com.jakewharton.timber:timber:5.0.1"
        private const val kotlinImmutableCollections = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5"

        val common = listOf(coroutines, hiltLibrary, timber, kotlinImmutableCollections)

        const val androidXCore = "androidx.core:core-ktx:1.9.0"
        const val appCompat = "androidx.appcompat:appcompat:1.7.0-alpha02"

        private const val lifecycleVersion = "2.4.1"
        val lifecycle = listOf(
            "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion",
            "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion",
            "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion"
        )

        const val composeVersion = "1.4.0"
        val compose = listOf(
            "androidx.compose.ui:ui:$composeVersion",
            "androidx.compose.ui:ui-tooling-preview:$composeVersion",
            "androidx.compose.material:material:$composeVersion",
            "androidx.activity:activity-compose:1.7.0-beta01",
            "androidx.navigation:navigation-compose:2.5.3",
            "androidx.hilt:hilt-navigation-compose:1.0.0"
        )

        val debugUiTooling = "androidx.compose.ui:ui-tooling:$composeVersion"

        private val composeDestinationsVersion = "1.8.33-beta"
        val composeDestinations = listOf(
            "io.github.raamcosta.compose-destinations:core:$composeDestinationsVersion",
            "io.github.raamcosta.compose-destinations:animations-core:$composeDestinationsVersion"
        )
        val composeDestinationsKsp =
            "io.github.raamcosta.compose-destinations:ksp:$composeDestinationsVersion"

        val accompanist = listOf(
            "com.google.accompanist:accompanist-pager:0.29.1-alpha",
            "com.google.accompanist:accompanist-pager-indicators:0.29.1-alpha"
        )

        private const val roomVersion = "2.4.2"

        val room = listOf(
            "androidx.room:room-runtime:$roomVersion",
            "androidx.room:room-ktx:$roomVersion",
            "androidx.room:room-paging:2.5.0-alpha01"
        )
        val roomKsp = "androidx.room:room-compiler:$roomVersion"

        val dataStore = "androidx.datastore:datastore-preferences:1.0.0"

        val ui = listOf(androidXCore, appCompat) + compose + lifecycle + accompanist

        val ads = "com.google.android.gms:play-services-ads:22.0.0"

        // region firebase
        val firebasePlatform = "com.google.firebase:firebase-bom:31.2.1"

        val analytics = "com.google.firebase:firebase-analytics-ktx"
        val crashlytics = "com.google.firebase:firebase-crashlytics"
        val performance = "com.google.firebase:firebase-perf"
        val messaging = "com.google.firebase:firebase-messaging-ktx"
        //endregion

        val rrule = "com.github.PhilJay:RRule:1.0.3"

        const val workVersion = "2.8.0"

        val work = "androidx.work:work-runtime-ktx:$workVersion"

        private val billing_version = "5.1.0"

        val billing = "com.android.billingclient:billing-ktx:$billing_version"

        val review = "com.google.android.play:review-ktx:2.0.1"

        val arrow = "io.arrow-kt:arrow-core:1.0.1"

        val composeDialogsDateTime = "io.github.vanpra.compose-material-dialogs:datetime:0.9.0"

        val composeReorderable = "org.burnoutcrew.composereorderable:reorderable:0.9.6"

        val unitTest = listOf(
            "junit:junit:4.13.2",
            "com.google.truth:truth:1.1.3"
        )

        val uiTest = listOf(
            "androidx.test.ext:junit:1.1.5",
            "androidx.test.espresso:espresso-core:3.5.1",
            "androidx.compose.ui:ui-test-junit4:$composeVersion",
            "androidx.room:room-testing:$roomVersion"
        )
    }
}
