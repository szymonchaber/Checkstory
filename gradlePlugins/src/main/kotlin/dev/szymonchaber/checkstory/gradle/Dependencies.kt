package dev.szymonchaber.checkstory.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class Dependencies : Plugin<Project> {

    override fun apply(target: Project) = Unit

    companion object {

        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0"

        private const val hiltVersion = "2.41"
        private const val hiltLibrary = "com.google.dagger:hilt-android:$hiltVersion"
        const val hiltKapt = "com.google.dagger:hilt-compiler:$hiltVersion"
        private const val timber = "com.jakewharton.timber:timber:5.0.1"

        val common = listOf(coroutines, hiltLibrary, timber)

        const val androidXCore = "androidx.core:core-ktx:1.7.0"
        const val appCompat = "androidx.appcompat:appcompat:1.4.1"

        private const val lifecycleVersion = "2.4.1"
        val lifecycle = listOf(
            "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion",
            "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion",
            "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion"
        )

        const val composeVersion = "1.1.1"
        val compose = listOf(
            "androidx.compose.ui:ui:$composeVersion",
            "androidx.compose.ui:ui-tooling:$composeVersion",
            "androidx.compose.material:material:$composeVersion",
            "androidx.activity:activity-compose:1.4.0",
            "androidx.navigation:navigation-compose:2.4.1",
            "androidx.hilt:hilt-navigation-compose:1.0.0"
        )

        private val composeDestinationsVersion = "1.4.2-beta"
        val composeDestinations = listOf(
            "io.github.raamcosta.compose-destinations:core:$composeDestinationsVersion",
            "io.github.raamcosta.compose-destinations:animations-core:$composeDestinationsVersion"
        )
        val composeDestinationsKsp =
            "io.github.raamcosta.compose-destinations:ksp:$composeDestinationsVersion"

        private const val roomVersion = "2.4.2"

        val room = listOf(
            "androidx.room:room-runtime:$roomVersion",
            "androidx.room:room-ktx:$roomVersion",
            "androidx.room:room-paging:2.5.0-alpha01"
        )
        val roomKsp = "androidx.room:room-compiler:$roomVersion"

        val ui = listOf(androidXCore, appCompat) + compose + lifecycle

        val ads = "com.google.android.gms:play-services-ads:20.6.0"

        val firebasePlatform = "com.google.firebase:firebase-bom:30.1.0"

        val analytics = "com.google.firebase:firebase-analytics-ktx"
        val crashlytics = "com.google.firebase:firebase-crashlytics"
        val performance = "com.google.firebase:firebase-perf"
        val messaging = "com.google.firebase:firebase-messaging-ktx"

        val rrule = "com.github.PhilJay:RRule:1.0.3"

        const val workVersion = "2.7.1"

        val work = "androidx.work:work-runtime-ktx:$workVersion"

        val unitTest = listOf(
            "junit:junit:4.13.2",
            "com.google.truth:truth:1.1.3"
        )

        val uiTest = listOf(
            "androidx.test.ext:junit:1.1.3",
            "androidx.test.espresso:espresso-core:3.4.0",
            "androidx.compose.ui:ui-test-junit4:$composeVersion",
            "androidx.room:room-testing:$roomVersion"
        )
    }
}
