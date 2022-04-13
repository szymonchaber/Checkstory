import dev.szymonchaber.checkstory.gradle.Dependencies

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
    id("dependencies")
    id("library-config")
}

android {
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")
    Dependencies.common.forEach(::implementation)

    kapt(Dependencies.hiltKapt)

    Dependencies.unitTest.forEach(::testImplementation)
}
