plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp") version "1.8.20-1.0.11"
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

    implementation(libs.bundles.common)
    implementation(libs.bundles.ui)

    ksp(libs.compose.destinations.ksp)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.cloud.messaging)

    implementation(libs.work)

    kapt(libs.hilt.compiler)

    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.uiTest)
}
