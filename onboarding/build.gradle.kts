plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("library-config")
    id("kotlin-parcelize")
}

android {
    hilt {
        enableAggregatingTask = true
    }
}

android {
    namespace = "dev.szymonchaber.checkstory.onboarding"
    hilt {
        enableAggregatingTask = true
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":design"))
    implementation(project(":data"))
    implementation(project(":navigation"))

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.bundles.common)
    implementation(libs.bundles.ui)

    ksp(libs.hilt.compiler)

    ksp(libs.compose.destinations.ksp)

    debugImplementation(libs.compose.debugUiTooling)
    testImplementation(libs.bundles.unitTest)
    androidTestImplementation(libs.bundles.uiTest)
}
