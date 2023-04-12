import dev.szymonchaber.checkstory.gradle.Dependencies

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("dependencies")
    id("com.google.devtools.ksp") version "1.8.10-1.0.9"
    id("library-config")
    id("kotlinx-serialization")
}

android {

    namespace = "dev.szymonchaber.checkstory.data"
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    hilt {
        enableAggregatingTask = true
    }
    sourceSets {
        getByName("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
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
    implementation("androidx.test:monitor:1.5.0")

    Dependencies.common.forEach(::implementation)
    Dependencies.ui.forEach(::implementation)

    kapt(Dependencies.hiltKapt)

    Dependencies.room.forEach(::implementation)
    ksp(Dependencies.roomKsp)

    implementation(Dependencies.rrule)
    implementation(Dependencies.dataStore)


    implementation(platform(Dependencies.firebasePlatform))
    implementation(Dependencies.crashlytics)
    implementation(Dependencies.auth)

    Dependencies.ktor.forEach(::implementation)
    implementation(Dependencies.kotlinx_datetime)

    Dependencies.unitTest.forEach(::testImplementation)
    Dependencies.unitTest.forEach(::androidTestImplementation)
    Dependencies.uiTest.forEach(::androidTestImplementation)
}
