package dev.szymonchaber.checkstory.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class Dependencies : Plugin<Project> {

    override fun apply(target: Project) = Unit

    companion object {

        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0"

        private const val hiltVersion = "2.41"
        const val hiltLibrary = "com.google.dagger:hilt-android:$hiltVersion"
        const val hiltKapt = "com.google.dagger:hilt-compiler:$hiltVersion"

        val common = listOf(coroutines, hiltLibrary)

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
        val composeDestinations =
            "io.github.raamcosta.compose-destinations:core:$composeDestinationsVersion"
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

        val unitTest = listOf(
            "junit:junit:4.13.2"
        )

        val uiTest = listOf(
            "androidx.test.ext:junit:1.1.3",
            "androidx.test.espresso:espresso-core:3.4.0",
            "androidx.compose.ui:ui-test-junit4:$composeVersion",
            "androidx.room:room-testing:$roomVersion"
        )
    }
}
