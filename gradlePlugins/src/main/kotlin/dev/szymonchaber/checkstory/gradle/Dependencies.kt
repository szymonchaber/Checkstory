package dev.szymonchaber.checkstory.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class Dependencies : Plugin<Project> {

    override fun apply(target: Project) = Unit

    companion object {

        private const val hiltVersion = "2.44.2"
        const val hiltKapt = "com.google.dagger:hilt-compiler:$hiltVersion"

        const val hiltWork = "androidx.hilt:hilt-work:1.0.0"
        const val hiltWorkKapt = "androidx.hilt:hilt-compiler:1.0.0"

        const val composeVersion = "1.4.0"

        val debugUiTooling = "androidx.compose.ui:ui-tooling:$composeVersion"

        private val composeDestinationsVersion = "1.8.33-beta"
        val composeDestinations = listOf(
            "io.github.raamcosta.compose-destinations:core:$composeDestinationsVersion",
            "io.github.raamcosta.compose-destinations:animations-core:$composeDestinationsVersion"
        )
        val composeDestinationsKsp =
            "io.github.raamcosta.compose-destinations:ksp:$composeDestinationsVersion"

        private const val roomVersion = "2.5.1"

        val room = listOf(
            "androidx.room:room-runtime:$roomVersion",
            "androidx.room:room-ktx:$roomVersion",
            "androidx.room:room-paging:2.5.1"
        )
        val roomKsp = "androidx.room:room-compiler:$roomVersion"

        val dataStore = "androidx.datastore:datastore-preferences:1.0.0"

        val ads = "com.google.android.gms:play-services-ads:22.0.0"

        // region firebase
        val firebasePlatform = "com.google.firebase:firebase-bom:31.2.1"

        val analytics = "com.google.firebase:firebase-analytics-ktx"
        val crashlytics = "com.google.firebase:firebase-crashlytics"
        val performance = "com.google.firebase:firebase-perf"
        val messaging = "com.google.firebase:firebase-messaging-ktx"
        val auth = "com.google.firebase:firebase-auth-ktx"
        //endregion

        val rrule = "com.github.PhilJay:RRule:1.0.3"

        val work = "androidx.work:work-runtime-ktx:2.8.1"

        private val billing_version = "5.1.0"

        val billing = "com.android.billingclient:billing-ktx:$billing_version"

        val review = "com.google.android.play:review-ktx:2.0.1"

        val arrow = "io.arrow-kt:arrow-core:1.0.1"

        val composeDialogsDateTime = "io.github.vanpra.compose-material-dialogs:datetime:0.9.0"

        val composeReorderable = "org.burnoutcrew.composereorderable:reorderable:0.9.6"

        const val kotlinx_datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.4.0"

        val unitTest = listOf(
            "junit:junit:4.13.2",
            "com.google.truth:truth:1.1.3",
            "androidx.room:room-testing:$roomVersion",
            "org.mockito:mockito-core:5.3.1",
            "org.mockito.kotlin:mockito-kotlin:4.1.0"
        )

        val uiTest = listOf(
            "androidx.test.ext:junit:1.1.5",
            "androidx.test.espresso:espresso-core:3.5.1",
            "androidx.compose.ui:ui-test-junit4:$composeVersion",
            "androidx.room:room-testing:$roomVersion"
        )
    }
}
