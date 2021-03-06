import dev.szymonchaber.checkstory.gradle.Dependencies

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("dependencies")
    id("com.google.devtools.ksp") version "1.6.10-1.0.4"
    id("library-config")
}

android {
    namespace = "dev.szymonchaber.checkstory.notifications"
    hilt {
        enableAggregatingTask = true
    }
    libraryVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":design"))
    implementation(project(":data"))
    implementation(project(":navigation"))

    Dependencies.common.forEach(::implementation)
    Dependencies.ui.forEach(::implementation)

    Dependencies.composeDestinations.forEach(::implementation)
    ksp(Dependencies.composeDestinationsKsp)

    implementation(platform(Dependencies.firebasePlatform))
    implementation(Dependencies.messaging)

    implementation(Dependencies.work)

    kapt(Dependencies.hiltKapt)

    Dependencies.unitTest.forEach(::testImplementation)
    Dependencies.uiTest.forEach(::androidTestImplementation)
}
