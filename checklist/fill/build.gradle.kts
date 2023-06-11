plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("dependencies")
    id("com.google.devtools.ksp") version "1.8.20-1.0.11"
    id("library-config")
}

android {
    namespace = "dev.szymonchaber.checkstory.checklist.fill"
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

    implementation(libs.bundles.common)
    implementation(libs.bundles.ui)

    ksp(libs.compose.destinations.ksp)

    kapt(libs.hilt.compiler)

    debugImplementation(libs.compose.debugUiTooling)

    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.uiTest)
}
